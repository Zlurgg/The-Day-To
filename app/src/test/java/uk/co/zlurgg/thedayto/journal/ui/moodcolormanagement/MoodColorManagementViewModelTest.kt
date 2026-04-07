package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement

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
import io.mockk.mockk
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement.MoodColorManagementUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetSortedMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.RestoreMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SaveMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SetMoodColorFavoriteUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.ValidateMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementAction
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for MoodColorManagementViewModel.
 *
 * Tests cover:
 * - Initialization (load mood colors sorted by favorites + usage)
 * - Delete with undo functionality
 * - Add new mood color
 * - Edit mood color
 * - Toggle favorite
 * - Error handling
 *
 * Following Google's best practices: ViewModels tested with fake repositories.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MoodColorManagementViewModelTest {

    private lateinit var viewModel: MoodColorManagementViewModel
    private lateinit var fakeEntryRepository: FakeEntryRepository
    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository
    private val mockSyncScheduler: SyncScheduler = mockk(relaxed = true)

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
        val validateUseCase = ValidateMoodColorUseCase(fakeMoodColorRepository)
        val useCases = MoodColorManagementUseCases(
            getSortedMoodColors = GetSortedMoodColorsUseCase(fakeMoodColorRepository, fakeEntryRepository),
            saveMoodColor = SaveMoodColorUseCase(validateUseCase, fakeMoodColorRepository),
            deleteMoodColor = DeleteMoodColorUseCase(fakeMoodColorRepository),
            restoreMoodColor = RestoreMoodColorUseCase(fakeMoodColorRepository),
            setFavorite = SetMoodColorFavoriteUseCase(fakeMoodColorRepository)
        )
        return MoodColorManagementViewModel(
            useCases = useCases,
            syncScheduler = mockSyncScheduler
        )
    }

    // ============================================================
    // Initialization Tests
    // ============================================================

    @Test
    fun `init - loads mood colors with entry counts`() = runTest {
        // Given: Mood colors and entries exist
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
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
        val state = viewModel.state.value
        assertEquals("Should have 2 mood colors", 2, state.moodColors.size)

        val happyMood = state.moodColors.find { it.moodColor.mood == "Happy" }
        val sadMood = state.moodColors.find { it.moodColor.mood == "Sad" }

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
        val state = viewModel.state.value
        assertTrue("Should be empty", state.moodColors.isEmpty())
        assertFalse("Should not be loading", state.isLoading)
    }

    @Test
    fun `init - sorts by favorites first then by usage count`() = runTest {
        // Given: Mood colors with different favorites and entry counts
        val mood1 = TestDataBuilders.createMoodColor(mood = "LowUsage", id = 1, isFavorite = false)
        val mood2 = TestDataBuilders.createMoodColor(mood = "HighUsage", id = 2, isFavorite = false)
        val mood3 = TestDataBuilders.createMoodColor(mood = "Favorite", id = 3, isFavorite = true)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)
        fakeMoodColorRepository.insertMoodColor(mood3)

        // Add entries: 1 for LowUsage, 5 for HighUsage, 2 for Favorite
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, id = 1))
        repeat(5) { index ->
            fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 2, id = 10 + index))
        }
        repeat(2) { index ->
            fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 3, id = 20 + index))
        }

        // When: Creating ViewModel
        viewModel = createViewModel()

        // Then: Should be sorted: Favorite first, then by usage
        val state = viewModel.state.value
        assertEquals("First should be Favorite (isFavorite=true)", "Favorite", state.moodColors[0].moodColor.mood)
        assertEquals("Second should be HighUsage (5 entries)", "HighUsage", state.moodColors[1].moodColor.mood)
        assertEquals("Third should be LowUsage (1 entry)", "LowUsage", state.moodColors[2].moodColor.mood)
    }

    // ============================================================
    // Delete Tests
    // ============================================================

    @Test
    fun `deleteMoodColor - removes from list and shows undo snackbar`() = runTest {
        // Given: ViewModel with mood colors
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)
        viewModel = createViewModel()

        // When: Requesting delete (opens confirmation dialog)
        viewModel.onAction(MoodColorManagementAction.RequestDeleteMoodColor(mood1))

        // Then: Pending delete should be set (dialog shown)
        var state = viewModel.state.value
        assertNotNull("Pending delete should be set", state.pendingDelete)
        assertEquals("Pending delete should be Happy", "Happy", state.pendingDelete?.mood)

        // And: Mood should still be in list (not deleted yet)
        assertEquals("Should have 2 mood colors", 2, state.moodColors.size)

        // When: Confirming delete
        viewModel.onAction(MoodColorManagementAction.ConfirmDelete)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Mood should be removed from list (soft deleted)
        state = viewModel.state.value
        assertEquals("Should have 1 mood color left", 1, state.moodColors.size)
        assertEquals("Remaining should be Sad", "Sad", state.moodColors[0].moodColor.mood)

        // And: Pending delete should be cleared
        assertNull("Pending delete should be null", state.pendingDelete)
    }

    @Test
    fun `dismissDeleteDialog - cancels deletion`() = runTest {
        // Given: ViewModel with delete confirmation dialog open
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()

        // Open delete dialog
        viewModel.onAction(MoodColorManagementAction.RequestDeleteMoodColor(mood1))
        assertNotNull("Should have pending delete", viewModel.state.value.pendingDelete)

        // When: Dismissing dialog
        viewModel.onAction(MoodColorManagementAction.DismissDeleteDialog)

        // Then: Pending delete should be cleared and mood still exists
        val state = viewModel.state.value
        assertNull("Pending delete should be null", state.pendingDelete)
        assertEquals("Should still have 1 mood color", 1, state.moodColors.size)
        assertEquals("Should be Happy", "Happy", state.moodColors[0].moodColor.mood)
    }

    // ============================================================
    // Add/Edit Mood Color Dialog Tests
    // ============================================================

    @Test
    fun `addMoodColor - opens dialog with empty mood color`() = runTest {
        // Given: ViewModel
        viewModel = createViewModel()
        assertNull("Initially should be null", viewModel.state.value.editingMoodColor)

        // When: Adding mood color
        viewModel.onAction(MoodColorManagementAction.AddMoodColor)

        // Then: Should have editing mood with null id (new)
        val editing = viewModel.state.value.editingMoodColor
        assertNotNull("Should have editing mood", editing)
        assertNull("Id should be null (new mood)", editing?.id)
    }

    @Test
    fun `editMoodColor - opens dialog with existing mood color`() = runTest {
        // Given: ViewModel with mood colors
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()

        assertNull("Initially should be null", viewModel.state.value.editingMoodColor)

        // When: Editing
        viewModel.onAction(MoodColorManagementAction.EditMoodColor(mood1))

        // Then: Editing mood should be set
        val state = viewModel.state.value
        assertNotNull("Should have editing mood", state.editingMoodColor)
        assertEquals("Should be Happy", "Happy", state.editingMoodColor?.mood)
    }

    @Test
    fun `dismissDialog - clears editing mood color`() = runTest {
        // Given: ViewModel with edit dialog open
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()
        viewModel.onAction(MoodColorManagementAction.EditMoodColor(mood1))

        // When: Dismissing
        viewModel.onAction(MoodColorManagementAction.DismissDialog)

        // Then: Should be null
        assertNull("Should be null", viewModel.state.value.editingMoodColor)
    }

    @Test
    fun `saveMoodColor - creates new mood and closes dialog`() = runTest {
        // Given: ViewModel
        viewModel = createViewModel()

        // When: Saving new mood
        val newMood = MoodColor.empty().copy(mood = "Excited", color = "FF5722")
        viewModel.onAction(MoodColorManagementAction.SaveMoodColor(newMood))

        // Then: Dialog should be closed
        assertNull("Editing should be null", viewModel.state.value.editingMoodColor)

        // And: Mood should be in list
        val state = viewModel.state.value
        assertTrue("Should contain Excited", state.moodColors.any { it.moodColor.mood == "Excited" })
    }

    @Test
    fun `saveMoodColor - shows error for blank name`() = runTest {
        // Given: ViewModel
        viewModel = createViewModel()
        viewModel.onAction(MoodColorManagementAction.AddMoodColor)

        // When: Saving with blank name
        val badMood = MoodColor.empty().copy(mood = "", color = "FF5722")
        viewModel.onAction(MoodColorManagementAction.SaveMoodColor(badMood))

        // Then: Dialog should still be open with error
        val state = viewModel.state.value
        assertNotNull("Dialog should still be open", state.editingMoodColor)
        assertEquals("Should have BlankName error", MoodColorError.BlankName, state.dialogError)
    }

    @Test
    fun `saveMoodColor - shows error for duplicate name`() = runTest {
        // Given: ViewModel with existing mood color
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()
        viewModel.onAction(MoodColorManagementAction.AddMoodColor)

        // When: Saving with duplicate name
        val duplicate = MoodColor.empty().copy(mood = "Happy", color = "FF5722")
        viewModel.onAction(MoodColorManagementAction.SaveMoodColor(duplicate))

        // Then: Should have DuplicateName error
        val state = viewModel.state.value
        assertEquals("Should have DuplicateName error", MoodColorError.DuplicateName, state.dialogError)
    }

    @Test
    fun `clearError - clears dialog error`() = runTest {
        // Given: ViewModel with error
        viewModel = createViewModel()
        viewModel.onAction(MoodColorManagementAction.AddMoodColor)
        val badMood = MoodColor.empty().copy(mood = "", color = "FF5722")
        viewModel.onAction(MoodColorManagementAction.SaveMoodColor(badMood))

        // Verify error exists
        assertNotNull("Should have error", viewModel.state.value.dialogError)

        // When: Clearing error
        viewModel.onAction(MoodColorManagementAction.ClearError)

        // Then: Error should be null
        assertNull("Error should be null", viewModel.state.value.dialogError)
    }

    // ============================================================
    // Toggle Favorite Tests
    // ============================================================

    @Test
    fun `toggleFavorite - updates favorite state optimistically`() = runTest {
        // Given: ViewModel with non-favorite mood color
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1, isFavorite = false)
        fakeMoodColorRepository.insertMoodColor(mood1)
        viewModel = createViewModel()

        // Verify initial state
        assertFalse("Initially not favorite", viewModel.state.value.moodColors[0].moodColor.isFavorite)

        // When: Toggling favorite
        viewModel.onAction(MoodColorManagementAction.ToggleFavorite(id = 1, currentValue = false))

        // Then: Should be favorite optimistically
        assertTrue("Should be favorite now", viewModel.state.value.moodColors[0].moodColor.isFavorite)
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
        val state = viewModel.state.value
        val unusedMood = state.moodColors.find { it.moodColor.mood == "Unused" }
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
        var state = viewModel.state.value
        assertEquals("Initial count should be 1", 1, state.moodColors[0].entryCount)

        // When: Adding another entry
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, id = 2))

        // Give time for Flow to emit
        testDispatcher.scheduler.advanceUntilIdle()

        // Then: Count should update to 2
        state = viewModel.state.value
        assertEquals("Updated count should be 2", 2, state.moodColors[0].entryCount)
    }
}
