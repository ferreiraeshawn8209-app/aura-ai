package com.aura.ai.domain.usecase

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.aura.ai.util.ReminderReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SetReminderUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Schedules an exact alarm at [epochMillis] to fire [ReminderReceiver].
     * Requires [android.Manifest.permission.SCHEDULE_EXACT_ALARM] on API 31+.
     */
    fun execute(title: String, description: String, epochMillis: Long): Result<Unit> = runCatching {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_DESCRIPTION, description)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            epochMillis.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            // Fall back to inexact alarm
            alarmManager.set(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
        } else {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
        }
    }
}
