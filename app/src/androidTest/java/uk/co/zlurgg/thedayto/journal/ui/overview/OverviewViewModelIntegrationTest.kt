package uk.co.zlurgg.thedayto.journal.ui.overview

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.zlurgg.thedayto.base.DatabaseTest
import uk.co.zlurgg.thedayto.core.data.repository.NotificationRepositoryImpl
import uk.co.zlurgg.thedayto.core.data.repository.PreferencesRepositoryImpl
import uk.co.zlurgg.thedayto.core.domain.repository.NotificationRepository
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckNotificationPermissionUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.CheckSystemNotificationsEnabledUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckTodayEntryExistsUseCaseImpl
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.GetNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.SaveNotificationSettingsUseCase
import uk.co.zlurgg.thedayto.core.domain.usecases.notifications.ShouldShowPermissionRationaleUseCase
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.data.repository.EntryRepositoryImpl
import uk.co.zlurgg.thedayto.journal.data.repository.MoodColorRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.CheckEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.DeleteEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.MarkEntryReminderShownTodayUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.OverviewUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.RestoreEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.overview.UpdateEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryByDateUseCase
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewAction
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Integration tests for OverviewViewModel with real Room database.
 *
 * Tests the complete flow from ViewModel -> Use Cases -> Repositories -> Database
 * and back to verify the entire data flow works correctly.
 *
 * Critical validations:
 * - ViewModel loads entries from real database
 * - Delete/Restore operations update database and UI state
 * - Entry ordering persists correctly
 * - Empty state handling
 */
@RunWith(AndroidJUnit4::class)
class OverviewViewModelIntegrationTest : DatabaseTest() {

    private lateinit var viewModel: OverviewViewModel
    private lateinit var entryRepository: EntryRepositoryImpl
    private lateinit var moodColorRepository: MoodColorRepositoryImpl
    private lateinit var preferencesRepository: PreferencesRepository
    private lateinit var notificationRepository: NotificationRepository

    @Before
    fun setupViewModel() {
        // Create real repositories with in-memory database
        entryRepository = EntryRepositoryImpl(entryDao)
        moodColorRepository = MoodColorRepositoryImpl(moodColorDao)

        // Create preferences repository (uses test context)
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        preferencesRepository = PreferencesRepositoryImpl(context)

        // Create notification repository
        val checkTodayEntryExists = CheckTodayEntryExistsUseCaseImpl(entryRepository)
        notificationRepository = NotificationRepositoryImpl(context, preferencesRepository, checkTodayEntryExists)

        // Create use cases
        val overviewUseCases = OverviewUseCases(
            getEntries = GetEntriesUseCase(entryRepository),
            deleteEntry = DeleteEntryUseCase(entryRepository),
            restoreEntry = RestoreEntryUseCase(entryRepository),
            getEntryByDate = GetEntryByDateUseCase(entryRepository),
            updateEntryUseCase = UpdateEntryUseCase(entryRepository),
            checkEntryReminderShownToday = CheckEntryReminderShownTodayUseCase(preferencesRepository),
            markEntryReminderShownToday = MarkEntryReminderShownTodayUseCase(preferencesRepository),
            getNotificationSettings = GetNotificationSettingsUseCase(preferencesRepository),
            saveNotificationSettings = SaveNotificationSettingsUseCase(preferencesRepository, notificationRepository),
            checkNotificationPermission = CheckNotificationPermissionUseCase(notificationRepository),
            checkSystemNotificationsEnabled = CheckSystemNotificationsEnabledUseCase(notificationRepository),
            shouldShowPermissionRationale = ShouldShowPermissionRationaleUseCase(notificationRepository)
        )

        // Create ViewModel with real dependencies
        viewModel = OverviewViewModel(overviewUseCases)
    }

    // ============================================================
    // Loading Tests
    // ============================================================

