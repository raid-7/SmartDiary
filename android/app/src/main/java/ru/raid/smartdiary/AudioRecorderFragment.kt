package ru.raid.smartdiary


import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import java.io.File
import java.io.IOException
import java.util.*

class AudioRecorderFragment : Fragment() {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var recordingOn = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_audio_recorder, container, false)
    }

    override fun onResume() {
        super.onResume()
        startRecording()
    }

    override fun onPause() {
        super.onPause()
        stopRecording()
    }

    private fun stopRecording() {
        if (recordingOn) {
            recorder?.apply {
                stop()
                release()
            }
            recordingOn = false

            (activity as? MainActivity)?.onAudioReady(outputFile!!)
        }
        recorder = null
    }

    private fun startRecording() {
        if (recordingOn)
            return

        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            outputFile = nextRandomFile()
            setOutputFile(outputFile!!.absolutePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("Sound", "prepare() failed")
                showError()
            }

            start()
        }

        recordingOn = true
    }

    private fun showError() {
        // TODO
    }

    private fun nextRandomFile() =
            File(context!!.filesDir, UUID.randomUUID().toString())
}
