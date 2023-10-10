package com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color

import androidx.compose.ui.graphics.Color

sealed class AddEditMoodColorEvent {
    data class OnMoodChange(val mood: String): AddEditMoodColorEvent()
    data class OnColorChange(val color: Color): AddEditMoodColorEvent()
    data object OnSaveMoodColorClick: AddEditMoodColorEvent()
}
