package uk.co.zlurgg.thedayto.core.data.notifications

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepositoryImpl
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.TimeUnit

object Notifications {
    fun scheduleNotification(data: Data, context: Context) {
        val preferencesRepositoryImpl = PreferencesRepositoryImpl(context)
        /** if the entry date is from yesterday then we create a notification (0 check to account for 1st time user **/
        if (preferencesRepositoryImpl.getDailyEntryDate() == LocalDate.now().atStartOfDay()
                .minusDays(1).toEpochSecond(ZoneOffset.UTC)
            || preferencesRepositoryImpl.getDailyEntryDate() == 0L
        ) {
            val userNotificationTime = LocalDateTime.now().plusDays(1).toEpochSecond(
                ZoneOffset.UTC
            )
            val delay = userNotificationTime - (LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
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
                ExistingWorkPolicy.REPLACE, notificationWorker
            ).enqueue()
        }


    }
}