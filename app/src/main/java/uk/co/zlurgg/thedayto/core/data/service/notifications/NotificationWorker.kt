package uk.co.zlurgg.thedayto.core.data.service.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.Worker
import androidx.work.WorkerParameters
import timber.log.Timber
import uk.co.zlurgg.thedayto.MainActivity
import uk.co.zlurgg.thedayto.R

class NotificationWorker(context: Context, params: WorkerParameters) : Worker(context, params) {
    override fun doWork(): Result {
        // Check if notification permission is granted (Android 13+)
        if (!hasNotificationPermission()) {
            Timber.w("Notification permission not granted - skipping notification")
            return Result.success()  // Not a failure, just can't show notification
        }

        val id = inputData.getLong(NOTIFICATION_ID, 0).toInt()
        createNotification(id)
        return Result.success()
    }

    /**
     * Check if app has notification permission.
     * On API < 33, always returns true (no runtime permission required).
     */
    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
        } else {
            true  // Pre-Android 13: no runtime permission needed
        }
    }

    private fun createNotification(id: Int) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(NOTIFICATION_ID, id)

        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_MUTABLE)
        } else {
            PendingIntent.getActivity(
                applicationContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // Use pendingIntent for content intent (fallback if deep link fails)
        val deepLinkPendingIntent = createPendingIntent(
            deepLink = "https://thedayto.co.uk/editor?entryId=null&showBackButton=true",
            context = applicationContext
        )

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL)
            .setContentTitle(applicationContext.getString(R.string.notification_title))
            .setContentText(applicationContext.getString(R.string.notification_content_text))
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(deepLinkPendingIntent ?: pendingIntent) // Use deep link or fallback
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)

        val notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notification.setChannelId(NOTIFICATION_CHANNEL)

        val ringtoneManager = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val audioAttributes =
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()

        val channel =
            NotificationChannel(
                NOTIFICATION_CHANNEL,
                NOTIFICATION_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )

        channel.enableLights(true)
        channel.lightColor = Color.GREEN
        channel.enableVibration(true)
        channel.vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
        channel.setSound(ringtoneManager, audioAttributes)
        notificationManager.createNotificationChannel(channel)

        notificationManager.notify(id, notification.build())
    }

    /** allows for deep linking to go from notification to screen in app **/
    private fun createPendingIntent(deepLink: String, context: Context): PendingIntent? {
        val startActivityIntent = Intent(
            Intent.ACTION_VIEW, deepLink.toUri(),
            context, MainActivity::class.java
        )
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(startActivityIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }

        if (resultPendingIntent == null) {
            Timber.e("Failed to create PendingIntent for deep link: $deepLink")
        }

        return resultPendingIntent
    }

    companion object {
        const val NOTIFICATION_ID = "thedayto_notification_id"
        const val NOTIFICATION_NAME = "thedayto"
        const val NOTIFICATION_CHANNEL = "thedayto_channel_01"
        const val NOTIFICATION_WORK = "thedayto_notification_work"
    }
}
