package uk.co.zlurgg.thedayto

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.Data
import com.google.android.gms.auth.api.identity.Identity
import uk.co.zlurgg.thedayto.core.notifications.NotificationWorker.Companion.NOTIFICATION_ID
import uk.co.zlurgg.thedayto.core.notifications.Notifications.scheduleNotification
import uk.co.zlurgg.thedayto.core.presentation.TheDayToApp
import uk.co.zlurgg.thedayto.feature_sign_in.presentation.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.ui.theme.TheDayToTheme

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
            scheduleNotification(data, this)
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
}

