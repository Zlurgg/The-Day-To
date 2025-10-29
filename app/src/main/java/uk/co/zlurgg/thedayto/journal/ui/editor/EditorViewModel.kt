package uk.co.zlurgg.thedayto.journal.ui.editor

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidEntryException
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorAction
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiState
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorOrder
import uk.co.zlurgg.thedayto.core.domain.util.DateUtils
import java.time.LocalDate
import java.time.ZoneOffset

class EditorViewModel(
    private val editorUseCases: EditorUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState = _uiState.asStateFlow()

    // One-time UI events
    private val _uiEvents = MutableSharedFlow<EditorUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var getMoodColorsJob: Job? = null

    init {
        // Load mood colors
        loadMoodColors()

        // Set entry date from navigation parameter if provided
        savedStateHandle.get<Long>("entryDate")?.let { entryDate ->
            if (entryDate != -1L) {
                _uiState.update { it.copy(entryDate = entryDate) }
                updateMoodHint()
            }
        }

        // Update hint for initial state
        updateMoodHint()

        // Load existing entry if editing
        savedStateHandle.get<Int>("entryId")?.let { entryId ->
            if (entryId != -1) {
                viewModelScope.launch {
                    // Debounced loading: only show if operation takes > 150ms
                    val loadingJob = launch {
                        delay(150)
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    try {
                        val entry = withContext(Dispatchers.IO) {
                            editorUseCases.getEntryUseCase(entryId)
                        }
                        entry?.let {
                            loadingJob.cancel() // Cancel if finished quickly
                            _uiState.update { state ->
                                state.copy(
                                    currentEntryId = it.id,
                                    entryDate = it.dateStamp,
                                    entryMood = it.mood,
                                    entryContent = it.content,
                                    entryColor = it.color,
                                    isMoodHintVisible = false,
                                    isContentHintVisible = false,
                                    isLoading = false
                                )
                            }
                        } ?: run {
                            // Entry not found
                            loadingJob.cancel()
                            _uiState.update { it.copy(isLoading = false) }
                            _uiEvents.emit(
                                EditorUiEvent.ShowSnackbar(
                                    message = "Entry not found"
                                )
                            )
                        }
                    } catch (e: Exception) {
                        loadingJob.cancel()
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(
                            EditorUiEvent.ShowSnackbar(
                                message = "Failed to load entry: ${e.message}"
                            )
                        )
                    }
                }
            }
        }
    }

    fun onAction(action: EditorAction) {
        when (action) {
            is EditorAction.EnteredDate -> {
                _uiState.update { it.copy(entryDate = action.date) }
            }

            is EditorAction.EnteredMood -> {
                _uiState.update { it.copy(entryMood = action.mood) }
            }

            is EditorAction.ChangeMoodFocus -> {
                _uiState.update {
                    it.copy(
                        isMoodHintVisible = !action.focusState.isFocused &&
                                it.entryMood.isBlank()
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

            is EditorAction.EnteredColor -> {
                _uiState.update { it.copy(entryColor = action.color) }
            }

            is EditorAction.ToggleMoodColorSection -> {
                _uiState.update {
                    it.copy(isMoodColorSectionVisible = !it.isMoodColorSectionVisible)
                }
            }

            is EditorAction.SaveMoodColor -> {
                viewModelScope.launch {
                    try {
                        withContext(Dispatchers.IO) {
                            editorUseCases.addMoodColorUseCase(
                                MoodColor(
                                    mood = action.mood.trim(),
                                    color = action.colorHex,
                                    dateStamp = DateUtils.getTodayStartEpoch(),
                                    id = null
                                )
                            )
                        }
                        // Close dialog after successful save
                        _uiState.update { it.copy(isMoodColorSectionVisible = false) }
                    } catch (e: InvalidMoodColorException) {
                        _uiEvents.emit(
                            EditorUiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save mood color"
                            )
                        )
                    }
                }
            }

            is EditorAction.DeleteMoodColor -> {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        editorUseCases.deleteMoodColor(action.moodColor)
                    }

                    // Check if the deleted mood was currently selected
                    val currentState = _uiState.value
                    if (currentState.entryMood == action.moodColor.mood &&
                        currentState.entryColor == action.moodColor.color) {

                        // Reset to first available mood color or empty
                        val remainingMoodColors = currentState.moodColors.filter {
                            it.id != action.moodColor.id
                        }

                        if (remainingMoodColors.isNotEmpty()) {
                            // Default to first mood color in list
                            val defaultMoodColor = remainingMoodColors.first()
                            _uiState.update {
                                it.copy(
                                    entryMood = defaultMoodColor.mood,
                                    entryColor = defaultMoodColor.color,
                                    isMoodHintVisible = false
                                )
                            }
                        } else {
                            // No mood colors left, reset to empty
                            _uiState.update {
                                it.copy(
                                    entryMood = "",
                                    entryColor = "",
                                    isMoodHintVisible = true
                                )
                            }
                        }
                    }
                }
            }

            is EditorAction.SaveEntry -> {
                viewModelScope.launch {
                    val state = _uiState.value

                    // Debounced loading: only show if operation takes > 150ms
                    val loadingJob = launch {
                        delay(150)
                        _uiState.update { it.copy(isLoading = true) }
                    }

                    try {
                        withContext(Dispatchers.IO) {
                            editorUseCases.addEntryUseCase(
                                Entry(
                                    content = state.entryContent,
                                    dateStamp = state.entryDate,
                                    mood = state.entryMood,
                                    color = state.entryColor,
                                    id = state.currentEntryId
                                )
                            )
                        }
                        loadingJob.cancel() // Cancel if finished quickly
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(EditorUiEvent.SaveEntry)
                    } catch (e: InvalidEntryException) {
                        loadingJob.cancel()
                        _uiState.update { it.copy(isLoading = false) }
                        _uiEvents.emit(
                            EditorUiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save entry"
                            )
                        )
                    }
                }
            }
        }
    }

    /**
     * Update mood hint based on whether the entry is for today or a previous day
     */
    private fun updateMoodHint() {
        val isToday = _uiState.value.entryDate == LocalDate.now().atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)

        val hint = if (isToday) {
            "How're you feeling today?"
        } else {
            "How were you feeling that day?"
        }

        _uiState.update { it.copy(moodHint = hint) }
    }

    private fun loadMoodColors() {
        getMoodColorsJob?.cancel()
        getMoodColorsJob = editorUseCases.getMoodColors(
            MoodColorOrder.Date(
                uk.co.zlurgg.thedayto.core.domain.util.OrderType.Descending
            )
        )
            .onEach { moodColors ->
                _uiState.update { it.copy(moodColors = moodColors) }
            }
            .launchIn(viewModelScope)
    }
}
