package com.jbrightman.thedayto.core.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

object Notifications {
    fun scheduleNotification(data: Data, context: Context) {
        val userNotificationTime = LocalDate.now().plusDays(1).atTime(9,0).toEpochSecond(
            ZoneOffset.UTC)
        val delay = userNotificationTime - (LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC))
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val notificationWorker = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        val instanceWorkManager = WorkManager.getInstance(context)
        instanceWorkManager.beginUniqueWork(
            NotificationWorker.NOTIFICATION_WORK,
            ExistingWorkPolicy.REPLACE, notificationWorker).enqueue()

    }
}