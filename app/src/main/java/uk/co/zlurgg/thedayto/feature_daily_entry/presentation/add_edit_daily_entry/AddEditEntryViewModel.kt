package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.InvalidDailyEntryException
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.DailyEntryUseCases
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.state.AddEditEntryAction
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.state.AddEditEntryUiEvent
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.state.AddEditEntryUiState
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case.MoodColorUseCases
import java.time.LocalDate
import java.time.ZoneOffset

class AddEditEntryViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val dailyEntryUseCases: DailyEntryUseCases,
    private val moodColorUseCases: MoodColorUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(AddEditEntryUiState())
    val uiState = _uiState.asStateFlow()

    // One-time UI events
    private val _uiEvents = MutableSharedFlow<AddEditEntryUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        savedStateHandle.get<Int>("entryId")?.let { entryId ->
            if (entryId != -1) {
                viewModelScope.launch(Dispatchers.IO) {
                    dailyEntryUseCases.getDailyEntry(entryId)?.also { entry ->
                        _uiState.update {
                            it.copy(
                                currentEntryId = entry.id,
                                entryDate = entry.dateStamp,
                                entryMood = entry.mood,
                                entryContent = entry.content,
                                entryColor = entry.color,
                                isMoodHintVisible = false,
                                isContentHintVisible = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun onAction(action: AddEditEntryAction) {
        when (action) {
            is AddEditEntryAction.EnteredDate -> {
                _uiState.update { it.copy(entryDate = action.date) }
            }

            is AddEditEntryAction.EnteredMood -> {
                _uiState.update { it.copy(entryMood = action.mood) }
            }

            is AddEditEntryAction.ChangeMoodFocus -> {
                _uiState.update {
                    it.copy(
                        isMoodHintVisible = !action.focusState.isFocused &&
                                it.entryMood.isBlank()
                    )
                }
            }

            is AddEditEntryAction.EnteredContent -> {
                _uiState.update { it.copy(entryContent = action.value) }
            }

            is AddEditEntryAction.ChangeContentFocus -> {
                _uiState.update {
                    it.copy(
                        isContentHintVisible = !action.focusState.isFocused &&
                                it.entryContent.isBlank()
                    )
                }
            }

            is AddEditEntryAction.EnteredColor -> {
                _uiState.update { it.copy(entryColor = action.color) }
            }

            is AddEditEntryAction.ToggleMoodColorSection -> {
                _uiState.update {
                    it.copy(isMoodColorSectionVisible = !it.isMoodColorSectionVisible)
                }
            }

            is AddEditEntryAction.SaveMoodColor -> {
                viewModelScope.launch(Dispatchers.IO) {
                    // Validate input
                    if (action.mood.isBlank()) {
                        _uiEvents.emit(
                            AddEditEntryUiEvent.ShowSnackbar(
                                message = "Mood cannot be empty"
                            )
                        )
                        return@launch
                    }

                    try {
                        moodColorUseCases.addMoodColor(
                            MoodColor(
                                mood = action.mood.trim(),
                                color = action.colorHex,
                                dateStamp = LocalDate.now().atStartOfDay()
                                    .toEpochSecond(ZoneOffset.UTC),
                                id = null
                            )
                        )
                        // Close dialog after successful save
                        _uiState.update { it.copy(isMoodColorSectionVisible = false) }
                    } catch (e: InvalidMoodColorException) {
                        _uiEvents.emit(
                            AddEditEntryUiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save mood color"
                            )
                        )
                    }
                }
            }

            is AddEditEntryAction.SaveEntry -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val state = _uiState.value
                    try {
                        dailyEntryUseCases.addDailyEntry(
                            DailyEntry(
                                content = state.entryContent,
                                dateStamp = state.entryDate,
                                mood = state.entryMood,
                                color = state.entryColor,
                                id = state.currentEntryId
                            )
                        )
                        _uiEvents.emit(AddEditEntryUiEvent.SaveEntry)
                        preferencesRepository.setDailyEntryDate(state.entryDate)
                    } catch (e: InvalidDailyEntryException) {
                        _uiEvents.emit(
                            AddEditEntryUiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save entry"
                            )
                        )
                    }
                }
            }
        }
    }
}
