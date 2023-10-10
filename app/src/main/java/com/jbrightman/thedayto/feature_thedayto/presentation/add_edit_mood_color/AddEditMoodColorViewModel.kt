package com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbrightman.thedayto.feature_thedayto.domain.model.mood_color.MoodColor
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.mood_color.MoodColorUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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

    private val _moodColorColor = mutableIntStateOf(MoodColor.defaultColors.random().toArgb())
    val moodColorColor: State<Int> = _moodColorColor

    private var currentMoodColorId: Int? = null

    init {
        savedStateHandle.get<Int>("moodColorId")?.let { moodColorId ->
            if (moodColorId != -1) {
                viewModelScope.launch {
                    moodColorUseCases.getMoodColor(moodColorId)?.also { moodColor ->

                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        object SaveNote : UiEvent()
    }
}