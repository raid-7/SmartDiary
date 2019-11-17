package ru.raid.smartdiary

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.lifecycle.MutableLiveData
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
import java.io.IOException
import java.util.*
import kotlin.random.Random


class MainActivity : FragmentActivity() {
    lateinit var playbackManager: RecordPlaybackManager
        private set

    lateinit var talkIntentionManager: TalkIntentionManager
        private set

    val recordingOn: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playbackManager = RecordPlaybackManager(this, lifecycleScope)
        talkIntentionManager = TalkIntentionManager(this)
        pager.adapter = PagerAdapter(supportFragmentManager)

        recordingOn.observe(::getLifecycle) {
            if (it) {
                pager.currentItem = 1
            }
        }
    }

    fun startRecording() {
        if (isDestroyed)
            return

        supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentInfo, AudioRecorderFragment())
                .addToBackStack(RECORDING_FRAGMENT)
                .commit()
        recordingOn.postValue(true)
    }

    fun stopRecording() {
        supportFragmentManager.popBackStack(RECORDING_FRAGMENT, FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    fun onAudioReady(file: File) {
        recordingOn.postValue(false)

        val recordDao = AppDatabase.getInstance(this).recordDao()
        val metaDao = AppDatabase.getInstance(this).metaDao()
        lifecycleScope.launch(Dispatchers.IO) {
            val record = Record(0, file.absolutePath, Calendar.getInstance().timeInMillis, null)
            val rowId = recordDao.insert(record)
            val rollback: suspend () -> Unit = {
                recordDao.delete(Record(rowId, record.soundPath, record.date, null))
            }

            val userId = getUserId()
            if (userId == null) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, R.string.network_error, Toast.LENGTH_SHORT).show()
                }
                rollback()
                return@launch
            }

            val body = MultipartBody.Part.createFormData("data", file.name, RequestBody.create(null, file))
            val response = try {
                api.addRecord(userId, body).execute().body()
            } catch (exc: IOException) {
                exc.printStackTrace()
                null
            }
            if (response == null) {
                launch(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, R.string.network_error, Toast.LENGTH_SHORT).show()
                }
                rollback()
                return@launch
            }

            metaDao.insert(ru.raid.smartdiary.db.Metadata.AVATAR_LEVEL, response.avatar_level.toString())
            recordDao.update(Record(rowId, record.soundPath, record.date, response))
        }
    }

    fun askForTalk() {
        playbackManager.playExtra(ASK_SOUNDS[Random.nextInt(ASK_SOUNDS.size)])
        startRecording()
    }

    override fun onStop() {
        super.onStop()
        playbackManager.terminate()
    }

    private suspend fun getUserId(): Long? {
        val metaDao = AppDatabase.getInstance(this).metaDao()
        return withContext(lifecycleScope.coroutineContext + Dispatchers.IO) {
            metaDao.atomicGet(ru.raid.smartdiary.db.Metadata.USER_ID) {
                try {
                    val res = api.addUser(UUID.randomUUID().toString().substring(0..20)).execute()
                    res.body()?.uid?.toString()
                } catch (exc: IOException) {
                    exc.printStackTrace()
                    null
                }
            }
        }?.toLong()
    }

    class PagerAdapter(fragmentManager: FragmentManager)
        : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
        override fun getItem(position: Int) =
                if (position == 1) {
                    RecordListFragment()
                } else {
                    AvatarFragment()
                }

        override fun getCount(): Int = 2
    }

    companion object {
        private val RECORDING_FRAGMENT = "${MainActivity::class.java.canonicalName}.recording_fragment"
        private val ACTION_START_RECORDING = "${MainActivity::class.java.canonicalName}.start_recording"
        private val ASK_SOUNDS = arrayOf(R.raw.tts0, R.raw.tts1, R.raw.tts2)

        fun startRecordingIntent(context: Context) =
                Intent(context, MainActivity::class.java).apply {
                    action = ACTION_START_RECORDING
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                }
    }
}