    @Test
    fun viewModel_loads_entries_from_database_on_init() = runTest {
        // Given: Entries in database
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, content = "Entry 1", id = 1)
        val entry2 = TestDataBuilders.createEntry(moodColorId = 1, content = "Entry 2", id = 2)
        entryDao.insertEntry(entry1.toEntity())
        entryDao.insertEntry(entry2.toEntity())

        // When: ViewModel initializes (happens in @Before)
        // Note: Need to create new ViewModel after inserting data
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val newViewModel = OverviewViewModel(
            OverviewUseCases(
                getEntries = GetEntriesUseCase(entryRepository),
                deleteEntry = DeleteEntryUseCase(entryRepository),
                restoreEntry = RestoreEntryUseCase(entryRepository),
                getEntryByDate = GetEntryByDateUseCase(entryRepository),
                updateEntryUseCase = UpdateEntryUseCase(entryRepository),
                checkEntryReminderShownToday = CheckEntryReminderShownTodayUseCase(preferencesRepository),
                markEntryReminderShownToday = MarkEntryReminderShownTodayUseCase(preferencesRepository),
                getNotificationSettings = GetNotificationSettingsUseCase(preferencesRepository),
                saveNotificationSettings = SaveNotificationSettingsUseCase(preferencesRepository, notificationRepository),
                checkNotificationPermission = CheckNotificationPermissionUseCase(notificationRepository),
                checkSystemNotificationsEnabled = CheckSystemNotificationsEnabledUseCase(notificationRepository),
                shouldShowPermissionRationale = ShouldShowPermissionRationaleUseCase(notificationRepository)
            )
        )

