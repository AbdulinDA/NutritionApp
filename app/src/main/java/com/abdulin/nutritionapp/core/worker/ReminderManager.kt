package com.abdulin.nutritionapp.core.worker

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReminderManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleAllReminders() {
        scheduleWaterReminder()
    }

    private fun scheduleWaterReminder() {
        val waterReminderRequest = PeriodicWorkRequestBuilder<WaterReminderWorker>(
            3, TimeUnit.HOURS // Проверяем каждые 3 часа
        )
        .setConstraints(Constraints.NONE)
        .build()

        workManager.enqueueUniquePeriodicWork(
            "water_reminder_periodic",
            ExistingPeriodicWorkPolicy.KEEP,
            waterReminderRequest
        )
    }

    fun cancelAllReminders() {
        workManager.cancelAllWork()
    }
}
