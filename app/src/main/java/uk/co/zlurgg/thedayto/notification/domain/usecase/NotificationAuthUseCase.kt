package uk.co.zlurgg.thedayto.notification.domain.usecase

import timber.log.Timber
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository
import uk.co.zlurgg.thedayto.notification.domain.scheduler.NotificationScheduler

/**
 * Result of sign-in notification settings handling.
 *
 * Allows UI to provide appropriate feedback based on what happened.
 */
sealed interface SignInNotificationResult {
    /** Anonymous settings were migrated to the signed-in user */
    data object MigratedAnonymous : SignInNotificationResult

    /** No anonymous settings existed, user starts fresh */
    data object NoSettingsFound : SignInNotificationResult
}

/**
 * Use case for handling notification settings during auth transitions.
 *
 * On sign-in:
 * - Migrates anonymous settings to the signed-in user if they exist
 * - Reschedules notifications for the new user context
 *
 * On sign-out:
 * - Cancels any scheduled notifications
 * - Deletes the user's notification settings
 *
 * Note: Remote sync (Firestore) will be added in Phase 4. Currently handles
 * local settings migration only.
 */
class NotificationAuthUseCase(
    private val settingsRepository: NotificationSettingsRepository,
    private val notificationScheduler: NotificationScheduler
) {

    /**
     * Handles notification settings on successful sign-in.
     *
     * Migrates anonymous settings to the signed-in user if they exist.
     * In Phase 4, this will also attempt to download remote settings first.
     *
     * @param userId The Firebase UID of the signed-in user
     * @return Result indicating what happened with the settings
     */
    suspend fun handleSignInSuccess(userId: String): SignInNotificationResult {
        Timber.d("Handling sign-in notification settings for user: %s", userId)

        // Check for existing anonymous settings to migrate
        val anonymousSettings = settingsRepository.getSettings(ANONYMOUS_USER_ID)

        return if (anonymousSettings != null) {
            // Migrate anonymous settings to signed-in user
            Timber.d(
                "Migrating anonymous settings to user %s: enabled=%b, hour=%d, minute=%d",
                userId,
                anonymousSettings.enabled,
                anonymousSettings.hour,
                anonymousSettings.minute
            )

            settingsRepository.saveSettings(userId, anonymousSettings)
            settingsRepository.deleteSettings(ANONYMOUS_USER_ID)

            // Reschedule notifications for new user context if enabled
            if (anonymousSettings.enabled) {
                notificationScheduler.updateNotificationTime(
                    anonymousSettings.hour,
                    anonymousSettings.minute
                )
            }

            SignInNotificationResult.MigratedAnonymous
        } else {
            Timber.d("No anonymous settings found for user %s", userId)
            SignInNotificationResult.NoSettingsFound
        }
    }

    /**
     * Handles notification settings cleanup on sign-out.
     *
     * Cancels scheduled notifications and deletes the user's settings.
     * User can reconfigure notifications after signing in again.
     *
     * @param userId The Firebase UID of the signing-out user
     */
    suspend fun handleSignOut(userId: String) {
        Timber.d("Handling sign-out notification cleanup for user: %s", userId)

        // Cancel any scheduled notifications
        notificationScheduler.cancelNotifications()

        // Delete user's notification settings
        settingsRepository.deleteSettings(userId)

        Timber.d("Sign-out notification cleanup complete for user: %s", userId)
    }
}
