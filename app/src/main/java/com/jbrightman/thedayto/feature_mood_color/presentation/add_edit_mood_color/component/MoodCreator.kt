package com.jbrightman.thedayto.feature_mood_color.presentation.add_edit_mood_color.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbrightman.thedayto.feature_mood_color.presentation.add_edit_mood_color.AddEditMoodColorEvent
import com.jbrightman.thedayto.feature_mood_color.presentation.add_edit_mood_color.AddEditMoodColorViewModel

@Composable
fun MoodCreator(
    viewModel: AddEditMoodColorViewModel = hiltViewModel()
) {
    val moodState = viewModel.moodColorMood.value

    MoodTextField(
        mood = moodState.mood,
        hint = moodState.hint,
        onValueChange = {
            viewModel.onEvent(AddEditMoodColorEvent.EnteredMood(it))
        },
        onFocusChange = {
            viewModel.onEvent(AddEditMoodColorEvent.ChangeMoodFocus(it))
        },
        isHintVisible = moodState.isHintVisible,
        singleLine = true,
        textStyle = MaterialTheme.typography.headlineSmall
    )
}