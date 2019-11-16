package ru.raid.smartdiary

import android.app.AlarmManager
import android.content.Context
import java.util.*

class TalkIntentionManager(private val context: Context) {
    private val alarmManager: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAlarmAlmostNow(listener: () -> Unit) {
        val targetTime = Calendar.getInstance().timeInMillis + 4_000
        alarmManager.set(AlarmManager.RTC_WAKEUP, targetTime, ALARM_TAG, listener, null)
    }

    companion object {
        private const val ALARM_TAG = "TALK_INTENT_ALARM"
    }
}
