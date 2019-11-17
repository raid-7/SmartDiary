# -*- coding: utf-8 -*-
#
# reqs: pip install azure-cognitiveservices-speech
# reqs pip install azure-cognitiveservices-anomalydetector
# reqs: pip install requests

import requests
import azure.cognitiveservices.speech as speechsdk
import time

from azure.cognitiveservices.anomalydetector import AnomalyDetectorClient
from azure.cognitiveservices.anomalydetector.models import Request, Point, Granularity, APIErrorException
from msrest.authentication import CognitiveServicesCredentials
import pandas as pd

class MSTextAnalysis:
    def __init__(self):
        self.subscription_key = '35b3ac31ea6b4b8b9d5bfb094f7d83b9'
        self.endpoint = 'https://testsptotxt.cognitiveservices.azure.com/'

        self.sentiment_url = self.endpoint + "/text/analytics/v2.1/sentiment"
        self.headers = {"Ocp-Apim-Subscription-Key": self.subscription_key}

    def process(self, text):
        documents = {"documents": [
            {"id": "1",
             "language": "en",
             "text": text}
        ]}
        try:
            response = requests.post(self.sentiment_url, headers=self.headers, json=documents)
            sentiments = response.json()
            return sentiments["documents"][0]["score"]
        except: raise RuntimeError('jopa')

class MSSpeechToText:
    def __init__(self):
        speech_key = "5e9367dbea5448b98b4a2d823068476c"
        service_region = "northeurope"
        self.speech_config = speechsdk.SpeechConfig(subscription=speech_key, region=service_region)
        self.result_text = ''

    def process(self, wav_file):
        self.result_text = ''

        audio_filename = wav_file
        audio_input = speechsdk.AudioConfig(filename=audio_filename)

        # Creates a recognizer with the given settings
        speech_recognizer = speechsdk.SpeechRecognizer(speech_config=self.speech_config, audio_config=audio_input)

        done = False

        def stop_cb(evt):
            """callback that stops continuous recognition upon receiving an event `evt`"""
            speech_recognizer.stop_continuous_recognition()
            nonlocal done
            done = True

        def new_text(evt):
            self.result_text += evt.result.text

        # Connect callbacks to the events fired by the speech recognizer
        speech_recognizer.recognized.connect(new_text)
        speech_recognizer.session_stopped.connect(stop_cb)
        speech_recognizer.canceled.connect(stop_cb)

        # Start continuous speech recognition
        speech_recognizer.start_continuous_recognition()
        while not done:
            time.sleep(.5)
        return self.result_text

class MSAnomalyDetector:
    def __init__(self):
        self.subscription_key = '00e8002c1b9243b8ab8eeabf25f3dc66'
        self.endpoint = 'https://junc-anomaly-detector.cognitiveservices.azure.com/'

        self.client = AnomalyDetectorClient(self.endpoint, CognitiveServicesCredentials(self.subscription_key))

    def process(self, series):
        request = Request(series=[Point(timestamp=x, value=y) for x, y in series], granularity=Granularity.daily)

        try:
            response = self.client.last_detect(request)
        except: raise

        return int(response.is_anomaly)
