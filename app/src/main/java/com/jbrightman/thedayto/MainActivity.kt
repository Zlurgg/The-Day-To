package com.jbrightman.thedayto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.android.gms.auth.api.identity.Identity
import com.jbrightman.thedayto.core.notifications.NotificationWorker
import com.jbrightman.thedayto.core.notifications.NotificationWorker.Companion.NOTIFICATION_ID
import com.jbrightman.thedayto.domain.repository.TheDayToPrefRepository
import com.jbrightman.thedayto.feature_sign_in.presentation.GoogleAuthUiClient
import com.jbrightman.thedayto.presentation.TheDayToApp
import com.jbrightman.thedayto.ui.theme.TheDayToTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    /** Firebase auth for google sign in **/
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    /** notification permissions **/
    private lateinit var checkNotificationPermission: ActivityResultLauncher<String>
    private var isPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkNotificationPermission = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            isPermission = isGranted
        }
        checkPermission()
        if (isPermission) {
            val data = Data.Builder().putInt(NOTIFICATION_ID, 0).build()
            scheduleNotification(data)

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        setContent {
            TheDayToTheme {
                TheDayToApp(
                    googleAuthUiClient
                )
            }
        }
    }

    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                isPermission = true
            } else {
                isPermission = false

                checkNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            isPermission = true
        }
    }

    private fun scheduleNotification(data: Data) {

        val theDayToPrefRepository = TheDayToPrefRepository(applicationContext)

        var userNotificationTime = theDayToPrefRepository.getDailyEntryDate() ?: LocalDateTime.now().toEpochSecond(ZoneOffset.UTC)
        val delay = (LocalDateTime.now().plusDays(1).toEpochSecond(ZoneOffset.UTC)) - userNotificationTime
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val notificationWorker = OneTimeWorkRequest.Builder(NotificationWorker::class.java)
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.SECONDS)
            .setConstraints(constraints)
            .build()

        val instanceWorkManager = WorkManager.getInstance(this)
        instanceWorkManager.beginUniqueWork(
            NotificationWorker.NOTIFICATION_WORK,
            ExistingWorkPolicy.REPLACE, notificationWorker).enqueue()
    }
}

