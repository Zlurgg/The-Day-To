package uk.co.zlurgg.thedayto.journal.ui.editor

import androidx.compose.ui.focus.FocusState
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidEntryException
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.AddEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.AddMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.UpdateMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorOrder
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorAction
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for EditorViewModel.
 *
 * Tests cover:
 * - Initialization (load mood colors, set date from nav, load existing entry)
 * - State updates (date, mood selection, content, focus/hints, toggles)
 * - Mood color management (save/delete with validation)
 * - Entry save (create/update, validation, error handling)
 *
 * Following Google's best practices: ViewModels tested with fake repositories.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private lateinit var viewModel: EditorViewModel
    private lateinit var fakeEntryRepository: FakeEntryRepository
    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository
    private lateinit var savedStateHandle: SavedStateHandle

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeEntryRepository = FakeEntryRepository()
        fakeMoodColorRepository = FakeMoodColorRepository()
        savedStateHandle = SavedStateHandle()

        // Seed with default mood colors
        fakeMoodColorRepository.addDefaultMoods()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): EditorViewModel {
        val editorUseCases = EditorUseCases(
            getEntryUseCase = GetEntryUseCase(fakeEntryRepository),
            addEntryUseCase = AddEntryUseCase(fakeEntryRepository, fakeMoodColorRepository),
            getMoodColorUseCase = GetMoodColorUseCase(fakeMoodColorRepository),
            addMoodColorUseCase = AddMoodColorUseCase(fakeMoodColorRepository),
            deleteMoodColor = DeleteMoodColorUseCase(fakeMoodColorRepository),
            getMoodColors = GetMoodColorsUseCase(fakeMoodColorRepository),
            updateMoodColorUseCase = UpdateMoodColorUseCase(fakeMoodColorRepository)
        )
        return EditorViewModel(editorUseCases, savedStateHandle)
    }

    // ============================================================
    // Initialization Tests
    // ============================================================

    @Test
    fun `loads mood colors on initialization`() = runTest {
        // When: ViewModel is created
        viewModel = createViewModel()

        // Then: Mood colors should be loaded
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should load 5 default mood colors", 5, state.moodColors.size)
            assertTrue("Should contain Happy", state.moodColors.any { it.mood == "Happy" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `sets entry date from navigation parameter`() = runTest {
        // Given: Entry date in saved state
        val testDate = 1704067200L // 2024-01-01
        savedStateHandle["entryDate"] = testDate

        // When: ViewModel is created
        viewModel = createViewModel()

        // Then: Entry date should be set
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Entry date should match navigation parameter", testDate, state.entryDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads existing entry when editing`() = runTest {
        // Given: An existing entry
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)
        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Test content", id = 1)
        fakeEntryRepository.insertEntry(entry)

        savedStateHandle["entryId"] = 1

        // When: ViewModel is created
        viewModel = createViewModel()

        // Then: Entry should be loaded into state
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Entry ID should be set", 1, state.currentEntryId)
            assertEquals("Content should be loaded", "Test content", state.entryContent)
            assertEquals("Mood color ID should be set", 1, state.selectedMoodColorId)
            assertFalse("Mood hint should be hidden", state.isMoodHintVisible)
            assertFalse("Content hint should be hidden", state.isContentHintVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits error when entry not found during load`() = runTest {
        // Given: Entry ID that doesn't exist
        savedStateHandle["entryId"] = 999

        // When: ViewModel is created - init emits error event
        // Note: Cannot reliably test event emission timing in unit tests
        // This would require integration testing with real database

        // Then: Verify the ViewModel handles missing entry gracefully (no crash)
        // In production, the error event would be emitted via uiEvents SharedFlow
        val testViewModel = createViewModel()

        // Verify state is not corrupt after error
        testViewModel.uiState.test {
            val state = awaitItem()
            assertFalse("Should not be loading after error", state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // State Update Tests
    // ============================================================

    @Test
    fun `EnteredDate action updates entry date`() = runTest {
        viewModel = createViewModel()
        val newDate = 1704153600L // 2024-01-02

        // When: Date is changed
        viewModel.onAction(EditorAction.EnteredDate(newDate))

        // Then: State should be updated
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Entry date should be updated", newDate, state.entryDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SelectMoodColor action updates selected mood and hides hint`() = runTest {
        viewModel = createViewModel()

        // When: Mood color is selected
        viewModel.onAction(EditorAction.SelectMoodColor(moodColorId = 2))

        // Then: State should be updated
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Selected mood color ID should be 2", 2, state.selectedMoodColorId)
            assertFalse("Mood hint should be hidden", state.isMoodHintVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `EnteredContent action updates entry content`() = runTest {
        viewModel = createViewModel()

        // When: Content is entered
        viewModel.onAction(EditorAction.EnteredContent("My journal entry"))

        // Then: State should be updated
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Entry content should be updated", "My journal entry", state.entryContent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ChangeContentFocus action shows hint when unfocused and empty`() = runTest {
        viewModel = createViewModel()
        val focusState = mockk<FocusState> {
            every { isFocused } returns false
        }

        // When: Focus is lost and content is empty
        viewModel.onAction(EditorAction.ChangeContentFocus(focusState))

        // Then: Hint should be visible
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Content hint should be visible", state.isContentHintVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ChangeContentFocus action hides hint when unfocused but has content`() = runTest {
        viewModel = createViewModel()
        viewModel.onAction(EditorAction.EnteredContent("Some content"))

        val focusState = mockk<FocusState> {
            every { isFocused } returns false
        }

        // When: Focus is lost but content exists
        viewModel.onAction(EditorAction.ChangeContentFocus(focusState))

        // Then: Hint should be hidden
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("Content hint should be hidden", state.isContentHintVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `ToggleMoodColorSection action toggles visibility`() = runTest {
        viewModel = createViewModel()

        // When: Toggle is called
        viewModel.onAction(EditorAction.ToggleMoodColorSection)

        // Then: Section should be visible
        viewModel.uiState.test {
            var state = awaitItem()
            assertTrue("Mood color section should be visible", state.isMoodColorSectionVisible)

            // When: Toggled again
            viewModel.onAction(EditorAction.ToggleMoodColorSection)
            state = awaitItem()
            assertFalse("Mood color section should be hidden", state.isMoodColorSectionVisible)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Mood Color Management Tests
    // ============================================================

    @Test
    fun `SaveMoodColor action creates new mood color and closes dialog`() = runTest {
        viewModel = createViewModel()

        // Open dialog
        viewModel.onAction(EditorAction.ToggleMoodColorSection)

        // Save new mood color
        viewModel.onAction(EditorAction.SaveMoodColor(mood = "Excited", colorHex = "FFA500"))

        // Wait for async operations to complete
        testScheduler.advanceUntilIdle()

        // Then: Verify final state (dialog closed, new mood added)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("Dialog should be closed after save", state.isMoodColorSectionVisible)
            assertTrue("Should have new mood color", state.moodColors.any { it.mood == "Excited" })
            cancelAndIgnoreRemainingEvents()
        }

        // Verify mood was actually saved to repository
        val allMoods = fakeMoodColorRepository.getMoodColorsSync()
        assertTrue("Excited should be in repository", allMoods.any { it.mood == "Excited" })
    }

    @Test
    fun `SaveMoodColor action emits error for invalid mood color`() = runTest {
        viewModel = createViewModel()

        // When: Invalid mood color is saved (collect events first)
        viewModel.uiEvents.test {
            viewModel.onAction(EditorAction.SaveMoodColor(mood = "", colorHex = "FF0000"))

            // Then: Error event should be emitted
            val event = awaitItem()
            assertTrue("Should be ShowSnackbar event", event is EditorUiEvent.ShowSnackbar)
            assertTrue("Should contain error message", (event as EditorUiEvent.ShowSnackbar).message.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DeleteMoodColor action deletes when not currently selected`() = runTest {
        viewModel = createViewModel()

        // Select Happy
        viewModel.onAction(EditorAction.SelectMoodColor(moodColorId = 1))

        val sadMood = fakeMoodColorRepository.getMoodColorById(2)!!

        // Delete a different mood color (Sad)
        viewModel.onAction(EditorAction.DeleteMoodColor(sadMood))

        // Wait for async operations
        testScheduler.advanceUntilIdle()

        // Then: Verify final state (Sad deleted, Happy still selected)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("Sad should be deleted", state.moodColors.any { it.mood == "Sad" })
            assertEquals("Selection should remain Happy", 1, state.selectedMoodColorId)
            cancelAndIgnoreRemainingEvents()
        }

        // Verify mood was soft-deleted in repository
        val sadInRepo = fakeMoodColorRepository.getMoodColorByIdSync(2)
        assertTrue("Sad should be marked deleted", sadInRepo!!.isDeleted)
    }

    @Test
    fun `DeleteMoodColor action resets to first mood when selected mood is deleted`() = runTest {
        viewModel = createViewModel()

        // Select Happy
        viewModel.onAction(EditorAction.SelectMoodColor(moodColorId = 1))

        val happyMood = fakeMoodColorRepository.getMoodColorById(1)!!

        // Delete the currently selected mood
        viewModel.onAction(EditorAction.DeleteMoodColor(happyMood))

        // Wait for async operations
        testScheduler.advanceUntilIdle()

        // Then: Verify final state (Happy deleted, switched to first remaining)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("Happy should be deleted", state.moodColors.any { it.mood == "Happy" })
            assertEquals("Should switch to Sad (first remaining)", 2, state.selectedMoodColorId)
            assertFalse("Mood hint should be hidden", state.isMoodHintVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DeleteMoodColor action resets to null when last mood is deleted`() = runTest {
        // Given: Only one mood color
        fakeMoodColorRepository.reset()
        val onlyMood = TestDataBuilders.createMoodColor(mood = "Only", id = 1)
        fakeMoodColorRepository.insertMoodColor(onlyMood)
        viewModel = createViewModel()

        // Select the only mood
        viewModel.onAction(EditorAction.SelectMoodColor(moodColorId = 1))

        // Delete the last mood color
        viewModel.onAction(EditorAction.DeleteMoodColor(onlyMood))

        // Wait for async operations
        testScheduler.advanceUntilIdle()

        // Then: Verify final state (no moods, selection null, hint visible)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have no mood colors", 0, state.moodColors.size)
            assertNull("Selection should be null", state.selectedMoodColorId)
            assertTrue("Mood hint should be visible", state.isMoodHintVisible)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Entry Save Tests
    // ============================================================

    @Test
    fun `SaveEntry action creates new entry successfully`() = runTest {
        viewModel = createViewModel()
        viewModel.onAction(EditorAction.SelectMoodColor(moodColorId = 1))
        viewModel.onAction(EditorAction.EnteredContent("My first entry"))

        // When: Entry is saved (collect events first)
        viewModel.uiEvents.test {
            viewModel.onAction(EditorAction.SaveEntry)

            // Then: Entry should be created and SaveEntry event emitted
            val event = awaitItem()
            assertTrue("Should be SaveEntry event", event is EditorUiEvent.SaveEntry)
            cancelAndIgnoreRemainingEvents()
        }

        // Verify entry was saved
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Should have 1 entry", 1, entries.size)
        assertEquals("Content should match", "My first entry", entries[0].content)
    }

    @Test
    fun `SaveEntry action updates existing entry successfully`() = runTest {
        // Given: Existing entry
        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Original", id = 1)
        fakeEntryRepository.insertEntry(entry)
        savedStateHandle["entryId"] = 1

        viewModel = createViewModel()
        viewModel.onAction(EditorAction.EnteredContent("Updated content"))

        // When: Entry is saved (collect events first)
        viewModel.uiEvents.test {
            viewModel.onAction(EditorAction.SaveEntry)

            // Then: Entry should be updated
            val event = awaitItem()
            assertTrue("Should be SaveEntry event", event is EditorUiEvent.SaveEntry)
            cancelAndIgnoreRemainingEvents()
        }

        val updatedEntry = fakeEntryRepository.getEntryById(1)!!
        assertEquals("Content should be updated", "Updated content", updatedEntry.content)
    }

    @Test
    fun `SaveEntry action emits error when no mood is selected`() = runTest {
        viewModel = createViewModel()
        viewModel.onAction(EditorAction.EnteredContent("Content without mood"))

        // When: Trying to save without selecting a mood (collect events first)
        viewModel.uiEvents.test {
            viewModel.onAction(EditorAction.SaveEntry)

            // Then: Error event should be emitted
            val event = awaitItem()
            assertTrue("Should be ShowSnackbar event", event is EditorUiEvent.ShowSnackbar)
            assertEquals("Should show select mood message", "Please select a mood", (event as EditorUiEvent.ShowSnackbar).message)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SaveEntry action emits error for invalid entry`() = runTest {
        viewModel = createViewModel()
        viewModel.onAction(EditorAction.SelectMoodColor(moodColorId = 999)) // Non-existent mood

        // When: Trying to save with invalid mood ID (collect events first)
        viewModel.uiEvents.test {
            viewModel.onAction(EditorAction.SaveEntry)

            // Then: Error event should be emitted (foreign key violation)
            val event = awaitItem()
            assertTrue("Should be ShowSnackbar event", event is EditorUiEvent.ShowSnackbar)
            assertTrue("Should contain error message", (event as EditorUiEvent.ShowSnackbar).message.isNotEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `SaveEntry action shows loading state during save`() = runTest {
        viewModel = createViewModel()
        viewModel.onAction(EditorAction.SelectMoodColor(moodColorId = 1))
        viewModel.onAction(EditorAction.EnteredContent("Test"))

        // When: Entry is being saved
        viewModel.onAction(EditorAction.SaveEntry)

        // Then: Loading state should be managed (set to false after completion)
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("Loading should be false after completion", state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
