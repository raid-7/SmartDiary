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

typealias RecordPlaybackListener = () -> Unit

class RecordPlaybackManager(private val context: Context, private val scope: CoroutineScope) {
    private val mediaPlayer = MediaPlayer()
    private var playing: PlayingSound? = null

    private val listeners = mutableSetOf<RecordPlaybackListener>()

    init {
        mediaPlayer.setOnCompletionListener {
            scope.launch(Dispatchers.Main) {
                stopPlaying()
            }
        }
    }

    fun play(record: Record) {
        val oldId = playing
        if (oldId?.type == SoundType.EXTRA)
            return

        if (oldId != null)
            stopPlaying()

        if (oldId?.id != record.id)
            startPlaying(record)
    }

    fun isPlaying(record: Record) = playing?.let {
        it.type == SoundType.RECORD && it.id == record.id
    } ?: false

    fun addListener(l: RecordPlaybackListener) {
        listeners.add(l)
    }

    fun removeListener(l: RecordPlaybackListener) {
        listeners.remove(l)
    }

    fun playExtra(resourceId: Int) {
        terminate()

        playing = PlayingSound(SoundType.EXTRA)
        scope.launch(Dispatchers.IO) {
            mediaPlayer.apply {
                setAudioAttributes(
                        AudioAttributes.Builder()
                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                .build()
                )
                setDataSource(context.resources.openRawResourceFd(resourceId))
                prepare()
                start()
            }
        }
    }

    fun terminate() {
        if (playing != null)
            stopPlaying()
    }

    private fun startPlaying(record: Record) {
        playing = PlayingSound(SoundType.RECORD, record.id)
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
        triggerListeners()
    }

    private fun stopPlaying() {
        scope.launch(Dispatchers.IO) {
            mediaPlayer.stop()
            mediaPlayer.reset()
        }
        val currentPlaying = playing
        playing = null
        if (currentPlaying != null)
            triggerListeners()
    }

    private fun triggerListeners() {
        listeners.forEach {
            it()
        }
    }

    enum class SoundType {
        RECORD, EXTRA
    }

    class PlayingSound(
            val type: SoundType,
            val id: Long? = null
    )
}
