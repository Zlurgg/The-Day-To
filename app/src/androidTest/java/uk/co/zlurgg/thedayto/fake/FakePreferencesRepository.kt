package uk.co.zlurgg.thedayto.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import uk.co.zlurgg.thedayto.core.domain.model.ThemeMode
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import java.time.LocalDate

/**
 * Fake implementation of PreferencesRepository for instrumented testing.
 */
class FakePreferencesRepository : PreferencesRepository {

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

    override suspend fun isFirstLaunch(): Boolean = isFirstLaunch

    override suspend fun markFirstLaunchComplete() {
        isFirstLaunch = false
    }

    override suspend fun hasSeenWelcomeDialog(): Boolean = welcomeDialogSeen

    override suspend fun markWelcomeDialogSeen() {
        welcomeDialogSeen = true
    }

    override suspend fun hasSeenEditorTutorial(): Boolean = editorTutorialSeen

    override suspend fun markEditorTutorialSeen() {
        editorTutorialSeen = true
    }

    override suspend fun isSyncEnabled(): Boolean = syncEnabled

    override suspend fun setSyncEnabled(enabled: Boolean) {
        syncEnabled = enabled
    }

    override suspend fun getLastSyncTimestamp(): Long? = lastSyncTimestamp

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
}
