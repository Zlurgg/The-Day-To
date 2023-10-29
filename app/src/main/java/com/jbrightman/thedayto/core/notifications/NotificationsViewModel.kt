package com.jbrightman.thedayto.core.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDeepLinkBuilder
import androidx.navigation.Navigation
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.jbrightman.thedayto.MainActivity
import com.jbrightman.thedayto.R
import com.jbrightman.thedayto.presentation.util.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.internal.UTC
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val NOTIFICATION_CHANNEL_ID = "Channel ID"
private const val NOTIFICATION_CHANNEL_NAME = "Channel Name"
private const val NOTIFICATION_ID = 0

@HiltViewModel
class NotificationsViewModel@Inject constructor(
    @ApplicationContext context: Context
) : ViewModel() {
    init {
        createNotificationChannel(context)
        createNotification(context)
    }

    private fun setNotificationTimeViaWorkManager(
        context: Context
    ) {
        val workManager = WorkManager.getInstance(context)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

//        val notificationWorker = OneTimeWorkRequestBuilder<NotificationWorker>()
//            .setInitialDelay(10, TimeUnit.SECONDS)
//            .setConstraints(constraints)
//            .build()

//        workManager.enqueue(notificationWorker)
    }

    private fun createPendingIntent(deepLink: String, context: Context): PendingIntent {
        val startActivityIntent = Intent(Intent.ACTION_VIEW, deepLink.toUri(),
            context,MainActivity::class.java)
        val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
            addNextIntentWithParentStack(startActivityIntent)
            getPendingIntent(0, PendingIntent.FLAG_IMMUTABLE)
        }
        return resultPendingIntent!!
    }

    private fun createNotification(
        context: Context
    ) {
        val pending = createPendingIntent(
            deepLink = "https://thedayto.co.uk/sign-in",
            context = context
        )

        val notification = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(context.getString(R.string.notification_content_text))
            .setSmallIcon(R.drawable.ic_notification_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pending)
            .build()

        val notificationManager = NotificationManagerCompat.from(context)
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel(
        context: Context
    ) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_name)
            val descriptionText = context.getString(R.string.notification_channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system.
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}