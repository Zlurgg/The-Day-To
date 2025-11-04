package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import java.time.LocalDate

/**
 * Fake implementation of PreferencesRepository for testing.
 * Stores data in memory instead of SharedPreferences.
 */
class FakePreferencesRepository : PreferencesRepository {

    // In-memory storage for testing
    private var entryReminderDate: LocalDate? = null
    private var isFirstLaunch: Boolean = true
    private var welcomeDialogSeen: Boolean = false
    private var notificationEnabled: Boolean = false
    private var notificationHour: Int = 9
    private var notificationMinute: Int = 0

    override suspend fun hasShownEntryReminderToday(): Boolean {
        return entryReminderDate == LocalDate.now()
    }

    override suspend fun markEntryReminderShownToday() {
        entryReminderDate = LocalDate.now()
    }

    override suspend fun isFirstLaunch(): Boolean {
        return isFirstLaunch
    }

    override suspend fun markFirstLaunchComplete() {
        isFirstLaunch = false
    }

    override suspend fun hasSeenWelcomeDialog(): Boolean {
        return welcomeDialogSeen
    }

    override suspend fun markWelcomeDialogSeen() {
        welcomeDialogSeen = true
    }

    override suspend fun isNotificationEnabled(): Boolean {
        return notificationEnabled
    }

    override suspend fun setNotificationEnabled(enabled: Boolean) {
        notificationEnabled = enabled
    }

    override suspend fun getNotificationHour(): Int {
        return notificationHour
    }

    override suspend fun getNotificationMinute(): Int {
        return notificationMinute
    }

    override suspend fun setNotificationTime(hour: Int, minute: Int) {
        notificationHour = hour
        notificationMinute = minute
    }

    /**
     * Helper method to reset all values to defaults.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        entryReminderDate = null
        isFirstLaunch = true
        welcomeDialogSeen = false
        notificationEnabled = false
        notificationHour = 9
        notificationMinute = 0
    }
}
