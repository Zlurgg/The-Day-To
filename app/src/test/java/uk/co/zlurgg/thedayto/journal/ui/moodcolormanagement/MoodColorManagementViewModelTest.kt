package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement.MoodColorManagementUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetMoodColorEntryCountsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.AddMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.UpdateMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorOrder
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementAction
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementUiEvent
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for MoodColorManagementViewModel.
 *
 * Tests cover:
 * - Initialization (load mood colors with entry counts)
 * - Sorting (Date/Mood, Ascending/Descending)
 * - Delete with undo functionality
 * - Add new mood color
 * - Edit mood color
 * - Error handling
 *
 * Following Google's best practices: ViewModels tested with fake repositories.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MoodColorManagementViewModelTest {

    private lateinit var viewModel: MoodColorManagementViewModel
    private lateinit var fakeEntryRepository: FakeEntryRepository
    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeMoodColorRepository = FakeMoodColorRepository()
        fakeEntryRepository = FakeEntryRepository(fakeMoodColorRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): MoodColorManagementViewModel {
        val useCases = MoodColorManagementUseCases(
            getMoodColors = GetMoodColorsUseCase(fakeMoodColorRepository),
            addMoodColor = AddMoodColorUseCase(fakeMoodColorRepository),
            updateMoodColor = UpdateMoodColorUseCase(fakeMoodColorRepository),
            deleteMoodColor = DeleteMoodColorUseCase(fakeMoodColorRepository),
            getMoodColorEntryCounts = GetMoodColorEntryCountsUseCase(fakeEntryRepository)
        )
        return MoodColorManagementViewModel(useCases)
    }

    // ============================================================
    // Initialization Tests
    // ============================================================

    @Test
    fun `init - loads mood colors with entry counts`() = runTest {
        // Given: Mood colors and entries exist
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1, dateStamp = 1000L)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2, dateStamp = 2000L)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)

        // Add entries: 3 for Happy, 1 for Sad
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, id = 1))
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, id = 2))
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, id = 3))
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 2, id = 4))

        // When: Creating ViewModel
        viewModel = createViewModel()

        // Then: Should load mood colors with correct counts
        val state = viewModel.uiState.value
        assertEquals("Should have 2 mood colors", 2, state.moodColorsWithCount.size)

        val happyMood = state.moodColorsWithCount.find { it.moodColor.mood == "Happy" }
        val sadMood = state.moodColorsWithCount.find { it.moodColor.mood == "Sad" }

        assertNotNull("Happy mood should exist", happyMood)
        assertNotNull("Sad mood should exist", sadMood)
        assertEquals("Happy should have 3 entries", 3, happyMood?.entryCount)
        assertEquals("Sad should have 1 entry", 1, sadMood?.entryCount)
    }

    @Test
    fun `init - shows empty list when no mood colors exist`() = runTest {
        // Given: No mood colors

        // When: Creating ViewModel
        viewModel = createViewModel()

        // Then: Should have empty list
        val state = viewModel.uiState.value
        assertTrue("Should be empty", state.moodColorsWithCount.isEmpty())
        assertFalse("Should not be loading", state.isLoading)
    }

    @Test
    fun `init - default sort order is Date Descending`() = runTest {
        // Given: Nothing special

        // When: Creating ViewModel
        viewModel = createViewModel()

        // Then: Default sort order should be Date Descending
        val state = viewModel.uiState.value
        assertTrue("Should be Date order", state.sortOrder is MoodColorOrder.Date)
        assertTrue("Should be Descending", state.sortOrder.orderType is OrderType.Descending)
    }

    // ============================================================
    // Sorting Tests
    // ============================================================

    @Test
    fun `toggleSortOrder - changes to Mood Ascending`() = runTest {
        // Given: ViewModel with mood colors
        val mood1 = TestDataBuilders.createMoodColor(mood = "Zebra", id = 1, dateStamp = 1000L)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Apple", id = 2, dateStamp = 2000L)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)
        viewModel = createViewModel()

        // When: Changing to Mood Ascending
        viewModel.onAction(MoodColorManagementAction.ToggleSortOrder(MoodColorOrder.Mood(OrderType.Ascending)))

        // Then: Sort order should change and list should be sorted A-Z
        val state = viewModel.uiState.value
        assertTrue("Should be Mood order", state.sortOrder is MoodColorOrder.Mood)
        assertTrue("Should be Ascending", state.sortOrder.orderType is OrderType.Ascending)
        assertEquals("First should be Apple", "Apple", state.moodColorsWithCount[0].moodColor.mood)
        assertEquals("Second should be Zebra", "Zebra", state.moodColorsWithCount[1].moodColor.mood)
    }

    @Test
    fun `toggleSortOrder - changes to Date Ascending`() = runTest {
        // Given: ViewModel with mood colors created at different times
        val mood1 = TestDataBuilders.createMoodColor(mood = "First", id = 1, dateStamp = 1000L)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Second", id = 2, dateStamp = 2000L)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)
        viewModel = createViewModel()

        // When: Changing to Date Ascending
        viewModel.onAction(MoodColorManagementAction.ToggleSortOrder(MoodColorOrder.Date(OrderType.Ascending)))

        // Then: Should be sorted oldest first
        val state = viewModel.uiState.value
        assertTrue("Should be Date order", state.sortOrder is MoodColorOrder.Date)
        assertTrue("Should be Ascending", state.sortOrder.orderType is OrderType.Ascending)
        assertEquals("First should be oldest", "First", state.moodColorsWithCount[0].moodColor.mood)
    }

    @Test
    fun `toggleSortOrder - same order does nothing`() = runTest {
        // Given: ViewModel with default order (Date Descending)
        viewModel = createViewModel()
        val initialState = viewModel.uiState.value

        // When: Setting same order again
        viewModel.onAction(MoodColorManagementAction.ToggleSortOrder(MoodColorOrder.Date(OrderType.Descending)))

        // Then: State should remain the same (no unnecessary reload)
        val finalState = viewModel.uiState.value
        assertEquals("Sort order should be same", initialState.sortOrder::class, finalState.sortOrder::class)
    }

    // ============================================================
    // Delete Tests
    // ============================================================

    @Test
    fun `deleteMoodColor - removes from list and shows snackbar`() = runTest {
        // Given: ViewModel with mood colors
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)
        viewModel = createViewModel()

        // Collect events before action
        viewModel.uiEvents.test {
            // When: Deleting Happy
            viewModel.onAction(MoodColorManagementAction.DeleteMoodColor(mood1))

            // Then: Should emit snackbar event
            val event = awaitItem()
            assertTrue("Should be snackbar event", event is MoodColorManagementUiEvent.ShowSnackbar)
            val snackbarEvent = event as MoodColorManagementUiEvent.ShowSnackbar
            assertTrue("Message should mention deleted mood", snackbarEvent.message.contains("Happy"))
            assertEquals("Should have Undo action", "Undo", snackbarEvent.actionLabel)

            cancelAndIgnoreRemainingEvents()
        }

        // Then: Mood should be removed from list
        val state = viewModel.uiState.value
        assertEquals("Should have 1 mood color left", 1, state.moodColorsWithCount.size)
        assertEquals("Remaining should be Sad", "Sad", state.moodColorsWithCount[0].moodColor.mood)

        // And: Recently deleted should be set
        assertNotNull("Recently deleted should be set", state.recentlyDeletedMoodColor)
        assertEquals("Recently deleted should be Happy", "Happy", state.recentlyDeletedMoodColor?.mood)
    }

    @Test
    fun `restoreMoodColor - restores deleted mood and shows snackbar`() = runTest {
        // Given: ViewModel with a deleted mood color
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()

        // Collect events - must start before actions to capture SharedFlow emissions
        viewModel.uiEvents.test {
            // Delete the mood first
            viewModel.onAction(MoodColorManagementAction.DeleteMoodColor(mood1))

            // Should get delete snackbar
            val deleteEvent = awaitItem()
            assertTrue("Should be delete snackbar", (deleteEvent as MoodColorManagementUiEvent.ShowSnackbar).message.contains("deleted"))

            // When: Restoring
            viewModel.onAction(MoodColorManagementAction.RestoreMoodColor)

            // Then: Should emit restored snackbar
            val restoreEvent = awaitItem()
            assertTrue("Should be snackbar event", restoreEvent is MoodColorManagementUiEvent.ShowSnackbar)
            val snackbarEvent = restoreEvent as MoodColorManagementUiEvent.ShowSnackbar
            assertTrue("Message should mention restored", snackbarEvent.message.contains("restored"))

            cancelAndIgnoreRemainingEvents()
        }

        // Then: Recently deleted should be cleared
        val state = viewModel.uiState.value
        assertNull("Recently deleted should be null", state.recentlyDeletedMoodColor)
    }

    @Test
    fun `clearRecentlyDeleted - clears the recently deleted mood`() = runTest {
        // Given: ViewModel with a deleted mood color
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()
        viewModel.onAction(MoodColorManagementAction.DeleteMoodColor(mood1))

        // Verify it was set
        assertNotNull("Should have recently deleted", viewModel.uiState.value.recentlyDeletedMoodColor)

        // When: Clearing
        viewModel.onAction(MoodColorManagementAction.ClearRecentlyDeleted)

        // Then: Should be cleared
        assertNull("Should be null", viewModel.uiState.value.recentlyDeletedMoodColor)
    }

    // ============================================================
    // Add Mood Color Dialog Tests
    // ============================================================

    @Test
    fun `showAddMoodColorDialog - sets dialog state to true`() = runTest {
        // Given: ViewModel
        viewModel = createViewModel()
        assertFalse("Initially should be false", viewModel.uiState.value.showAddMoodColorDialog)

        // When: Showing dialog
        viewModel.onAction(MoodColorManagementAction.ShowAddMoodColorDialog)

        // Then: Should be true
        assertTrue("Should be true", viewModel.uiState.value.showAddMoodColorDialog)
    }

    @Test
    fun `dismissAddMoodColorDialog - sets dialog state to false`() = runTest {
        // Given: ViewModel with dialog open
        viewModel = createViewModel()
        viewModel.onAction(MoodColorManagementAction.ShowAddMoodColorDialog)

        // When: Dismissing
        viewModel.onAction(MoodColorManagementAction.DismissAddMoodColorDialog)

        // Then: Should be false
        assertFalse("Should be false", viewModel.uiState.value.showAddMoodColorDialog)
    }

    @Test
    fun `saveNewMoodColor - creates mood and shows snackbar`() = runTest {
        // Given: ViewModel
        viewModel = createViewModel()

        // Collect events
        viewModel.uiEvents.test {
            // When: Saving new mood
            viewModel.onAction(MoodColorManagementAction.SaveNewMoodColor("Excited", "FF5722"))

            // Then: Should emit snackbar
            val event = awaitItem()
            assertTrue("Should be snackbar event", event is MoodColorManagementUiEvent.ShowSnackbar)
            val snackbarEvent = event as MoodColorManagementUiEvent.ShowSnackbar
            assertTrue("Message should mention created", snackbarEvent.message.contains("created"))

            cancelAndIgnoreRemainingEvents()
        }

        // Then: Dialog should be closed
        assertFalse("Dialog should be closed", viewModel.uiState.value.showAddMoodColorDialog)

        // And: Mood should be in list
        val state = viewModel.uiState.value
        assertTrue("Should contain Excited", state.moodColorsWithCount.any { it.moodColor.mood == "Excited" })
    }

    @Test
    fun `saveNewMoodColor - with empty mood shows error`() = runTest {
        // Given: ViewModel
        viewModel = createViewModel()

        // Collect events
        viewModel.uiEvents.test {
            // When: Saving with empty mood name
            viewModel.onAction(MoodColorManagementAction.SaveNewMoodColor("", "FF5722"))

            // Then: Should emit error snackbar
            val event = awaitItem()
            assertTrue("Should be snackbar event", event is MoodColorManagementUiEvent.ShowSnackbar)
            val snackbarEvent = event as MoodColorManagementUiEvent.ShowSnackbar
            assertTrue("Should be error message", snackbarEvent.message.contains("blank") || snackbarEvent.message.contains("empty") || snackbarEvent.message.contains("Invalid"))

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Edit Mood Color Dialog Tests
    // ============================================================

    @Test
    fun `showEditMoodColorDialog - sets editing mood color`() = runTest {
        // Given: ViewModel with mood colors
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()

        assertNull("Initially should be null", viewModel.uiState.value.editingMoodColor)

        // When: Showing edit dialog
        viewModel.onAction(MoodColorManagementAction.ShowEditMoodColorDialog(mood1))

        // Then: Editing mood should be set
        val state = viewModel.uiState.value
        assertNotNull("Should have editing mood", state.editingMoodColor)
        assertEquals("Should be Happy", "Happy", state.editingMoodColor?.mood)
    }

    @Test
    fun `dismissEditMoodColorDialog - clears editing mood color`() = runTest {
        // Given: ViewModel with edit dialog open
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()
        viewModel.onAction(MoodColorManagementAction.ShowEditMoodColorDialog(mood1))

        // When: Dismissing
        viewModel.onAction(MoodColorManagementAction.DismissEditMoodColorDialog)

        // Then: Should be null
        assertNull("Should be null", viewModel.uiState.value.editingMoodColor)
    }

    @Test
    fun `saveEditedMoodColor - updates color and shows snackbar`() = runTest {
        // Given: ViewModel with mood color
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()
        viewModel.onAction(MoodColorManagementAction.ShowEditMoodColorDialog(mood1))

        // Collect events
        viewModel.uiEvents.test {
            // When: Saving edited color
            viewModel.onAction(MoodColorManagementAction.SaveEditedMoodColor(1, "FF5722"))

            // Then: Should emit snackbar
            val event = awaitItem()
            assertTrue("Should be snackbar event", event is MoodColorManagementUiEvent.ShowSnackbar)
            val snackbarEvent = event as MoodColorManagementUiEvent.ShowSnackbar
            assertTrue("Message should mention updated", snackbarEvent.message.contains("updated"))

            cancelAndIgnoreRemainingEvents()
        }

        // Then: Dialog should be closed
        assertNull("Editing mood should be null", viewModel.uiState.value.editingMoodColor)

        // And: Color should be updated in list
        val state = viewModel.uiState.value
        val updatedMood = state.moodColorsWithCount.find { it.moodColor.mood == "Happy" }
        assertEquals("Color should be updated", "FF5722", updatedMood?.moodColor?.color)
    }

    // ============================================================
    // Error Handling Tests
    // ============================================================

    @Test
    fun `retryLoadMoodColors - clears error and reloads`() = runTest {
        // Given: ViewModel
        viewModel = createViewModel()

        // When: Retrying (simulates error recovery)
        viewModel.onAction(MoodColorManagementAction.RetryLoadMoodColors)

        // Then: Error should be null and not loading
        val state = viewModel.uiState.value
        assertNull("Error should be null", state.loadError)
        assertFalse("Should not be loading", state.isLoading)
    }

    @Test
    fun `dismissLoadError - clears error state`() = runTest {
        // Given: ViewModel
        viewModel = createViewModel()

        // When: Dismissing error
        viewModel.onAction(MoodColorManagementAction.DismissLoadError)

        // Then: Error should be null
        assertNull("Error should be null", viewModel.uiState.value.loadError)
    }

    // ============================================================
    // Entry Count Tests
    // ============================================================

    @Test
    fun `mood color with zero entries shows count of 0`() = runTest {
        // Given: Mood color with no entries
        val mood1 = TestDataBuilders.createMoodColor(mood = "Unused", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)

        // When: Creating ViewModel
        viewModel = createViewModel()

        // Then: Entry count should be 0
        val state = viewModel.uiState.value
        val unusedMood = state.moodColorsWithCount.find { it.moodColor.mood == "Unused" }
        assertEquals("Should have 0 entries", 0, unusedMood?.entryCount)
    }

    @Test
    fun `entry counts update when entries change`() = runTest {
        // Given: ViewModel with mood color and initial entry
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, id = 1))

        viewModel = createViewModel()

        // Verify initial count
        var state = viewModel.uiState.value
        assertEquals("Initial count should be 1", 1, state.moodColorsWithCount[0].entryCount)

        // When: Adding another entry
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, id = 2))

        // Give time for Flow to emit
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Count should update to 2
        state = viewModel.uiState.value
        assertEquals("Updated count should be 2", 2, state.moodColorsWithCount[0].entryCount)
    }
}
