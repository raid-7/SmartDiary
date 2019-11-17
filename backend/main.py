import psycopg2
import datetime
import time
import sys
import requests
import scipy.io.wavfile
import json

import vokaturi.Vokaturi as Vokaturi
Vokaturi.load("./vokaturi/OpenVokaturi-3-3-linux64.so")

from ms_text_analysis import *
from cassandra_test import *


MSSp = MSSpeechToText()
MST  = MSTextAnalysis()
MSAD = MSAnomalyDetector()

CApi = CassandraAPI()


def get_emotions(filename):
    (sample_rate, samples) = scipy.io.wavfile.read(filename)

    buffer_length = len(samples)
    c_buffer = Vokaturi.SampleArrayC(buffer_length)
    if samples.ndim == 1:
        c_buffer[:] = samples[:] / 32768.0
    else:
        c_buffer[:] = 0.5 * (samples[:, 0] + samples[:, 1]) / 32768.0

    voice = Vokaturi.Voice(sample_rate, buffer_length)
    voice.fill(buffer_length, c_buffer)
    quality = Vokaturi.Quality()
    emotionProbabilities = Vokaturi.EmotionProbabilities()
    voice.extract(quality, emotionProbabilities)
    voice.destroy()

    if quality.valid:
        return ("%.3f" % emotionProbabilities.neutrality,
                "%.3f" % emotionProbabilities.happiness,
                "%.3f" % emotionProbabilities.sadness,
                "%.3f" % emotionProbabilities.anger,
                "%.3f" % emotionProbabilities.fear)
    else: raise RuntimeError('bad quality')


DB_NAME  = "defaultdb"
USER     = "avnadmin"
PASSWORD = "hq3fi662tthholn2"
HOST     = "pg-2e774192-dimak24-5fb9.aivencloud.com"
PORT     = "21756"


INFLUXDB_HOST     = "influx-1ab60b47-dimak24-5fb9.aivencloud.com"
INFLUXDB_PORT     = "21756"
INFLUXDB_DB_NAME  = "defaultdb"
INFLUXDB_USER     = "avnadmin"
INFLUXDB_PASSWORD = "e6gkm3n9bmvcbpfb"


def _execute_op(operation):
    conn = psycopg2.connect(
        database=DB_NAME, 
        user=USER, 
        password=PASSWORD, 
        host=HOST, 
        port=PORT)

    cur = conn.cursor()

    try:
        res = operation(cur)
        conn.commit()
        return res
    except psycopg2.Error as e:
        print(e)
        # raise
    finally:
        cur.close()
        conn.close()


def _execute(*args):
    _execute_op(lambda cur: cur.execute(*args))

def _execute_fetch(*args):
    def _op(cur):
        cur.execute(*args)
        return cur.fetchall()

    return _execute_op(_op)


def _influxdb_query(query):
    return json.loads(requests.post(f'https://{INFLUXDB_USER}:{INFLUXDB_PASSWORD}@{INFLUXDB_HOST}:{INFLUXDB_PORT}/query?db={INFLUXDB_DB_NAME}',
                         data='q=' + query, headers={'content-type': 'application/x-www-form-urlencoded'}).text)

def _influxdb_write(measurement, args):
    query = ', '.join([','.join([f'{tag["name"]}={tag["value"]}' for tag in arg['tags']]) + f' value={arg["value"]}' for arg in args])

    return requests.post(f'https://{INFLUXDB_USER}:{INFLUXDB_PASSWORD}@{INFLUXDB_HOST}:{INFLUXDB_PORT}/write?db={INFLUXDB_DB_NAME}',
                  data=f'{measurement},{query} {int(time.time() * 1e9)}',
                  headers={'content-type': 'application/x-www-form-urlencoded'}).text


def cassandra_insert(u_id, timestamp, filename, comment='comment'):
    with open(filename, 'rb') as file:
        print(CApi.db_execute("""INSERT INTO cycling.records (u_d,r_time,audio,comment)
                           VALUES(%s,%s,%s,%s)""", 
                           (str(u_id), timestamp, file.read(), comment)))


def load_record(u_id, timestamp):
    result_set = CApi.db_query("SELECT * FROM cycling.records where u_d=%s and r_time=%s ALLOW FILTERING;", (u_id, int(timestamp)))
    for res in result_set: return res.audio



def create_tables():
    _execute('''CREATE TABLE diary
                (u_id    INT       NOT NULL,
                 r_time  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                 script  TEXT      NOT NULL,
                 score      REAL   NOT NULL,
                 is_anomaly INT    NOT NULL, 
                 neutrality REAL   NOT NULL,
                 happiness  REAL   NOT NULL,
                 sadness    REAL   NOT NULL,
                 anger      REAL   NOT NULL,
                 fear       REAL   NOT NULL);''')

    _execute('''CREATE TABLE users
                (u_id SERIAL,
                 name CHAR(30) NOT NULL,
                 age  INT);''')

    _execute('''CREATE TABLE avatars
                (u_id INT      NOT NULL,
                 name CHAR(50) NOT NULL,
                 level INT     NOT NULL DEFAULT 0);''')


    CApi.db_execute("""CREATE KEYSPACE cycling WITH REPLICATION = {
                           'class' : 'SimpleStrategy',
                           'replication_factor' : 1
                        };""")

    CApi.db_execute("""CREATE TABLE records (
       u_d text PRIMARY KEY,
       r_time int,
       audio blob,
       comment text );""")



