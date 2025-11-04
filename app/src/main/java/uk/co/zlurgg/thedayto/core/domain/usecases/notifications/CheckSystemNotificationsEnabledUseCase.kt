package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * Use case to check if notifications are enabled at the system level.
 *
 * This is separate from Android 13+ runtime permission (POST_NOTIFICATIONS).
 * Even with permission granted, users can disable notifications in Android Settings.
 *
 * Use Cases:
 * - Before enabling notifications: Warn if system notifications are disabled
 * - Troubleshooting: Help users understand why notifications aren't appearing
 *
 * Following Clean Architecture:
 * - Single responsibility: Check system notification state
 * - No business logic - just queries system state
 * - Used by ViewModels to make decisions about notification settings
 *
 * @param context Application context for accessing NotificationManager
 */
class CheckSystemNotificationsEnabledUseCase(
    private val context: Context
) {
    /**
     * Check if notifications are enabled for this app in system settings.
     *
     * Checks the master notification toggle in Android Settings > Apps > App > Notifications.
     * This is independent of:
     * - Runtime permission (POST_NOTIFICATIONS for Android 13+)
     * - Notification channels (can be enabled/disabled individually)
     * - Do Not Disturb mode
     *
     * @return true if system notifications are enabled, false otherwise
     */
    operator fun invoke(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as? NotificationManager
            notificationManager?.areNotificationsEnabled() ?: false
        } else {
            // Pre-API 24: Use NotificationManagerCompat fallback
            androidx.core.app.NotificationManagerCompat.from(context)
                .areNotificationsEnabled()
        }
    }
}