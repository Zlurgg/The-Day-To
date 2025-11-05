package uk.co.zlurgg.thedayto.core.util

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.core.net.toUri
import timber.log.Timber

/**
 * Utility functions for navigating to Android system settings.
 * These functions open various system settings screens for the user.
 */
object AndroidSystemUtils {

    /**
     * Opens the system notification settings screen for this app.
     *
     * Guides user to Android Settings > Apps > App > Notifications
     * where they can enable/disable notifications at the system level.
     *
     * @param context The Android context
     */
    fun openSystemNotificationSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open system notification settings")
        }
    }

    /**
     * Opens the app settings screen for this app.
     *
     * Guides user to Android Settings > Apps > App
     * where they can manage app permissions including notifications.
     *
     * @param context The Android context
     */
    fun openAppSettings(context: Context) {
        val intent = Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = "package:${context.packageName}".toUri()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Timber.e(e, "Failed to open app settings")
        }
    }
}
