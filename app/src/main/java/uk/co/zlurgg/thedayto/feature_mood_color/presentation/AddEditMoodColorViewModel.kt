package uk.co.zlurgg.thedayto.feature_mood_color.presentation

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case.MoodColorUseCases
import uk.co.zlurgg.thedayto.feature_mood_color.domain.util.MoodColorOrder

class AddEditMoodColorViewModel(
    private val moodColorUseCases: MoodColorUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _moodColorDate = mutableStateOf(MoodColorDateFieldState(date = 0L))
    val moodColorDate: State<MoodColorDateFieldState> = _moodColorDate

    private val _moodColorMood = mutableStateOf(MoodTextFieldState(hint = "Enter a new mood"))
    val moodColorMood: State<MoodTextFieldState> = _moodColorMood

    private val _moodColorColor = mutableStateOf("#000000")
    val moodColorColor: MutableState<String> = _moodColorColor

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentMoodColorId: Int? = null

    private val _state = mutableStateOf(MoodColorState())
    val state: State<MoodColorState> = _state

    private var getMoodColorsJob: Job? = null

    private var recentlyDeletedMoodColor: MoodColor? = null

    init {
        getMoodColors(MoodColorOrder.Date(OrderType.Descending))
        savedStateHandle.get<Int>("moodColorId")?.let { moodColorId ->
            if (moodColorId != -1) {
                viewModelScope.launch {
                    moodColorUseCases.getMoodColor(moodColorId)?.also { moodColor ->
                        withContext(Dispatchers.Main) {
                            currentMoodColorId = moodColor.id
                            _moodColorDate.value = moodColorDate.value.copy(
                                date = moodColor.dateStamp,
                            )
                        }
                        _moodColorMood.value = moodColorMood.value.copy(
                            mood = moodColor.mood,
                            isHintVisible = false
                        )
                        _moodColorColor.value = moodColor.color
                    }
                }
            }
        }
    }

    fun onEvent(event: AddEditMoodColorEvent) {
        when (event) {
            is AddEditMoodColorEvent.EnteredDate -> {
                _moodColorDate.value = moodColorDate.value.copy(
                    date = event.date
                )
            }

            is AddEditMoodColorEvent.EnteredMood -> {
                _moodColorMood.value = moodColorMood.value.copy(
                    mood = event.mood
                )
            }

            is AddEditMoodColorEvent.ChangeMoodFocus -> {
                _moodColorMood.value = moodColorMood.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            moodColorMood.value.mood.isBlank()
                )
            }

            is AddEditMoodColorEvent.EnteredColor -> {
                _moodColorColor.value = event.colorEnvelope.hexCode
            }

            is AddEditMoodColorEvent.SaveMoodColor -> {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        moodColorUseCases.addMoodColor(
                            MoodColor(
                                dateStamp = moodColorDate.value.date,
                                mood = moodColorMood.value.mood,
                                color = moodColorColor.value,
                                id = currentMoodColorId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveMoodColor)
                    } catch (e: InvalidMoodColorException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save new mood color"
                            )
                        )
                    }
                }
            }

            is AddEditMoodColorEvent.DeleteMoodColor -> {
                viewModelScope.launch {
                    moodColorUseCases.deleteMoodColor(event.moodColor)
                    recentlyDeletedMoodColor = event.moodColor
                }
            }
        }
    }

    private fun getMoodColors(moodColorOrder: MoodColorOrder) {
        getMoodColorsJob?.cancel()
        getMoodColorsJob = moodColorUseCases.getMoodColors(moodColorOrder)
            .onEach { moodColors ->
                _state.value = state.value.copy(
                    moodColors = moodColors,
                    moodColorOrder = moodColorOrder
                )
            }
            .launchIn(viewModelScope)
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SaveMoodColor : UiEvent()
    }
}