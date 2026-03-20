package com.goi.foodtracker.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.goi.foodtracker.data.db.entities.NotificationSchedule
import com.goi.foodtracker.data.db.entities.ScheduleType
import com.goi.foodtracker.data.repository.NotificationRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationRepository: NotificationRepository
) {
    private val alarmManager = context.getSystemService(AlarmManager::class.java)
    private val workManager = WorkManager.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO)

    fun rescheduleAll() {
        scope.launch {
            cancelAll()
            NotificationHelper.createChannel(context)
            val enabled = notificationRepository.getEnabledSchedules()
            enabled.forEach { schedule ->
                when (schedule.type) {
                    ScheduleType.FIXED -> scheduleFixed(schedule)
                    ScheduleType.INTERVAL -> scheduleInterval(schedule)
                }
            }
        }
    }

    private fun cancelAll() {
        // Cancel all fixed alarms by iterating request codes 0..99
        for (i in 0..99) {
            val intent = Intent(context, NotificationReceiver::class.java)
                .setAction(NotificationHelper.ACTION_SHOW)
            val pi = PendingIntent.getBroadcast(
                context, FIXED_BASE_REQUEST_CODE + i, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { alarmManager.cancel(it) }
        }
        workManager.cancelUniqueWork(INTERVAL_WORK_NAME)
    }

    private fun scheduleFixed(schedule: NotificationSchedule) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, schedule.hour)
            set(Calendar.MINUTE, schedule.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            FIXED_BASE_REQUEST_CODE + schedule.id.toInt(),
            Intent(context, NotificationReceiver::class.java).setAction(NotificationHelper.ACTION_SHOW),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // setRepeating fires ~daily; acceptable for reminder use case on API 29
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun scheduleInterval(schedule: NotificationSchedule) {
        val intervalMinutes = schedule.intervalMinutes.toLong().coerceAtLeast(15)
        val request = PeriodicWorkRequestBuilder<ReminderWorker>(
            intervalMinutes, TimeUnit.MINUTES,
            (intervalMinutes / 4).coerceAtLeast(5), TimeUnit.MINUTES // flex interval
        ).build()

        workManager.enqueueUniquePeriodicWork(
            INTERVAL_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    companion object {
        private const val FIXED_BASE_REQUEST_CODE = 100
        private const val INTERVAL_WORK_NAME = "food_tracker_interval_reminder"
    }
}
