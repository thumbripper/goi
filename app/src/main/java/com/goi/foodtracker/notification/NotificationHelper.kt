package com.goi.foodtracker.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.goi.foodtracker.MainActivity
import com.goi.foodtracker.R

object NotificationHelper {

    const val CHANNEL_ID = "food_tracker_reminders"
    const val NOTIFICATION_ID = 1001

    const val ACTION_SHOW = "com.goi.foodtracker.ACTION_SHOW_NOTIFICATION"
    const val ACTION_SNOOZE = "com.goi.foodtracker.ACTION_SNOOZE"
    const val ACTION_DISMISS = "com.goi.foodtracker.ACTION_DISMISS"

    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Food Tracker Reminders",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Reminders to log your food and symptoms"
        }
        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    fun buildNotification(context: Context): Notification {
        val openIntent = PendingIntent.getActivity(
            context, 0,
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val snoozeIntent = PendingIntent.getBroadcast(
            context, 1,
            Intent(context, NotificationReceiver::class.java).setAction(ACTION_SNOOZE),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dismissIntent = PendingIntent.getBroadcast(
            context, 2,
            Intent(context, NotificationReceiver::class.java).setAction(ACTION_DISMISS),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Time to log!")
            .setContentText("How are you feeling? What have you eaten?")
            .setContentIntent(openIntent)
            .setAutoCancel(true)
            .addAction(0, "Snooze 30 min", snoozeIntent)
            .addAction(0, "Dismiss", dismissIntent)
            .build()
    }

    fun showNotification(context: Context) {
        createChannel(context)
        context.getSystemService(NotificationManager::class.java)
            .notify(NOTIFICATION_ID, buildNotification(context))
    }

    fun cancelNotification(context: Context) {
        context.getSystemService(NotificationManager::class.java)
            .cancel(NOTIFICATION_ID)
    }
}
