package ru.raid.smartdiary

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.raid.smartdiary.db.Record
import java.io.File

typealias RecordPlaybackListener = (Long) -> Unit

class RecordPlaybackManager(private val context: Context, private val scope: CoroutineScope) {
    private val mediaPlayer = MediaPlayer()
    private var playingRecordId: Long? = null

    private val listeners = mutableSetOf<RecordPlaybackListener>()

    init {
        mediaPlayer.setOnCompletionListener {
            scope.launch(Dispatchers.Main) {
                stopPlaying()
            }
        }
    }

    fun play(record: Record) {
        val oldId = playingRecordId
        if (oldId != null)
            stopPlaying()

        if (oldId != record.id)
            startPlaying(record)
    }

    fun isPlaying(record: Record) = playingRecordId == record.id

    fun addListener(l: RecordPlaybackListener) {
        listeners.add(l)
    }

    fun removeListener(l: RecordPlaybackListener) {
        listeners.remove(l)
    }

    private fun startPlaying(record: Record) {
        playingRecordId = record.id
        scope.launch(Dispatchers.IO) {
            mediaPlayer.apply {
                setAudioAttributes(
                        AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                )
                setDataSource(context, Uri.fromFile(File(record.soundPath)))
                prepare()
                start()
            }
        }
        triggerListeners(record.id)
    }

    private fun stopPlaying() {
        scope.launch(Dispatchers.IO) {
            mediaPlayer.stop()
            mediaPlayer.reset()
        }
        val recordId = playingRecordId
        playingRecordId = null
        if (recordId != null)
            triggerListeners(recordId)
    }

    private fun triggerListeners(recordId: Long) {
        listeners.forEach {
            it(recordId)
        }
    }
}
