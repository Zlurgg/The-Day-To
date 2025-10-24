package uk.co.zlurgg.thedayto.feature_mood_color.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case.MoodColorUseCases
import uk.co.zlurgg.thedayto.feature_mood_color.domain.util.MoodColorOrder
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.state.AddEditMoodColorAction
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.state.AddEditMoodColorUiEvent
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.state.AddEditMoodColorUiState

class AddEditMoodColorViewModel(
    private val moodColorUseCases: MoodColorUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(AddEditMoodColorUiState())
    val uiState = _uiState.asStateFlow()

    // One-time UI events
    private val _uiEvents = MutableSharedFlow<AddEditMoodColorUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var getMoodColorsJob: Job? = null

    init {
        getMoodColors(MoodColorOrder.Date(OrderType.Descending))
        savedStateHandle.get<Int>("moodColorId")?.let { moodColorId ->
            if (moodColorId != -1) {
                viewModelScope.launch {
                    moodColorUseCases.getMoodColor(moodColorId)?.also { moodColor ->
                        _uiState.update {
                            it.copy(
                                currentMoodColorId = moodColor.id,
                                date = moodColor.dateStamp,
                                mood = moodColor.mood,
                                color = moodColor.color,
                                isMoodHintVisible = false
                            )
                        }
                    }
                }
            }
        }
    }

    fun onAction(action: AddEditMoodColorAction) {
        when (action) {
            is AddEditMoodColorAction.EnteredDate -> {
                _uiState.update { it.copy(date = action.date) }
            }

            is AddEditMoodColorAction.EnteredMood -> {
                _uiState.update { it.copy(mood = action.mood) }
            }

            is AddEditMoodColorAction.ChangeMoodFocus -> {
                _uiState.update {
                    it.copy(
                        isMoodHintVisible = !action.focusState.isFocused &&
                                it.mood.isBlank()
                    )
                }
            }

            is AddEditMoodColorAction.EnteredColor -> {
                _uiState.update { it.copy(color = action.colorEnvelope.hexCode) }
            }

            is AddEditMoodColorAction.SaveMoodColor -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val state = _uiState.value
                    try {
                        moodColorUseCases.addMoodColor(
                            MoodColor(
                                dateStamp = state.date,
                                mood = state.mood,
                                color = state.color,
                                id = state.currentMoodColorId
                            )
                        )
                        _uiEvents.emit(AddEditMoodColorUiEvent.SaveMoodColor)
                    } catch (e: InvalidMoodColorException) {
                        _uiEvents.emit(
                            AddEditMoodColorUiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save new mood color"
                            )
                        )
                    }
                }
            }

            is AddEditMoodColorAction.DeleteMoodColor -> {
                viewModelScope.launch {
                    moodColorUseCases.deleteMoodColor(action.moodColor)
                    _uiState.update { it.copy(recentlyDeletedMoodColor = action.moodColor) }
                }
            }
        }
    }

    private fun getMoodColors(moodColorOrder: MoodColorOrder) {
        getMoodColorsJob?.cancel()
        getMoodColorsJob = moodColorUseCases.getMoodColors(moodColorOrder)
            .onEach { moodColors ->
                _uiState.update {
                    it.copy(
                        moodColors = moodColors,
                        moodColorOrder = moodColorOrder
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
