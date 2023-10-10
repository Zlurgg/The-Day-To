package com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.jbrightman.thedayto.feature_thedayto.domain.repository.mood_color.MoodColorRepository
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.mood_color.MoodColorUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AddEditMoodColorViewModel @Inject constructor(
    private val moodColorUseCases: MoodColorUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

}