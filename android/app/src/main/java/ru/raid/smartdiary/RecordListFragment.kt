package ru.raid.smartdiary

import android.Manifest
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_note_list.*
import ru.raid.smartdiary.db.AppDatabase
import ru.raid.smartdiary.db.Record

class RecordListFragment : PermissionHelperFragment<RecordListFragment.PermissionTag>(PermissionTag.values()) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_note_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val act = activity as MainActivity
        val recycleViewAdapter = RecordAdapter(::showDetailedView, act.playbackManager)
        AppDatabase.getInstance(context!!).recordDao().getAll().observe(::getLifecycle) {
            recycleViewAdapter.records = it
        }

        with(notesList) {
            layoutManager = LinearLayoutManager(this@RecordListFragment.context)
            recycledViewPool.setMaxRecycledViews(0, 10)
            adapter = recycleViewAdapter
        }

        addButton.setOnClickListener { startRecording() }
    }

    private fun showDetailedView(record: Record) {
        val mainActivity = activity as? MainActivity
        mainActivity?.showDetailedNote(record)
    }

    private fun startRecording() {
        withPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO),
            R.string.microphone_rationale,
            R.string.microphone_rationale_in_settings,
            PermissionTag.RECORDING_START
        )
    }

    override fun onPermissionsResult(tag: PermissionTag, granted: Boolean) {
        if (tag == PermissionTag.RECORDING_START && granted) {
            val mainActivity = activity as? MainActivity
            mainActivity?.startRecording()
        }
    }

    enum class PermissionTag {
        RECORDING_START
    }
}
