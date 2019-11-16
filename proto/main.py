import sys
import os
import scipy.io.wavfile
import pyaudio
import time
import struct
import wave
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

voka_path = "/home/raid/workspaces/Hacks/Junction2019/OpenVokaturi-3-3"

sys.path.append(voka_path + "/api")
import Vokaturi

Vokaturi.load(voka_path + "/lib/open/linux/OpenVokaturi-3-3-linux64.so")



def recode_data(data):
    res = []
    for i in range(len(data) // 4):
        part = data[i * 4 : (i + 1) * 4]
        [x] = struct.unpack('f', part)
        res.append(x)
    return np.array(res, dtype=np.float32)

def record(seconds):
    chunk = 1024  # Record in chunks of 1024 samples
    sample_format = pyaudio.paFloat32  # 16 bits per sample
    channels = 1
    fs = 44100  # Record at 44100 samples per second

    p = pyaudio.PyAudio()

    stream = p.open(format=sample_format,
                    channels=channels,
                    rate=fs,
                    frames_per_buffer=chunk,
                    input=True,
                    output=False)

    frames = []

    for i in range(0, int(fs / chunk * seconds)):
        data = stream.read(chunk)
        frames.append(data)

    stream.stop_stream()
    stream.close()

    p.terminate()
    return recode_data(b''.join(frames)), fs


def analyze(buffer, sample_rate):
    buffer_length = len(buffer)
    c_buffer = Vokaturi.SampleArrayC(buffer_length)
    c_buffer[:] = buffer[:]

    voice = Vokaturi.Voice(sample_rate, buffer_length)
    voice.fill(buffer_length, c_buffer)

    quality = Vokaturi.Quality()
    emotionProbabilities = Vokaturi.EmotionProbabilities()
    voice.extract(quality, emotionProbabilities)
    voice.destroy()

    if quality.valid:
        return emotionProbabilities
    else:
        return None

def write_wav(data):
    for p in data:
        print(p)
    print(len(data))


secs = int(sys.argv[1])
data, sr = record(secs)
res = analyze(data, sr)

if not os.path.isfile("data.csv"):
    with open("data.csv", "at") as f:
        f.write("timestamp,neutral,happy,sad,angry,fear\n")

if res is not None:
    with open("data.csv", "at") as f:
        f.write("%d,%.3f,%.3f,%.3f,%.3f,%.3f\n" % (
                time.time(),
                res.neutrality,
                res.happiness,
                res.sadness,
                res.anger,
                res.fear
            ))
else:
    print("None")


data = pd.read_csv("data.csv")
plt.figure()
for word in "neutral,happy,sad,angry,fear".split(","):
    v = data[word].as_matrix()
    plt.plot(v)

plt.show()