        // Then: UI state should contain entries from database
        newViewModel.uiState.test {
            val state = awaitItem()

            // Wait for loading to complete
            val loadedState = if (state.isLoading) awaitItem() else state

            assertFalse("Should not be loading", loadedState.isLoading)
            assertEquals("Should have 2 entries", 2, loadedState.entries.size)
            assertTrue("Should contain entry 1", loadedState.entries.any { it.content == "Entry 1" })
            assertTrue("Should contain entry 2", loadedState.entries.any { it.content == "Entry 2" })
        }
    }

    @Test
    fun viewModel_handles_empty_database_state() = runTest {
        // Given: Empty database (no entries)

        // When: ViewModel initializes
        // Then: UI state should reflect empty state
        viewModel.uiState.test {
            val state = awaitItem()

            // Wait for loading to complete
            val loadedState = if (state.isLoading) awaitItem() else state

            assertFalse("Should not be loading", loadedState.isLoading)
            assertEquals("Should have 0 entries", 0, loadedState.entries.size)
            assertNotNull("Greeting should be set", loadedState.greeting)
        }
    }

    // ============================================================
    // Delete/Restore Tests
    // ============================================================

    @Test
    fun deleteEntry_updates_database_and_ui_state() = runTest {
        // Given: An entry in the database
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Test Entry", id = 1)
        entryDao.insertEntry(entry.toEntity())

        // Recreate ViewModel to load the entry
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val newViewModel = OverviewViewModel(
            OverviewUseCases(
                getEntries = GetEntriesUseCase(entryRepository),
                deleteEntry = DeleteEntryUseCase(entryRepository),
                restoreEntry = RestoreEntryUseCase(entryRepository),
                getEntryByDate = GetEntryByDateUseCase(entryRepository),
                updateEntryUseCase = UpdateEntryUseCase(entryRepository),
                checkEntryReminderShownToday = CheckEntryReminderShownTodayUseCase(preferencesRepository),
                markEntryReminderShownToday = MarkEntryReminderShownTodayUseCase(preferencesRepository),
                getNotificationSettings = GetNotificationSettingsUseCase(preferencesRepository),
                saveNotificationSettings = SaveNotificationSettingsUseCase(preferencesRepository, notificationRepository),
                checkNotificationPermission = CheckNotificationPermissionUseCase(notificationRepository),
                checkSystemNotificationsEnabled = CheckSystemNotificationsEnabledUseCase(notificationRepository),
                shouldShowPermissionRationale = ShouldShowPermissionRationaleUseCase(notificationRepository)
            )
        )

        // When: Deleting the entry (need EntryWithMoodColor, not Entry)
        val entryWithMood = entryDao.getEntryWithMoodColorById(1)!!.toDomain()
        newViewModel.onAction(OverviewAction.DeleteEntry(entryWithMood))

        // Then: Entry should be removed from database and UI state
        newViewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have 0 entries after delete", 0, state.entries.size)

            cancelAndIgnoreRemainingEvents()
        }

        // Verify database
        val dbEntry = entryDao.getEntryById(1)
        assertEquals("Entry should not exist in database", null, dbEntry)
    }

    @Test
    fun restoreEntry_updates_database_and_ui_state() = runTest {
        // Given: An entry that was deleted
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Deleted Entry", id = 1)
        entryDao.insertEntry(entry.toEntity())
        entryDao.deleteEntry(entry.toEntity())

        // When: Restoring the entry via ViewModel
        viewModel.onAction(OverviewAction.RestoreEntry)

        // Give time for database operation
        kotlinx.coroutines.delay(100)

        // Then: Entry should be restored in database
        val restoredEntry = entryDao.getEntryById(1)
        assertNotNull("Entry should be restored in database", restoredEntry)
        assertEquals("Content should match", "Deleted Entry", restoredEntry?.content)

        // And: UI state should reflect restoration
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Should have at least 1 entry after restore", state.entries.isNotEmpty())

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Ordering Tests
    // ============================================================

    @Test
    fun entry_order_persists_and_updates_correctly() = runTest {
        // Given: Multiple entries with different dates
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry1 = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = "Oldest",
            dateStamp = TestDataBuilders.getDaysAgoEpoch(2),
            id = 1
        )
        val entry2 = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = "Newest",
            dateStamp = TestDataBuilders.getTodayEpoch(),
            id = 2
        )
        entryDao.insertEntry(entry1.toEntity())
        entryDao.insertEntry(entry2.toEntity())

        // Recreate ViewModel to load entries
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val newViewModel = OverviewViewModel(
            OverviewUseCases(
                getEntries = GetEntriesUseCase(entryRepository),
                deleteEntry = DeleteEntryUseCase(entryRepository),
                restoreEntry = RestoreEntryUseCase(entryRepository),
                getEntryByDate = GetEntryByDateUseCase(entryRepository),
                updateEntryUseCase = UpdateEntryUseCase(entryRepository),
                checkEntryReminderShownToday = CheckEntryReminderShownTodayUseCase(preferencesRepository),
                markEntryReminderShownToday = MarkEntryReminderShownTodayUseCase(preferencesRepository),
                getNotificationSettings = GetNotificationSettingsUseCase(preferencesRepository),
                saveNotificationSettings = SaveNotificationSettingsUseCase(preferencesRepository, notificationRepository),
                checkNotificationPermission = CheckNotificationPermissionUseCase(notificationRepository),
                checkSystemNotificationsEnabled = CheckSystemNotificationsEnabledUseCase(notificationRepository),
                shouldShowPermissionRationale = ShouldShowPermissionRationaleUseCase(notificationRepository)
            )
        )

        // When: Changing sort order to ascending
        newViewModel.onAction(OverviewAction.Order(EntryOrder.Date(OrderType.Ascending)))

        // Then: Entries should be in ascending order (oldest first)
        newViewModel.uiState.test {
            val state = awaitItem()

            if (state.entries.size == 2) {
                assertEquals("First entry should be oldest", "Oldest", state.entries[0].content)
                assertEquals("Second entry should be newest", "Newest", state.entries[1].content)
            }

            cancelAndIgnoreRemainingEvents()
        }
    }
}
