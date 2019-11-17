from pydub import AudioSegment

def convert(filename):
    # path = dir "/user/tmp/
    # filename = <filename.m4a>
    try:
        track = AudioSegment.from_file(filename, 'm4a')
        wav_filename = filename.replace('m4a', 'wav')
        file_handle = track.export(wav_filename, format='wav')
        return wav_filename
    except:
        raise RuntimeError("ERROR CONVERTING " + str(filename))
