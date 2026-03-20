package com.goi.foodtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class NotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                notificationScheduler.rescheduleAll()
            }
            NotificationHelper.ACTION_SHOW -> {
                NotificationHelper.showNotification(context)
            }
            NotificationHelper.ACTION_SNOOZE -> {
                NotificationHelper.cancelNotification(context)
                scheduleSnooze(context)
            }
            NotificationHelper.ACTION_DISMISS -> {
                NotificationHelper.cancelNotification(context)
            }
        }
    }

    private fun scheduleSnooze(context: Context) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val triggerAt = System.currentTimeMillis() + 30 * 60 * 1000L
        val pendingIntent = PendingIntent.getBroadcast(
            context, 999,
            Intent(context, NotificationReceiver::class.java).setAction(NotificationHelper.ACTION_SHOW),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }
}
