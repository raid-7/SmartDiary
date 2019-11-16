package ru.raid.smartdiary

import android.content.res.Resources
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import ru.raid.smartdiary.db.AppDatabase
import ru.raid.smartdiary.db.Note
import java.io.File

val Resources.isTablet: Boolean
    get() = getBoolean(R.bool.is_tablet)

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentSelection, NoteListFragment())
                .commit()
        }
    }

    fun showDetailedNote(note: Note) {
        supportFragmentManager.popBackStack(DETAILED_NOTE_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentInfo, DetailedNoteFragment.forNote(note))
            .addToBackStack(DETAILED_NOTE_FRAGMENT)
            .commit()
    }

    fun startRecording() {
        // TODO
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentInfo, CameraFragment())
//            .addToBackStack(null)
//            .commit()
    }

    fun onPictureCaptured(file: File) {
        supportFragmentManager.popBackStack()

        val noteDao = AppDatabase.getInstance(this).noteDao()
        noteDao.insert(NoteGenerator.generateNote(file))
    }

    companion object {
        private val DETAILED_NOTE_FRAGMENT = "detailed_note"
    }
}
