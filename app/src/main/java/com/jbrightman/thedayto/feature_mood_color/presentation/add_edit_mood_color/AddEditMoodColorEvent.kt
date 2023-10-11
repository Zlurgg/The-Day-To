package com.jbrightman.thedayto.feature_mood_color.presentation.add_edit_mood_color

import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.graphics.Color

sealed class AddEditMoodColorEvent {
    data class EnteredDate(val date: Long): AddEditMoodColorEvent()
    data class EnteredMood(val mood: String): AddEditMoodColorEvent()
    data class ChangeMoodFocus(val focusState: FocusState): AddEditMoodColorEvent()
    data class EnteredColor(val color: Color): AddEditMoodColorEvent()
    data object SaveMoodColor: AddEditMoodColorEvent()
}
