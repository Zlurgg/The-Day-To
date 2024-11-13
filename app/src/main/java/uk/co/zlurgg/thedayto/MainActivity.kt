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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handle notification permission
        handleNotificationPermission { isGranted ->
            if (isGranted) {
                val data = Data.Builder().putInt(NOTIFICATION_ID, 0).build()
                scheduleNotification(data, this)
            } else {
                TODO()// Handle permission denial (e.g., show a message)
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

    private fun handleNotificationPermission(onPermissionResult: (Boolean) -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            )

            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                onPermissionResult(true) // Permission already granted
            } else {
                // Request permission
                registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                    onPermissionResult(isGranted)
                }.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            onPermissionResult(true) // Permission not required on older versions
        }
    }
}