# debug
def drop_tables():
    return
    CApi.db_execute('DROP TABLE records;')
    _execute('DROP TABLE diary, users, avatars;')
    _influxdb_query('DROP MEASUREMENT mental_metrics')


def create_user(name, age=None, avatar_name=None):
    if avatar_name is None:
        avatar_name = f'{name}\'s avatar'

    assert len(name) <= 30
    assert len(avatar_name) <= 50

    if age is not None:
        res = _execute_fetch('''INSERT INTO users (name, age)
                                VALUES (%s, %s) RETURNING u_id;''', (name, age))
    else:
        res = _execute_fetch('''INSERT INTO users (name)
                                VALUES (%s) RETURNING u_id;''', (name,))

    u_id = res[0][0]

    _execute('''INSERT INTO avatars (u_id, name)
                VALUES (%s, %s);''', (u_id, avatar_name))

    return u_id


def to_timestamp(influxdb_date):
    d, t = influxdb_date[:-1].split('T')
    h, m, s = t.split(':')

    s = int(s.split('.')[0])

    h = int(h) + 3

    return int(datetime.datetime(*list(map(int, d.split('-'))), h, int(m), s).strftime("%s"))

def to_azure(timestamp):
    _date = datetime.date.fromtimestamp(timestamp)
    return f'{_date.year}-{_date.month}-{_date.day}T12:00:00Z'


def make_daily_series(series):
    s, n, last = 0, 0, None
    res = []
    for record in sorted(series, key=lambda _record: to_timestamp(_record[0])) + [(-1, -1)]:
        timestamp, metric = record
        if timestamp != -1:
            date = int(datetime.date.fromtimestamp(to_timestamp(timestamp)).strftime("%s"))
        else: date = -2
        if date != last:
            if last is not None:
                s /= n
                if len(res) > 0: mean = (s + res[-1][1]) / 2
                while len(res) > 0 and date - res[-1][0] > 86400 * 2:
                    res.append([res[-1][0] + 86400, mean])
                res.append([last, s])
            last = date
            n, s = 0, 0
        s += metric
        n += 1
    for i in range(len(res)): res[i][0] = to_azure(res[i][0])
    return res


def insert_record(u_id, data_file, date=int(time.time())):
    if date is None: date=int(time.time())

    text = MSSp.process(data_file)
    score = MST.process(text)
    metrics = get_emotions(data_file)

    for type, value in zip(['neutrality', 'happiness', 'sadness', 'anger', 'fear', 'score'],
                           metrics + (score,)):
        _influxdb_write('mental_metrics', 
                        [{'tags': [
                            {'name': 'u_id', 'value': u_id},
                            {'name': 'type', 'value': type}], 
                         'value': value}])

    res = _influxdb_query('SELECT "time","value" FROM "mental_metrics" WHERE u_id=\'%s\' AND type=\'score\''%u_id)
    try:
        series = res['results'][0]['series'][0]['values']
        series = make_daily_series(series)
        if len(series) < 12: is_anomaly = 0
        else: is_anomaly = MSAD.process(series)
    except:
        print(res)
        raise

    new_level = _execute_fetch('''UPDATE avatars SET level = level + 1 WHERE u_id = %s
                                  RETURNING level;''', (u_id,))[0][0]

    print(u_id, date)
    cassandra_insert(u_id, date, data_file)

    return _execute_fetch('''INSERT INTO diary (u_id, r_time, script, score, is_anomaly, neutrality, happiness, sadness, anger, fear)
                             VALUES (%s, to_timestamp(%s), %s, %s, %s, %s, %s, %s, %s, %s)
                             RETURNING score, is_anomaly, neutrality, happiness, sadness, anger, fear;''',
                         (u_id, date, text, score, is_anomaly, *metrics))[0] + (int(new_level), text)


def get_records(u_id, date_from=None, date_to=None, phrase=None):
    date_range = ''
    if date_from is not None:
        date_range += f" AND r_time >= to_timestamp('{date_from}', 'yyyy-mm-dd')"
    if date_to is not None:
        date_range += f" AND r_time < to_timestamp('{date_to}', 'yyyy-mm-dd')"

    if phrase is not None:
        return _execute_fetch(f"""SELECT r_time, script FROM diary
                                 WHERE u_id = {u_id} {date_range} AND
                                       data LIKE '%{phrase}%'""")
    return _execute_fetch(f"""SELECT r_time, script FROM diary
                                 WHERE u_id = {u_id} {date_range}""")


def get_audio(u_id, timestamp):
    return load_record(u_id, timestamp)
