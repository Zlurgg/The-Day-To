package com.jbrightman.thedayto.feature_mood_color.presentation.add_edit_mood_color

import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbrightman.thedayto.feature_mood_color.domain.model.InvalidMoodColorException
import com.jbrightman.thedayto.feature_mood_color.domain.model.MoodColor
import com.jbrightman.thedayto.feature_mood_color.domain.use_case.MoodColorUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddEditMoodColorViewModel @Inject constructor(
    private val moodColorUseCases: MoodColorUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _moodColorDate = mutableStateOf(MoodColorDateFieldState(date = 0L))
    val moodColorDate: State<MoodColorDateFieldState> = _moodColorDate

    private val _moodColorMood = mutableStateOf(MoodTextFieldState(hint = "Enter a new mood"))
    val moodColorMood: State<MoodTextFieldState> = _moodColorMood

    private val _moodColorColor = mutableFloatStateOf(1f)
    val moodColorColor: MutableFloatState = _moodColorColor

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentMoodColorId: Int? = null

    init {
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
                println("color: ${event.color}")
                println("value: ${event.color.component1()}")
                _moodColorColor.value = event.color.component1()
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
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SaveMoodColor : UiEvent()
    }
}