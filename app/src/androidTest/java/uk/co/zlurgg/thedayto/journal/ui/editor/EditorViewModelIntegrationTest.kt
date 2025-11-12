package uk.co.zlurgg.thedayto.journal.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.zlurgg.thedayto.base.DatabaseTest
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.data.repository.EntryRepositoryImpl
import uk.co.zlurgg.thedayto.journal.data.repository.MoodColorRepositoryImpl
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.AddEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntryUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.AddMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.DeleteMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.GetMoodColorsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.UpdateMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorAction
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Integration tests for EditorViewModel with real Room database.
 *
 * Tests the complete flow from ViewModel -> Use Cases -> Repositories -> Database
 * for entry creation and editing scenarios.
 *
 * Critical validations:
 * - Creating new entries persists to database
 * - Loading existing entries for editing
 * - Updating entries persists changes
 * - Foreign key validation (moodColorId must exist)
 */
@RunWith(AndroidJUnit4::class)
class EditorViewModelIntegrationTest : DatabaseTest() {

    private lateinit var viewModel: EditorViewModel
    private lateinit var entryRepository: EntryRepositoryImpl
    private lateinit var moodColorRepository: MoodColorRepositoryImpl
    private lateinit var savedStateHandle: SavedStateHandle

    @Before
    fun setupViewModel() {
        // Create real repositories with in-memory database
        entryRepository = EntryRepositoryImpl(entryDao)
        moodColorRepository = MoodColorRepositoryImpl(moodColorDao)

        // Create saved state handle
        savedStateHandle = SavedStateHandle()

        // Create use cases
        val editorUseCases = EditorUseCases(
            getEntryUseCase = GetEntryUseCase(entryRepository),
            addEntryUseCase = AddEntryUseCase(entryRepository, moodColorRepository),
            getMoodColorUseCase = GetMoodColorUseCase(moodColorRepository),
            addMoodColorUseCase = AddMoodColorUseCase(moodColorRepository),
            deleteMoodColor = DeleteMoodColorUseCase(moodColorRepository),
            getMoodColors = GetMoodColorsUseCase(moodColorRepository),
            updateMoodColorUseCase = UpdateMoodColorUseCase(moodColorRepository)
        )

        // Create ViewModel with real dependencies
        viewModel = EditorViewModel(editorUseCases, savedStateHandle)
    }

    // ============================================================
    // Create Entry Tests
    // ============================================================

