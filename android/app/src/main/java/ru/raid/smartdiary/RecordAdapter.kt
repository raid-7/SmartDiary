package ru.raid.smartdiary

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.raid.smartdiary.db.Record


class RecordAdapter(private val playback: RecordPlaybackManager, private val scope: CoroutineScope) :
    RecyclerView.Adapter<RecordViewHolder>() {
    var records: List<Record> = emptyList()
        set(value) {
            val old = field
            field = value
            applyChanges(old, value)
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

    private fun applyChanges(old: List<Record>, new: List<Record>) {
        scope.launch(Dispatchers.IO) {
            val diff = DiffUtil.calculateDiff(RecordDiffCallback(old, new), true)
            launch(Dispatchers.Main) {
                diff.dispatchUpdatesTo(this@RecordAdapter)
            }
        }
    }
}
