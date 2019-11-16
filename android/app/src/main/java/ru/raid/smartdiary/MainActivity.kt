package ru.raid.smartdiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.lifecycleScope
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.raid.smartdiary.db.AppDatabase
import ru.raid.smartdiary.db.Record
import ru.raid.smartdiary.net.api
import java.io.File
import java.util.*
import kotlin.random.Random


class MainActivity : FragmentActivity() {
    private lateinit var _playbackManager: RecordPlaybackManager
    val playbackManager: RecordPlaybackManager
        get() = _playbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        _playbackManager = RecordPlaybackManager(this, lifecycleScope)
        pager.adapter = PagerAdapter(supportFragmentManager)
    }

    override fun onResume() {
        super.onResume()

        val mng = TalkIntentionManager(this)
        mng.scheduleAlarmAlmostNow(::askForTalk)
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
            val userId = getUserId()
            if (userId == null) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, R.string.network_error, Toast.LENGTH_SHORT).show()
                }
                return@launch
            }

            val body = MultipartBody.Part.createFormData("data", file.name, RequestBody.create(null, file))
            val response = api.addRecord(userId, body).execute().body()
            if (response == null) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, R.string.network_error, Toast.LENGTH_SHORT).show()
                }
            }
            // TODO smth with response
        }
    }

    fun askForTalk() {
        val sounds = arrayOf(R.raw.tts0, R.raw.tts1, R.raw.tts2)
        playbackManager.playExtra(sounds[Random.nextInt(sounds.size)])
        startRecording()
    }

    override fun onStop() {
        super.onStop()
        playbackManager.terminate()
    }

    private suspend fun getUserId(): Long? {
        val metaDao = AppDatabase.getInstance(this).metaDao()
        return withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            metaDao.atomicGet("user_id") {
                val res = api.addUser(UUID.randomUUID().toString().substring(0..20)).execute()
                res.body()?.uid?.toString()
            }
        }?.toLong()
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

    companion object {
        private val DETAILED_NOTE_FRAGMENT = "${MainActivity::class.java.canonicalName}.detailed_note_fragment"
        private val RECORDING_FRAGMENT = "${MainActivity::class.java.canonicalName}.recording_fragment"
        private val ACTION_START_RECORDING = "${MainActivity::class.java.canonicalName}.start_recording"

        fun startRecordingIntent(context: Context) =
                Intent(context, MainActivity::class.java).apply {
                    action = ACTION_START_RECORDING
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                }
    }
}
