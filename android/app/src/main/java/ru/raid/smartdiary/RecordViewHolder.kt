package ru.raid.smartdiary

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.note_card.view.*
import ru.raid.smartdiary.db.Record
import java.text.SimpleDateFormat
import java.util.*

class RecordViewHolder(itemView: View, private val playback: RecordPlaybackManager) : RecyclerView.ViewHolder(itemView) {
    private lateinit var currentRecord: Record
    private val listener: RecordPlaybackListener = { id ->
        if (::currentRecord.isInitialized)
            bind(currentRecord)
    }

    init {
        itemView.setOnClickListener {
            playAudio(currentRecord)
        }
        playback.addListener(listener)
    }

    fun bind(record: Record) {
        currentRecord = record
        val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT)
        with(itemView) {
            recordDate.text = dateFormat.format(Date(record.date))
            playButton.setImageResource(
                    if (playback.isPlaying(record)) {
                        R.drawable.ic_pause_circle_outline
                    } else {
                        R.drawable.ic_play_circle_outline
                    }
            )
        }
    }

    fun recycle() {
        playback.removeListener(listener)
    }

    private fun playAudio(record: Record) {
        playback.play(record)
    }
}