    @Test
    fun createNewEntry_persists_to_database() = runTest {
        // Given: A mood color exists
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        // When: Creating a new entry
        viewModel.onAction(EditorAction.SelectMoodColor(moodColor.id!!))
        viewModel.onAction(EditorAction.EnteredContent("My first entry"))
        viewModel.onAction(EditorAction.SaveEntry)

        // Give time for async operation
        kotlinx.coroutines.delay(100)

        // Then: Entry should be in database
        val entries = entryDao.getEntries()
        entries.test {
            val entriesList = awaitItem()
            assertEquals("Should have 1 entry in database", 1, entriesList.size)
            assertEquals("Content should match", "My first entry", entriesList[0].content)
            assertEquals("MoodColorId should match", 1, entriesList[0].moodColorId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun createEntry_validates_foreign_key_constraint() = runTest {
        // Given: No mood colors in database

        // When: Attempting to create entry with non-existent moodColorId
        val fakeMoodColor = TestDataBuilders.createMoodColor(mood = "Fake", id = 999)
        viewModel.onAction(EditorAction.SelectMoodColor(fakeMoodColor.id!!))
        viewModel.onAction(EditorAction.EnteredContent("Test"))
        viewModel.onAction(EditorAction.SaveEntry)

        // Then: Should emit error event
        viewModel.uiEvents.test {
            val event = awaitItem()
            assertTrue("Should be ShowSnackbar event", event is EditorUiEvent.ShowSnackbar)
            if (event is EditorUiEvent.ShowSnackbar) {
                assertTrue(
                    "Error message should mention mood",
                    event.message.contains("mood", ignoreCase = true)
                )
            }

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Load/Edit Entry Tests
    // ============================================================

    @Test
    fun loadExistingEntry_populates_ui_state() = runTest {
        // Given: An existing entry in database
        val moodColor = TestDataBuilders.createMoodColor(mood = "Calm", color = "9C27B0", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = "Existing entry content",
            dateStamp = TestDataBuilders.getTodayEpoch(),
            id = 1
        )
        entryDao.insertEntry(entry.toEntity())

        // When: Creating ViewModel with entryId to edit
        val editSavedStateHandle = SavedStateHandle().apply {
            set("entryId", 1)
        }

        val editorUseCases = EditorUseCases(
            getEntryUseCase = GetEntryUseCase(entryRepository),
            addEntryUseCase = AddEntryUseCase(entryRepository, moodColorRepository),
            getMoodColorUseCase = GetMoodColorUseCase(moodColorRepository),
            addMoodColorUseCase = AddMoodColorUseCase(moodColorRepository),
            deleteMoodColor = DeleteMoodColorUseCase(moodColorRepository),
            getMoodColors = GetMoodColorsUseCase(moodColorRepository),
            updateMoodColorUseCase = UpdateMoodColorUseCase(moodColorRepository)
        )

        val editViewModel = EditorViewModel(editorUseCases, editSavedStateHandle)

        // Give time for loading
        kotlinx.coroutines.delay(200)

        // Then: UI state should be populated with entry data
        editViewModel.uiState.test {
            val state = awaitItem()

            assertEquals("Entry ID should be set", 1, state.currentEntryId)
            assertEquals("Content should match", "Existing entry content", state.entryContent)
            assertEquals("MoodColorId should match", 1, state.selectedMoodColorId)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateExistingEntry_persists_changes_to_database() = runTest {
        // Given: An existing entry
        val moodColor1 = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        val moodColor2 = TestDataBuilders.createMoodColor(mood = "Sad", color = "2196F3", id = 2)
        moodColorDao.insertMoodColor(moodColor1.toEntity())
        moodColorDao.insertMoodColor(moodColor2.toEntity())

        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = "Original content",
            dateStamp = TestDataBuilders.getTodayEpoch(),
            id = 1
        )
        entryDao.insertEntry(entry.toEntity())

        // When: Loading and updating the entry
        val editSavedStateHandle = SavedStateHandle().apply {
            set("entryId", 1)
        }

        val editorUseCases = EditorUseCases(
            getEntryUseCase = GetEntryUseCase(entryRepository),
            addEntryUseCase = AddEntryUseCase(entryRepository, moodColorRepository),
            getMoodColorUseCase = GetMoodColorUseCase(moodColorRepository),
            addMoodColorUseCase = AddMoodColorUseCase(moodColorRepository),
            deleteMoodColor = DeleteMoodColorUseCase(moodColorRepository),
            getMoodColors = GetMoodColorsUseCase(moodColorRepository),
            updateMoodColorUseCase = UpdateMoodColorUseCase(moodColorRepository)
        )

        val editViewModel = EditorViewModel(editorUseCases, editSavedStateHandle)

        // Wait for entry to load
        kotlinx.coroutines.delay(200)

        // Update the entry
        editViewModel.onAction(EditorAction.SelectMoodColor(moodColor2.id!!))
        editViewModel.onAction(EditorAction.EnteredContent("Updated content"))
        editViewModel.onAction(EditorAction.SaveEntry)

        // Wait for save
        kotlinx.coroutines.delay(100)

        // Then: Database should reflect the updates
        val updatedEntry = entryDao.getEntryById(1)
        assertNotNull("Entry should still exist", updatedEntry)
        assertEquals("Content should be updated", "Updated content", updatedEntry?.content)
        assertEquals("MoodColorId should be updated", 2, updatedEntry?.moodColorId)
    }

    // ============================================================
    // Mood Color Loading Tests
    // ============================================================

    @Test
    fun viewModel_loads_mood_colors_on_init() = runTest {
        // Given: Multiple mood colors in database
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
        val mood3 = TestDataBuilders.createMoodColor(mood = "Calm", id = 3)
        moodColorDao.insertMoodColor(mood1.toEntity())
        moodColorDao.insertMoodColor(mood2.toEntity())
        moodColorDao.insertMoodColor(mood3.toEntity())

        // When: Creating new ViewModel
        val newViewModel = EditorViewModel(
            EditorUseCases(
                getEntryUseCase = GetEntryUseCase(entryRepository),
                addEntryUseCase = AddEntryUseCase(entryRepository, moodColorRepository),
                getMoodColorUseCase = GetMoodColorUseCase(moodColorRepository),
                addMoodColorUseCase = AddMoodColorUseCase(moodColorRepository),
                deleteMoodColor = DeleteMoodColorUseCase(moodColorRepository),
                getMoodColors = GetMoodColorsUseCase(moodColorRepository),
                updateMoodColorUseCase = UpdateMoodColorUseCase(moodColorRepository)
            ),
            SavedStateHandle()
        )

        // Give time for loading
        kotlinx.coroutines.delay(100)

        // Then: UI state should contain mood colors
        newViewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have 3 mood colors", 3, state.moodColors.size)
            assertTrue("Should contain Happy", state.moodColors.any { it.mood == "Happy" })
            assertTrue("Should contain Sad", state.moodColors.any { it.mood == "Sad" })
            assertTrue("Should contain Calm", state.moodColors.any { it.mood == "Calm" })

            cancelAndIgnoreRemainingEvents()
        }
    }
}
