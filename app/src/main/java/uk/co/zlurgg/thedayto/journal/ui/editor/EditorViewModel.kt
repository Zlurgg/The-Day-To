package uk.co.zlurgg.thedayto.journal.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.result.getOrNull
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.ui.util.launchDebouncedLoading
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidEntryException
import uk.co.zlurgg.thedayto.journal.domain.model.sortedByFavorite
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorOrder
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorAction
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiState
import uk.co.zlurgg.thedayto.journal.ui.editor.util.EditorPromptConstants
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class EditorViewModel(
    private val editorUseCases: EditorUseCases,
    private val syncScheduler: SyncScheduler,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(
        EditorUiState(
            moodHint = EditorPromptConstants.TODAY_PROMPTS.random(), // Default to today prompt
        ),
    )
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

    // Delegate for mood color actions
    private val moodColorDelegate = MoodColorActionDelegate(
        editorUseCases = editorUseCases,
        uiState = _uiState,
        uiEvents = _uiEvents,
        scope = viewModelScope,
        onSyncRequired = ::triggerSync,
    )

    init {
        Timber.d("EditorViewModel initialized")

        // Load mood colors
        loadMoodColors()

        // Set entry date from navigation parameter if provided
        savedStateHandle.get<Long>("entryDate")?.let { entryDate ->
            if (entryDate != -1L) {
                Timber.d("Setting entry date from navigation: $entryDate")
                originalDate = entryDate
                _uiState.update {
                    it.copy(
                        entryDate = entryDate,
                        moodHint = getMoodHintForDate(entryDate),
                    )
                }
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
                            loadError = "Entry not found. It may have been deleted.",
                        )
                    }
                }
            } catch (e: Exception) {
                loadingJob.cancel()
                Timber.e(e, "Failed to load entry with ID: $entryId")
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        loadError = "Failed to load entry: ${e.message ?: "Unknown error"}",
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
                moodHint = getMoodHintForDate(entry.dateStamp),
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
                moodHint = getMoodHintForDate(date),
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
            // Entry actions
            is EditorAction.EnteredDate -> handleDateChange(action.date)
            is EditorAction.EnteredContent -> handleContentChange(action.value)
            is EditorAction.ChangeContentFocus -> handleContentFocusChange(action.focusState)
            is EditorAction.SaveEntry -> handleSaveEntry()

            // Mood color actions (delegated)
            is EditorAction.SelectMoodColor ->
                moodColorDelegate.handleSelectMoodColor(action.moodColorId)

            is EditorAction.ToggleMoodColorSection ->
                moodColorDelegate.handleToggleMoodColorSection()

            is EditorAction.SaveMoodColor ->
                moodColorDelegate.handleSaveMoodColor(action.mood, action.colorHex)

            is EditorAction.ToggleMoodColorFavorite ->
                moodColorDelegate.handleToggleFavorite(action.moodColor)

            is EditorAction.EditMoodColor ->
                moodColorDelegate.handleEditMoodColor(action.moodColor)

            is EditorAction.UpdateMoodColor ->
                moodColorDelegate.handleUpdateMoodColor(
                    action.moodColorId,
                    action.newMood,
                    action.newColorHex,
                )

            is EditorAction.CloseEditMoodColorDialog ->
                moodColorDelegate.handleCloseEditMoodColorDialog()

            is EditorAction.ClearEditMoodColorError ->
                moodColorDelegate.handleClearEditMoodColorError()

            // UI actions
            is EditorAction.DismissEditorTutorial -> handleDismissEditorTutorial()
            is EditorAction.ToggleDatePicker -> handleToggleDatePicker()

            // Navigation actions
            is EditorAction.RequestNavigateBack -> handleRequestNavigateBack()
            is EditorAction.ConfirmDiscardChanges -> handleConfirmDiscardChanges()
            is EditorAction.DismissUnsavedChangesDialog -> handleDismissUnsavedChangesDialog()
            is EditorAction.RetryLoadEntry -> handleRetryLoadEntry()
            is EditorAction.DismissLoadError -> handleDismissLoadError()
        }
    }

    // region Action Handlers

    private fun handleDateChange(date: Long) {
        viewModelScope.launch {
            Timber.d("Date changed to: $date, checking for existing entry")

            try {
                val existingEntry = editorUseCases.getEntryByDateUseCase(date).getOrNull()

                if (existingEntry != null) {
                    Timber.d("Found existing entry for date, loading: id=${existingEntry.id}")
                    loadEntryIntoState(existingEntry)
                } else {
                    Timber.d("No entry found for date, resetting to blank state")
                    resetToBlankState(date)
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to load entry for date: $date")
                resetToBlankState(date)
                _uiEvents.emit(
                    EditorUiEvent.ShowSnackbar(message = "Failed to load entry for this date"),
                )
            }
        }
    }

    private fun handleContentChange(value: String) {
        _uiState.update { it.copy(entryContent = value) }
    }

    private fun handleContentFocusChange(focusState: androidx.compose.ui.focus.FocusState) {
        _uiState.update {
            it.copy(isContentHintVisible = !focusState.isFocused && it.entryContent.isBlank())
        }
    }

    private fun handleDismissEditorTutorial() {
        viewModelScope.launch {
            Timber.d("Editor tutorial dismissed - marking as seen")
            editorUseCases.markEditorTutorialSeen()
            _uiState.update { it.copy(showEditorTutorial = false) }
        }
    }

    private fun handleToggleDatePicker() {
        Timber.d("Toggling date picker dialog")
        _uiState.update { it.copy(showDatePicker = !it.showDatePicker) }
    }

    private fun handleSaveEntry() {
        viewModelScope.launch {
            val state = _uiState.value

            val moodColorId = state.selectedMoodColorId
            if (moodColorId == null) {
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
                        id = state.currentEntryId,
                    ),
                )
                loadingJob.cancel()
                Timber.d("Successfully saved entry")
                triggerSync()
                _uiState.update { it.copy(isLoading = false, shouldNavigateBack = true) }
            } catch (e: InvalidEntryException) {
                loadingJob.cancel()
                Timber.e(e, "Failed to save entry")
                _uiState.update { it.copy(isLoading = false) }
                _uiEvents.emit(
                    EditorUiEvent.ShowSnackbar(message = e.message ?: "Couldn't save entry"),
                )
            }
        }
    }

    private fun handleRequestNavigateBack() {
        Timber.d("Back button pressed, checking for unsaved changes")
        if (hasUnsavedChanges()) {
            Timber.d("Unsaved changes detected, showing dialog")
            _uiState.update { it.copy(showUnsavedChangesDialog = true) }
        } else {
            Timber.d("No unsaved changes, navigating back")
            _uiState.update { it.copy(shouldNavigateBack = true) }
        }
    }

    private fun handleConfirmDiscardChanges() {
        Timber.d("User confirmed discard changes")
        _uiState.update { it.copy(showUnsavedChangesDialog = false, shouldNavigateBack = true) }
    }

    private fun handleDismissUnsavedChangesDialog() {
        Timber.d("User dismissed unsaved changes dialog")
        _uiState.update { it.copy(showUnsavedChangesDialog = false) }
    }

    private fun handleRetryLoadEntry() {
        Timber.d("Retrying entry load")
        _uiState.update { it.copy(loadError = null) }
        loadingEntryId?.let { entryId ->
            loadEntry(entryId)
        } ?: run {
            Timber.w("Cannot retry: no entry ID stored")
            _uiState.update { it.copy(loadError = "Cannot retry: no entry ID") }
        }
    }

    private fun handleDismissLoadError() {
        Timber.d("Dismissing load error banner")
        _uiState.update { it.copy(loadError = null) }
    }

    // endregion

    /**
     * Called after navigation has been handled by the UI
     */
    fun onNavigationHandled() {
        _uiState.update { it.copy(shouldNavigateBack = false) }
    }

    private fun loadMoodColors() {
        getMoodColorsJob?.cancel()
        getMoodColorsJob = editorUseCases.getMoodColors(
            MoodColorOrder.Date(OrderType.Descending),
        )
            // Sort by favorites first for the dropdown, inside the flow pipeline
            // so distinctUntilChanged can short-circuit redundant Room re-emissions
            // without the ViewModel's state being re-assigned unnecessarily.
            .map { moodColors -> moodColors.sortedByFavorite() }
            .distinctUntilChanged()
            .onEach { sorted ->
                _uiState.update { it.copy(moodColors = sorted) }
            }
            .launchIn(viewModelScope)
    }

    /**
     * Trigger sync after data changes.
     * Uses WorkManager's KEEP policy for deduplication (no debounce needed).
     */
    private fun triggerSync() {
        syncScheduler.requestImmediateSync()
    }
}
