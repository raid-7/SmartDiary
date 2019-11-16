package ru.raid.smartdiary


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.fragment_detailed_note.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.raid.smartdiary.db.AppDatabase
import ru.raid.smartdiary.db.Note


class DetailedNoteFragment: Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_detailed_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = arguments?.getLong(NOTE_ID) ?: throw IllegalStateException("Note id is not specified")
        val noteDao = AppDatabase.getInstance(context!!).noteDao()

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val note = noteDao.get(noteId)
            launch(Dispatchers.Main) {
                bindNote(checkNotNull(note) { "No such note" })
            }
        }
    }

    private fun bindNote(note: Note) {
        noteImage.setImageBitmap(note.bitmap)
        noteText.text = note.text
    }

    companion object {
        private const val NOTE_ID = "note_id"

        fun forNote(note: Note): DetailedNoteFragment {
            val fragment = DetailedNoteFragment()
            fragment.arguments = Bundle().apply {
                putLong(NOTE_ID, note.id)
            }
            return fragment
        }
    }
}
