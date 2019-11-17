package ru.raid.smartdiary

import android.app.AlarmManager
import android.content.Context
import kotlinx.coroutines.delay
import java.util.*
import kotlin.math.max

class TalkIntentionManager(private val context: Context) {
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private var lastBubbleTimestamp: Long = 0

    fun scheduleAlarmAlmostNow(listener: () -> Unit) {
        val targetTime = Calendar.getInstance().timeInMillis + 16_000
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetTime, ALARM_TAG, listener, null)
    }

    suspend fun waitForBubble() {
        val current = System.currentTimeMillis()
        val waitTime = max(2000, 40_000 - (current - lastBubbleTimestamp))
        delay(waitTime)
        lastBubbleTimestamp = System.currentTimeMillis()
    }

    companion object {
        private const val ALARM_TAG = "TALK_INTENT_ALARM"
    }
}
