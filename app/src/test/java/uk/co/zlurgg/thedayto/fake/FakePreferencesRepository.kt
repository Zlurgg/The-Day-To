package uk.co.zlurgg.thedayto.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import uk.co.zlurgg.thedayto.core.domain.model.ThemeMode
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
    private var editorTutorialSeen: Boolean = false
    private var syncEnabled: Boolean = false
    private var lastSyncTimestamp: Long? = null
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)

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

    // ==================== Cloud Sync ====================

    override suspend fun isSyncEnabled(): Boolean {
        return syncEnabled
    }

    override suspend fun setSyncEnabled(enabled: Boolean) {
        syncEnabled = enabled
    }

    override suspend fun getLastSyncTimestamp(): Long? {
        return lastSyncTimestamp
    }

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        lastSyncTimestamp = timestamp
    }

    // ==================== Theme ====================

    override fun observeThemeMode(): Flow<ThemeMode> = _themeMode

    override suspend fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
    }

    override suspend fun clear() {
        entryReminderDate = null
        isFirstLaunch = true
        welcomeDialogSeen = false
        editorTutorialSeen = false
        syncEnabled = false
        lastSyncTimestamp = null
        _themeMode.value = ThemeMode.SYSTEM
    }

    /**
     * Helper method to set editor tutorial seen state for testing.
     */
    fun setEditorTutorialSeen(seen: Boolean) {
        editorTutorialSeen = seen
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
        syncEnabled = false
        lastSyncTimestamp = null
        _themeMode.value = ThemeMode.SYSTEM
    }
}
