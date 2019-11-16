package ru.raid.smartdiary

import android.Manifest
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_note_list.*
import ru.raid.smartdiary.db.AppDatabase
import ru.raid.smartdiary.db.Note

class NoteListFragment : PermissionHelperFragment<NoteListFragment.PermissionTag>(PermissionTag.values()) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_note_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val isTablet = resources.isTablet
        val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        val recycleViewAdapter = NoteAdapter(::showDetailedView)
        AppDatabase.getInstance(context!!).noteDao().getAll().observe(::getLifecycle) {
            recycleViewAdapter.notes = it
        }

        with(notesList) {
            layoutManager = LinearLayoutManager(this@NoteListFragment.context)
            recycledViewPool.setMaxRecycledViews(0, 10)
            adapter = recycleViewAdapter
            setHasFixedSize(true)
        }

        addButton.setOnClickListener { showCamera() }
    }

    private fun showDetailedView(note: Note) {
        val mainActivity = activity as? MainActivity
        mainActivity?.showDetailedNote(note)
    }

    private fun showCamera() {
        withPermissions(
            arrayOf(Manifest.permission.RECORD_AUDIO),
            R.string.camera_rationale,
            R.string.camera_rationale_in_settings,
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
