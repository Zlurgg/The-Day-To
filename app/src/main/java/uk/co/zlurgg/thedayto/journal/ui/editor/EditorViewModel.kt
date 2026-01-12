package uk.co.zlurgg.thedayto.journal.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.result.getOrNull
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.ui.util.launchDebouncedLoading
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidEntryException
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorOrder
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorAction
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiState
import uk.co.zlurgg.thedayto.journal.ui.editor.util.EditorPromptConstants
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class EditorViewModel(
    private val editorUseCases: EditorUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(EditorUiState(
        moodHint = EditorPromptConstants.TODAY_PROMPTS.random() // Default to today prompt
    ))
    val uiState = _uiState.asStateFlow()

    // One-time UI events
    private val _uiEvents = MutableSharedFlow<EditorUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var getMoodColorsJob: Job? = null

    // Original state for detecting unsaved changes
    private var originalMoodColorId: Int? = null
    private var originalContent: String = ""
    private var originalDate: Long = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)

    // Store entry ID for retry on load failure
    private var loadingEntryId: Int? = null

    init {
        Timber.d("EditorViewModel initialized")

        // Load mood colors
        loadMoodColors()

        // Set entry date from navigation parameter if provided
        savedStateHandle.get<Long>("entryDate")?.let { entryDate ->
            if (entryDate != -1L) {
                Timber.d("Setting entry date from navigation: $entryDate")
                originalDate = entryDate
                _uiState.update { it.copy(
                    entryDate = entryDate,
                    moodHint = getMoodHintForDate(entryDate)
                ) }
            }
        }

        // Load existing entry if editing, or show tutorial for new entries
        val entryId = savedStateHandle.get<Int>("entryId")
        if (entryId != null && entryId != -1) {
            // Store for potential retry
            loadingEntryId = entryId
            // Load existing entry
            loadEntry(entryId)
        } else {
            // New entry (entryId is null or -1) - check if tutorial should be shown
            checkAndShowEditorTutorial()
        }
    }

    /**
     * Check if editor tutorial should be shown for first-time entry creation
     *
     * Shows the editor tutorial if:
     * - User hasn't seen it before
     * - This is a new entry (not editing existing)
     */
    private fun checkAndShowEditorTutorial() {
        viewModelScope.launch {
            val hasSeenTutorial = editorUseCases.checkEditorTutorialSeen()
            if (!hasSeenTutorial) {
                Timber.d("First time creating entry - showing editor tutorial")
                _uiState.update { it.copy(showEditorTutorial = true) }
            }
        }
    }

    /**
     * Load an entry by ID from the database
     *
     * Shows loading state, handles errors with persistent banner, and loads entry into state on success.
     *
     * @param entryId The ID of the entry to load
     */
    private fun loadEntry(entryId: Int) {
        Timber.d("Loading existing entry with ID: $entryId")
        viewModelScope.launch {
            val loadingJob = launchDebouncedLoading { isLoading ->
                _uiState.update { it.copy(isLoading = isLoading) }
            }

            try {
                val entry = editorUseCases.getEntryUseCase(entryId).getOrNull()
                entry?.let {
                    loadingJob.cancel() // Cancel if finished quickly
                    Timber.d("Successfully loaded entry with ID: ${it.id}")
                    loadEntryIntoState(it)
                    _uiState.update { state -> state.copy(isLoading = false, loadError = null) }
                } ?: run {
                    // Entry not found - show persistent error
                    loadingJob.cancel()
                    Timber.w("Entry not found with ID: $entryId")
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            loadError = "Entry not found. It may have been deleted."
                        )
                    }
                }
            } catch (e: Exception) {
                loadingJob.cancel()
                Timber.e(e, "Failed to load entry with ID: $entryId")
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        loadError = "Failed to load entry: ${e.message ?: "Unknown error"}"
                    )
                }
            }
        }
    }

    /**
     * Get appropriate mood hint based on the selected date.
     *
     * Returns a random prompt from:
     * - TODAY_PROMPTS if date is today
     * - PAST_PROMPTS if date is in the past
     * - FUTURE_PROMPTS if date is in the future
     *
     * @param dateEpochSeconds The date in epoch seconds (UTC)
     * @return A contextual mood prompt string
     */
    private fun getMoodHintForDate(dateEpochSeconds: Long): String {
        val selectedDate = Instant.ofEpochSecond(dateEpochSeconds).atZone(ZoneOffset.UTC).toLocalDate()
        val today = LocalDate.now()

        return when {
            selectedDate.isEqual(today) -> EditorPromptConstants.TODAY_PROMPTS.random()
            selectedDate.isBefore(today) -> EditorPromptConstants.PAST_PROMPTS.random()
            else -> EditorPromptConstants.FUTURE_PROMPTS.random()
        }
    }

    /**
     * Load entry data into UI state
     *
     * @param entry The entry to load
     */
    private fun loadEntryIntoState(entry: Entry) {
        Timber.d("Loading entry into state: id=${entry.id}")
        // Store original values for unsaved changes detection
        originalMoodColorId = entry.moodColorId
        originalContent = entry.content
        originalDate = entry.dateStamp

        _uiState.update { state ->
            state.copy(
                currentEntryId = entry.id,
                entryDate = entry.dateStamp,
                selectedMoodColorId = entry.moodColorId,
                entryContent = entry.content,
                isMoodHintVisible = false,
                isContentHintVisible = false,
                moodHint = getMoodHintForDate(entry.dateStamp)
            )
        }
    }

    /**
     * Reset UI state to blank (for new entry)
     *
     * @param date The date for the new entry
     */
    private fun resetToBlankState(date: Long) {
        Timber.d("Resetting to blank state for date: $date")
        _uiState.update { state ->
            state.copy(
                currentEntryId = null,
                entryDate = date,
                selectedMoodColorId = null,
                entryContent = "",
                isMoodHintVisible = true,
                isContentHintVisible = true,
                moodHint = getMoodHintForDate(date)
            )
        }
    }

    /**
     * Check if there are unsaved changes in the editor
     *
     * Compares current state with original values to determine if user has made changes
     * that haven't been saved yet.
     *
     * @return true if there are unsaved changes, false otherwise
     */
    private fun hasUnsavedChanges(): Boolean {
        val currentState = _uiState.value
        return currentState.selectedMoodColorId != originalMoodColorId ||
                currentState.entryContent != originalContent ||
                currentState.entryDate != originalDate
    }

    fun onAction(action: EditorAction) {
        when (action) {
            is EditorAction.EnteredDate -> {
                viewModelScope.launch {
                    Timber.d("Date changed to: ${action.date}, checking for existing entry")

                    try {
                        // Check if entry exists for this date
                        val existingEntry = editorUseCases.getEntryByDateUseCase(action.date).getOrNull()

                        if (existingEntry != null) {
                            // Load existing entry for this date
                            Timber.d("Found existing entry for date, loading: id=${existingEntry.id}")
                            loadEntryIntoState(existingEntry)
                        } else {
                            // No entry for this date - reset to blank state
                            Timber.d("No entry found for date, resetting to blank state")
                            resetToBlankState(action.date)
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to load entry for date: ${action.date}")
                        // On error, reset to blank state so user can still create entry
                        resetToBlankState(action.date)
                        _uiEvents.emit(
                            EditorUiEvent.ShowSnackbar(
                                message = "Failed to load entry for this date"
                            )
                        )
                    }
                }
            }

            is EditorAction.SelectMoodColor -> {
                _uiState.update {
                    it.copy(
                        selectedMoodColorId = action.moodColorId,
                        isMoodHintVisible = false,
                        moodError = null // Clear error when mood selected
                    )
                }
            }

            is EditorAction.EnteredContent -> {
                _uiState.update { it.copy(entryContent = action.value) }
            }

            is EditorAction.ChangeContentFocus -> {
                _uiState.update {
                    it.copy(
                        isContentHintVisible = !action.focusState.isFocused &&
                                it.entryContent.isBlank()
                    )
                }
            }

            is EditorAction.ToggleMoodColorSection -> {
                _uiState.update {
                    it.copy(isMoodColorSectionVisible = !it.isMoodColorSectionVisible)
                }
            }

            is EditorAction.SaveMoodColor -> {
                viewModelScope.launch {
                    try {
                        Timber.d("Saving new mood color: ${action.mood.trim()}")
                        val newMoodColorId = editorUseCases.addMoodColorUseCase(
                            MoodColor(
                                mood = action.mood.trim(),
                                color = action.colorHex,
                                dateStamp = System.currentTimeMillis() / 1000, // Use precise timestamp so user moods sort first
                                id = null
                            )
                        )
                        // Auto-select the newly created/restored mood color
                        Timber.d("Successfully saved mood color: ${action.mood.trim()}, auto-selecting ID: $newMoodColorId")
                        _uiState.update {
                            it.copy(
                                isMoodColorSectionVisible = false,
                                selectedMoodColorId = newMoodColorId,
                                isMoodHintVisible = false, // Hide hint since mood is now selected
                                moodError = null // Clear error since mood is now selected
                            )
                        }
                    } catch (e: InvalidMoodColorException) {
                        Timber.e(e, "Failed to save mood color: ${action.mood.trim()}")
                        _uiEvents.emit(
                            EditorUiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save mood color!"
                            )
                        )
                    }
                }
            }

            is EditorAction.DeleteMoodColor -> {
                viewModelScope.launch {
                    Timber.d("Deleting mood color: ${action.moodColor.mood}")
                    editorUseCases.deleteMoodColor(action.moodColor.id ?: return@launch)

                    // Check if the deleted mood was currently selected
                    val currentState = _uiState.value
                    if (currentState.selectedMoodColorId == action.moodColor.id) {

                        // Reset to first available mood color or null
                        val remainingMoodColors = currentState.moodColors.filter {
                            it.id != action.moodColor.id
                        }

                        if (remainingMoodColors.isNotEmpty()) {
                            // Default to first mood color in list
                            val defaultMoodColor = remainingMoodColors.first()
                            Timber.d("Selected mood was deleted, switching to: ${defaultMoodColor.mood}")
                            _uiState.update {
                                it.copy(
                                    selectedMoodColorId = defaultMoodColor.id,
                                    isMoodHintVisible = false
                                )
                            }
                        } else {
                            // No mood colors left, reset to null
                            Timber.w("No mood colors remaining after deletion")
                            _uiState.update {
                                it.copy(
                                    selectedMoodColorId = null,
                                    isMoodHintVisible = true
                                )
                            }
                        }
                    }
                }
            }

            is EditorAction.EditMoodColor -> {
                Timber.d("Opening edit dialog for mood: ${action.moodColor.mood}")
                _uiState.update {
                    it.copy(
                        showEditMoodColorDialog = true,
                        editingMoodColor = action.moodColor
                    )
                }
            }

            is EditorAction.UpdateMoodColor -> {
                viewModelScope.launch {
                    try {
                        val originalMood = _uiState.value.editingMoodColor
                        Timber.d("Updating mood color: id=${action.moodColorId}, newMood=${action.newMood}, newColor=${action.newColorHex}")

                        // Update name if changed
                        if (originalMood != null && originalMood.mood != action.newMood) {
                            Timber.d("Updating mood name: ${originalMood.mood} -> ${action.newMood}")
                            editorUseCases.updateMoodColorNameUseCase(
                                id = action.moodColorId,
                                newMood = action.newMood
                            )
                        }

                        // Update color
                        editorUseCases.updateMoodColorUseCase(
                            id = action.moodColorId,
                            newColor = action.newColorHex
                        )

                        // Close dialog and clear any errors
                        _uiState.update {
                            it.copy(
                                showEditMoodColorDialog = false,
                                editingMoodColor = null,
                                editMoodColorError = null
                            )
                        }

                        Timber.i("Successfully updated mood color: ${action.newMood}")
                        _uiEvents.emit(
                            EditorUiEvent.ShowSnackbar("\"${action.newMood}\" updated")
                        )
                    } catch (e: InvalidMoodColorException) {
                        Timber.w(e, "Invalid mood color update")
                        // Show inline error in dialog instead of snackbar
                        _uiState.update {
                            it.copy(editMoodColorError = e.message ?: "Invalid mood")
                        }
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to update mood color")
                        _uiEvents.emit(
                            EditorUiEvent.ShowSnackbar(
                                message = "Failed to update: ${e.message}"
                            )
                        )
                    }
                }
            }

            is EditorAction.CloseEditMoodColorDialog -> {
                Timber.d("Closing edit mood color dialog")
                _uiState.update {
                    it.copy(
                        showEditMoodColorDialog = false,
                        editingMoodColor = null,
                        editMoodColorError = null
                    )
                }
            }

            is EditorAction.ClearEditMoodColorError -> {
                _uiState.update {
                    it.copy(editMoodColorError = null)
                }
            }

            is EditorAction.DismissEditorTutorial -> {
                viewModelScope.launch {
                    Timber.d("Editor tutorial dismissed - marking as seen")
                    editorUseCases.markEditorTutorialSeen()
                    _uiState.update { it.copy(showEditorTutorial = false) }
                }
            }

            is EditorAction.ToggleDatePicker -> {
                Timber.d("Toggling date picker dialog")
                _uiState.update { it.copy(showDatePicker = !it.showDatePicker) }
            }

            is EditorAction.SaveEntry -> {
                viewModelScope.launch {
                    val state = _uiState.value

                    // Validate moodColorId is selected
                    val moodColorId = state.selectedMoodColorId
                    if (moodColorId == null) {
                        // Show inline error
                        _uiState.update { it.copy(moodError = "Please select or create a mood") }
                        return@launch
                    }

                    Timber.d("Saving entry: moodColorId=$moodColorId, date=${state.entryDate}, id=${state.currentEntryId}")

                    val loadingJob = launchDebouncedLoading { isLoading ->
                        _uiState.update { it.copy(isLoading = isLoading) }
                    }

                    try {
                        editorUseCases.addEntryUseCase(
                            Entry(
                                moodColorId = moodColorId,
                                content = state.entryContent,
                                dateStamp = state.entryDate,
                                id = state.currentEntryId
                            )
                        )
                        loadingJob.cancel() // Cancel if finished quickly
                        Timber.d("Successfully saved entry")
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(EditorUiEvent.SaveEntry)
                    } catch (e: InvalidEntryException) {
                        loadingJob.cancel()
                        Timber.e(e, "Failed to save entry")
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(
                            EditorUiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save entry"
                            )
                        )
                    }
                }
            }

            is EditorAction.RequestNavigateBack -> {
                Timber.d("Back button pressed, checking for unsaved changes")
                if (hasUnsavedChanges()) {
                    // Show unsaved changes dialog
                    Timber.d("Unsaved changes detected, showing dialog")
                    _uiState.update { it.copy(showUnsavedChangesDialog = true) }
                } else {
                    // No unsaved changes, navigate back immediately
                    Timber.d("No unsaved changes, navigating back")
                    viewModelScope.launch {
                        _uiEvents.emit(EditorUiEvent.NavigateBack)
                    }
                }
            }

            is EditorAction.ConfirmDiscardChanges -> {
                Timber.d("User confirmed discard changes")
                _uiState.update { it.copy(showUnsavedChangesDialog = false) }
                viewModelScope.launch {
                    _uiEvents.emit(EditorUiEvent.NavigateBack)
                }
            }

            is EditorAction.DismissUnsavedChangesDialog -> {
                Timber.d("User dismissed unsaved changes dialog")
                _uiState.update { it.copy(showUnsavedChangesDialog = false) }
            }

            is EditorAction.RetryLoadEntry -> {
                Timber.d("Retrying entry load")
                _uiState.update { it.copy(loadError = null) }
                loadingEntryId?.let { entryId ->
                    loadEntry(entryId)
                } ?: run {
                    Timber.w("Cannot retry: no entry ID stored")
                    _uiState.update { it.copy(loadError = "Cannot retry: no entry ID") }
                }
            }

            is EditorAction.DismissLoadError -> {
                Timber.d("Dismissing load error banner")
                _uiState.update { it.copy(loadError = null) }
            }
        }
    }

    private fun loadMoodColors() {
        getMoodColorsJob?.cancel()
        getMoodColorsJob = editorUseCases.getMoodColors(
            MoodColorOrder.Date(
                OrderType.Descending
            )
        )
            .onEach { moodColors ->
                _uiState.update { it.copy(moodColors = moodColors) }
            }
            .launchIn(viewModelScope)
    }
}
