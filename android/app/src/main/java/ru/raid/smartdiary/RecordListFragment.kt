package ru.raid.smartdiary

import android.Manifest
import android.os.Bundle
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_note_list.*
import ru.raid.smartdiary.db.AppDatabase

class RecordListFragment : PermissionHelperFragment<PermissionTag>(PermissionTag.values()) {
    private var recordingUiState: Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_note_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val act = activity as MainActivity
        val recycleViewAdapter = RecordAdapter(act.playbackManager)
        AppDatabase.getInstance(context!!).recordDao().getAll().observe(::getLifecycle) {
            recycleViewAdapter.records = it
        }

        with(notesList) {
            layoutManager = LinearLayoutManager(this@RecordListFragment.context)
            recycledViewPool.setMaxRecycledViews(0, 10)
            adapter = recycleViewAdapter
        }

        addButton.setOnClickListener {
            if (act.recordingOn.value != true)
                startRecording()
            else
                act.stopRecording()
        }
        act.recordingOn.observe(::getLifecycle) {
            setRecordingUiState(it)
        }
    }

    override fun onPermissionsResult(tag: PermissionTag, granted: Boolean) {
        if (tag == PermissionTag.RECORDING_START && granted) {
            val mainActivity = activity as? MainActivity
            mainActivity?.startRecording()
        }
    }

    private fun setRecordingUiState(recordingOn: Boolean) {
        if (recordingUiState == recordingOn)
            return
        recordingUiState = recordingOn

        TransitionManager.beginDelayedTransition(view as ViewGroup)
        addButton.setImageResource(
                if (recordingOn) {
                    R.drawable.ic_stop
                } else {
                    R.drawable.ic_add
                }
        )

        val cSet = ConstraintSet()
        val layout = view as ConstraintLayout
        cSet.clone(layout)
        if (recordingOn) {
            cSet.connect(R.id.addButton, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT)
        } else {
            cSet.clear(R.id.addButton, ConstraintSet.LEFT)
        }
        cSet.applyTo(layout)
    }

    private fun startRecording() {
        withPermissions(
                arrayOf(Manifest.permission.RECORD_AUDIO),
                R.string.microphone_rationale,
                R.string.microphone_rationale_in_settings,
                PermissionTag.RECORDING_START
        )
    }

}

enum class PermissionTag {
    RECORDING_START, ASK_FOR_TALK
}
