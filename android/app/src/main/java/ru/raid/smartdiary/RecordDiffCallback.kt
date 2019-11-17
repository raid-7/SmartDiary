package ru.raid.smartdiary

import androidx.recyclerview.widget.DiffUtil
import ru.raid.smartdiary.db.Record

class RecordDiffCallback(private val old: List<Record>, private val new: List<Record>) : DiffUtil.Callback() {
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }

    override fun getOldListSize(): Int = old.size

    override fun getNewListSize(): Int = new.size

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return old[oldItemPosition] == new[newItemPosition]
    }
}