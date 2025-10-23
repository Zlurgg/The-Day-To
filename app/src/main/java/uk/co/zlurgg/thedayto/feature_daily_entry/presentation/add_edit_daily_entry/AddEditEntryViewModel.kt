package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.InvalidDailyEntryException
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.DailyEntryUseCases
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

    private val _entryDate = mutableStateOf(
        EntryDateFieldState(
            date = 0L
        )
    )
    val entryDate: State<EntryDateFieldState> = _entryDate

    private val _entryMood = mutableStateOf(
        EntryMoodState(
            todayHint = "How're you feeling today?",
            previousDayHint = "How're were you feeling that day?"
        )
    )
    val entryMood: State<EntryMoodState> = _entryMood

    private val _entryContent = mutableStateOf(
        EntryContentFieldState(
            hint = "Any additional info?"
        )
    )
    val entryContent: State<EntryContentFieldState> = _entryContent

    private val _entryColor = mutableStateOf("#000000")
    val entryColor: State<String> = _entryColor

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentEntryId: Int? = null

    private val _state = mutableStateOf(EntryMoodColorSectionState())
    val state: State<EntryMoodColorSectionState> = _state


    init {
        savedStateHandle.get<Int>("entryId")?.let { entryId ->
            if (entryId != -1) {
                viewModelScope.launch(Dispatchers.IO) {
                    dailyEntryUseCases.getDailyEntry(entryId)?.also { entry ->
                        withContext(Dispatchers.Main) {
                            currentEntryId = entry.id
                            _entryDate.value = entryDate.value.copy(
                                date = entry.dateStamp,
                            )
                        }
                        _entryMood.value = entryMood.value.copy(
                            mood = entry.mood,
                            isHintVisible = false
                        )
                        _entryContent.value = entryContent.value.copy(
                            text = entry.content,
                            isHintVisible = false
                        )
                        _entryColor.value = entry.color
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditEntryEvent) {
        when (event) {
            is AddEditEntryEvent.EnteredDate -> {
                _entryDate.value = entryDate.value.copy(
                    date = event.date
                )
            }

            is AddEditEntryEvent.EnteredMood -> {
                _entryMood.value = entryMood.value.copy(
                    mood = event.mood
                )
            }

            is AddEditEntryEvent.ChangeMoodFocus -> {
                _entryMood.value = entryMood.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            entryMood.value.mood.isBlank()
                )
            }

            is AddEditEntryEvent.EnteredContent -> {
                _entryContent.value = _entryContent.value.copy(
                    text = event.value
                )
            }

            is AddEditEntryEvent.ChangeContentFocus -> {
                _entryContent.value = _entryContent.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            _entryContent.value.text.isBlank()
                )
            }

            is AddEditEntryEvent.EnteredColor -> {
                _entryColor.value = event.color
            }

            is AddEditEntryEvent.ToggleMoodColorSection -> {
                _state.value = state.value.copy(
                    isMoodColorSectionVisible = !state.value.isMoodColorSectionVisible
                )
            }

            is AddEditEntryEvent.SaveMoodColor -> {
                viewModelScope.launch(Dispatchers.IO) {
                    // Validate input
                    if (event.mood.isBlank()) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = "Mood cannot be empty"
                            )
                        )
                        return@launch
                    }

                    try {
                        moodColorUseCases.addMoodColor(
                            MoodColor(
                                mood = event.mood.trim(),
                                color = event.colorHex,
                                dateStamp = LocalDate.now().atStartOfDay()
                                    .toEpochSecond(ZoneOffset.UTC),
                                id = null
                            )
                        )
                        // Close dialog after successful save
                        withContext(Dispatchers.Main) {
                            _state.value = state.value.copy(
                                isMoodColorSectionVisible = false
                            )
                        }
                    } catch (e: InvalidMoodColorException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save mood color"
                            )
                        )
                    }
                }
            }

            is AddEditEntryEvent.SaveEntry -> {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        dailyEntryUseCases.addDailyEntry(
                            DailyEntry(
                                content = entryContent.value.text,
                                dateStamp = entryDate.value.date,
                                mood = entryMood.value.mood,
                                color = entryColor.value,
                                id = currentEntryId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveEntry)
                        preferencesRepository.setDailyEntryDate(entryDate.value.date)
                    } catch (e: InvalidDailyEntryException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save entry"
                            )
                        )
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data object SaveEntry : UiEvent()
    }
}