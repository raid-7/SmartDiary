package ru.raid.smartdiary

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.note_card.view.*
import ru.raid.smartdiary.db.Record
import ru.raid.smartdiary.net.AddRecordResponse
import java.text.SimpleDateFormat
import java.util.*

class RecordViewHolder(itemView: View, private val playback: RecordPlaybackManager) : RecyclerView.ViewHolder(itemView) {
    private lateinit var currentRecord: Record
    private val listener: RecordPlaybackListener = {
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
            recordSmile.setImageResource(record.info?.let { getEmotionDrawable(it) } ?: 0)
        }
    }

    fun recycle() {
        playback.removeListener(listener)
    }

    private fun playAudio(record: Record) {
        playback.play(record)
    }

    companion object {
        private val EMOTION_MAPPING = mapOf(
                AddRecordResponse::anger to R.drawable.ic_em_angry,
                AddRecordResponse::sadness to R.drawable.ic_em_sad,
                AddRecordResponse::fear to R.drawable.ic_em_fear,
                AddRecordResponse::happiness to R.drawable.ic_em_happy
        )

        private fun getEmotionDrawable(resp: AddRecordResponse): Int {
            return EMOTION_MAPPING.map {
                it.key.get(resp) to it.value
            }.maxBy {
                it.first
            }?.second ?: EMOTION_MAPPING.values.first()
        }
    }
}
