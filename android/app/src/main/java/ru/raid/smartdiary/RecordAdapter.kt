package ru.raid.smartdiary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.raid.smartdiary.db.Record


typealias NoteSelectionListener = (Record) -> Unit

class RecordAdapter(private val listener: NoteSelectionListener, private val playback: RecordPlaybackManager) :
    RecyclerView.Adapter<RecordViewHolder>() {
    var records: List<Record> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.note_card, parent, false)
        return RecordViewHolder(view, playback)
    }

    override fun getItemCount(): Int = records.size

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        holder.bind(records[position])
    }

    override fun onViewRecycled(holder: RecordViewHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }
}
