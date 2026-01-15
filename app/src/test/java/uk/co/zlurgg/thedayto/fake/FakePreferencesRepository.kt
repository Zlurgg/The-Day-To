package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import io.github.zlurgg.update.domain.repository.UpdatePreferencesRepository
import java.time.LocalDate

/**
 * Fake implementation of PreferencesRepository for testing.
 * Stores data in memory instead of SharedPreferences.
 *
 * Also implements UpdatePreferencesRepository to match the real implementation
 * and allow testing update-related use cases.
 */
class FakePreferencesRepository : PreferencesRepository, UpdatePreferencesRepository {

    // In-memory storage for testing
    private var entryReminderDate: LocalDate? = null
    private var isFirstLaunch: Boolean = true
    private var welcomeDialogSeen: Boolean = false
    private var editorTutorialSeen: Boolean = false
    private var notificationEnabled: Boolean = false
    private var notificationHour: Int = 9
    private var notificationMinute: Int = 0
    private var dismissedVersion: String? = null

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

    override suspend fun hasSeenEditorTutorial(): Boolean {
        return editorTutorialSeen
    }

    override suspend fun markEditorTutorialSeen() {
        editorTutorialSeen = true
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

    override suspend fun getDismissedVersion(): String? {
        return dismissedVersion
    }

    override suspend fun setDismissedVersion(version: String) {
        dismissedVersion = version
    }

    /**
     * Helper method to set editor tutorial seen state for testing.
     */
    fun setEditorTutorialSeen(seen: Boolean) {
        editorTutorialSeen = seen
    }

    /**
     * Helper method to set dismissed version for testing.
     */
    fun setDismissedVersionForTest(version: String?) {
        dismissedVersion = version
    }

    /**
     * Helper method to reset all values to defaults.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        entryReminderDate = null
        isFirstLaunch = true
        welcomeDialogSeen = false
        editorTutorialSeen = false
        notificationEnabled = false
        notificationHour = 9
        notificationMinute = 0
        dismissedVersion = null
    }
}
