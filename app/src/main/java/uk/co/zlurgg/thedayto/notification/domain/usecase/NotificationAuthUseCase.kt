package uk.co.zlurgg.thedayto.notification.domain.usecase

import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.repository.NotificationSettingsRepository
import uk.co.zlurgg.thedayto.notification.domain.scheduler.NotificationScheduler
import uk.co.zlurgg.thedayto.notification.domain.sync.NotificationSyncService

/**
 * Result of sign-in notification settings handling.
 *
 * Allows UI to provide appropriate feedback based on what happened.
 */
sealed interface SignInNotificationResult {
    /** Settings were restored from the user's account (Firestore) */
    data object RestoredFromAccount : SignInNotificationResult

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
    private val notificationScheduler: NotificationScheduler,
    private val syncService: NotificationSyncService,
) {

    /**
     * Handles notification settings on successful sign-in.
     *
     * Priority:
     * 1. Download settings from Firestore (account settings take precedence)
     * 2. If no remote settings, adopt anonymous settings if they exist
     * 3. Otherwise, user starts fresh
     *
     * @param userId The Firebase UID of the signed-in user
     * @return Result indicating what happened with the settings
     */
    suspend fun handleSignInSuccess(userId: String): SignInNotificationResult {
        Timber.d("Handling sign-in notification settings for user: %s", userId)

        // First, try to download settings from Firestore
        val downloadResult = syncService.download(userId)
        if (downloadResult is Result.Success && downloadResult.data != null) {
            val remoteSettings = downloadResult.data
            Timber.d(
                "Restored settings from account: enabled=%b, hour=%d, minute=%d",
                remoteSettings.enabled,
                remoteSettings.hour,
                remoteSettings.minute,
            )

            // Delete any anonymous settings (account takes precedence)
            when (val deleteResult = settingsRepository.deleteSettings(ANONYMOUS_USER_ID)) {
                is Result.Error -> Timber.w("Failed to delete anonymous settings: %s", deleteResult.error)
                is Result.Success -> Unit // success
            }

            // Reschedule notifications if enabled
            if (remoteSettings.enabled) {
                notificationScheduler.updateNotificationTime(
                    remoteSettings.hour,
                    remoteSettings.minute,
                )
            }

            return SignInNotificationResult.RestoredFromAccount
        }

        // No remote settings - check for anonymous settings to adopt
        val anonymousSettings = when (val anonymousSettingsResult = settingsRepository.getSettings(ANONYMOUS_USER_ID)) {
            is Result.Success -> anonymousSettingsResult.data
            is Result.Error -> {
                Timber.w("Failed to get anonymous settings: %s", anonymousSettingsResult.error)
                null
            }
        }

        return if (anonymousSettings != null) {
            Timber.d(
                "Adopting anonymous settings to user %s: enabled=%b, hour=%d, minute=%d",
                userId,
                anonymousSettings.enabled,
                anonymousSettings.hour,
                anonymousSettings.minute,
            )

            when (val saveResult = settingsRepository.saveSettings(userId, anonymousSettings)) {
                is Result.Error -> Timber.w("Failed to save settings for user: %s", saveResult.error)
                is Result.Success -> Unit // success
            }
            when (val deleteResult = settingsRepository.deleteSettings(ANONYMOUS_USER_ID)) {
                is Result.Error -> Timber.w("Failed to delete anonymous settings: %s", deleteResult.error)
                is Result.Success -> Unit // success
            }

            // Reschedule notifications for new user context if enabled
            if (anonymousSettings.enabled) {
                notificationScheduler.updateNotificationTime(
                    anonymousSettings.hour,
                    anonymousSettings.minute,
                )
            }

            SignInNotificationResult.MigratedAnonymous
        } else {
            Timber.d("No settings found for user %s", userId)
            SignInNotificationResult.NoSettingsFound
        }
    }

    /**
     * Handles notification settings on sign-out.
     *
     * Copies account settings to anonymous so notifications continue after sign-out.
     * Then deletes account settings for privacy.
     *
     * @param userId The Firebase UID of the signing-out user
     */
    suspend fun handleSignOut(userId: String) {
        Timber.d("Handling sign-out notification settings for user: %s", userId)

        // Get account settings before deleting
        val accountSettings = when (val accountSettingsResult = settingsRepository.getSettings(userId)) {
            is Result.Success -> accountSettingsResult.data
            is Result.Error -> {
                Timber.w("Failed to get account settings: %s", accountSettingsResult.error)
                null
            }
        }

        // Delete account settings (privacy - don't keep user data locally)
        when (val deleteResult = settingsRepository.deleteSettings(userId)) {
            is Result.Error -> Timber.w("Failed to delete account settings: %s", deleteResult.error)
            is Result.Success -> Unit // success
        }

        // Copy to anonymous so notifications continue seamlessly
        if (accountSettings != null) {
            Timber.d(
                "Copying account settings to anonymous: enabled=%b, hour=%d, minute=%d",
                accountSettings.enabled,
                accountSettings.hour,
                accountSettings.minute,
            )
            when (val saveResult = settingsRepository.saveSettings(ANONYMOUS_USER_ID, accountSettings)) {
                is Result.Error -> Timber.w("Failed to save anonymous settings: %s", saveResult.error)
                is Result.Success -> Unit // success
            }

            if (accountSettings.enabled) {
                notificationScheduler.updateNotificationTime(
                    accountSettings.hour,
                    accountSettings.minute,
                )
            } else {
                notificationScheduler.cancelNotifications()
            }
        } else {
            Timber.d("No account settings to copy for user: %s", userId)
            notificationScheduler.cancelNotifications()
        }

        Timber.d("Sign-out notification handling complete for user: %s", userId)
    }
}
