from flask import Flask, request, jsonify, redirect, abort, send_file
from main import *
import os
from convertor import convert
import io


application = Flask(__name__, static_url_path='')
application.config["DEBUG"] = True
application.config["UPLOAD_FOLDER"] = "/home/dimak/hackathons/junction/records"


def get(d, key):
    return d[key] if key in d else None


@application.route('/record', methods=['POST'])
def api_record():
    if 'u_id' not in request.form:
        return f"Error: <u_id> arg is not provided"

    if 'data' not in request.files:
        return f"Error: <data> arg is not provided"

    try:
        filename = os.path.join(application.config['UPLOAD_FOLDER'],
                                request.files['data'].filename)
        request.files['data'].save(filename)
        filename = convert(filename)

        return jsonify(dict(
            zip(['score', 'is_anomaly', 'neutrality', 'happiness', 'sadness', 'anger', 'fear', 'avatar_level', 'text'],
                insert_record(request.form['u_id'], 
                      os.path.join(application.config['UPLOAD_FOLDER'],
                                   filename),
                      get(request.form, 'timestamp')))))

    except Exception as e: abort(500, description=str(e))


@application.route('/add_user', methods=['POST'])
def api_create_user():
    if 'name' not in request.args:
        return "Error: <name> arg is not provided"

    try:
        u_id = create_user(request.args['name'], get(request.args, 'age'),
                           get(request.args, 'avatar_name'))
        return jsonify({'u_id': u_id})

    except Exception as e: abort(500, description=str(e))


@application.route('/get_records', methods=['GET'])
def api_get_records():
    if 'u_id' not in request.args:
        return "Error: <u_id> arg in not provided"

    try:
        return jsonify(
            dict(zip(['timestamp', 'data'], 
                     get_records(request.args['u_id'],
                                 get(request.args, 'date_from'),
                                 get(request.args, 'date_to'),
                                 get(request.args, 'phrase')))))
    
    except Exception as e: abort(500, description=str(e))

@application.route('/get_audio', methods=['GET'])
def get_audio():
    for arg in ['u_id', 'timestamp']:
        if arg not in request.args:
            return f"Error: <{arg}> arg is not provided"

    try:
        q = load_record(request.args["u_id"], request.args["timestamp"])
        return send_file(io.BytesIO(q),
            attachment_filename=f'{request.args["u_id"]}-{request.args["timestamp"]}.wav',
            mimetype='audio/wav')
    except Exception as e: abort(500, description=str(e))



if __name__ == '__main__':
    drop_tables()
    create_tables()
    application.run('0.0.0.0')
