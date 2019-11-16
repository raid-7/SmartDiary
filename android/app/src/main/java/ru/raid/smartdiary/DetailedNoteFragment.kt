package ru.raid.smartdiary


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.raid.smartdiary.db.AppDatabase
import ru.raid.smartdiary.db.Record


class DetailedNoteFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detailed_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = arguments?.getLong(NOTE_ID) ?: throw IllegalStateException("Note id is not specified")
        val noteDao = AppDatabase.getInstance(context!!).recordDao()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val note = noteDao.get(noteId)
            launch(Dispatchers.Main) {
                bindNote(checkNotNull(note) { "No such note" })
            }
        }
    }

    private fun bindNote(record: Record) {
        // TODO
    }

    companion object {
        private const val NOTE_ID = "note_id"

        fun forNote(record: Record): DetailedNoteFragment {
            val fragment = DetailedNoteFragment()
            fragment.arguments = Bundle().apply {
                putLong(NOTE_ID, record.id)
            }
            return fragment
        }
    }
}
