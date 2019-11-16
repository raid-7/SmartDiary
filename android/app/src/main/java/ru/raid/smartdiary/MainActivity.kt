package ru.raid.smartdiary

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.raid.smartdiary.db.AppDatabase
import ru.raid.smartdiary.db.Record
import java.io.File
import java.util.*


class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        pager.adapter = PagerAdapter(supportFragmentManager)
    }

    fun showDetailedNote(record: Record) {
        supportFragmentManager.popBackStack(DETAILED_NOTE_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentInfo, DetailedNoteFragment.forNote(record))
                .addToBackStack(DETAILED_NOTE_FRAGMENT)
                .commit()
    }

    fun startRecording() {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentInfo, AudioRecorderFragment())
                .addToBackStack(RECORDING_FRAGMENT)
                .commit()
    }

    fun onAudioReady(file: File) {
        supportFragmentManager.popBackStack(RECORDING_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE)

        val noteDao = AppDatabase.getInstance(this).recordDao()
        lifecycleScope.launch(Dispatchers.IO) {
            noteDao.insert(Record(0, file.absolutePath, Calendar.getInstance().timeInMillis))
        }
    }

    companion object {
        private val DETAILED_NOTE_FRAGMENT = "detailed_note"
        private val RECORDING_FRAGMENT = "recording_fragment"
    }

    class PagerAdapter(fragmentManager: FragmentManager)
        : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int) =
                if (position == 0) {
                    RecordListFragment()
                } else {
                    AvatarFragment()
                }

        override fun getCount(): Int = 2

    }
}
