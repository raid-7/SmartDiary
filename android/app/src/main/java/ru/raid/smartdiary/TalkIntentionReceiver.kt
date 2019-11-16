package ru.raid.smartdiary

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class TalkIntentionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(context, "Test!!! ${intent.action}", Toast.LENGTH_LONG).show()

        if (intent.action != ACTION)
            return

        context.startActivity(MainActivity.startRecordingIntent(context))
    }

    companion object {
        private const val ACTION = "ACTION_START_TALKING"

        fun getIntent(context: Context) = Intent(context, TalkIntentionReceiver::class.java).apply {
            action = ACTION
        }
    }
}
