package com.jbrightman.thedayto.feature_mood_color.presentation

import androidx.compose.ui.focus.FocusState
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.jbrightman.thedayto.feature_mood_color.domain.model.MoodColor

sealed class AddEditMoodColorEvent {
    data class EnteredDate(val date: Long): AddEditMoodColorEvent()
    data class EnteredMood(val mood: String): AddEditMoodColorEvent()
    data class ChangeMoodFocus(val focusState: FocusState): AddEditMoodColorEvent()
    data class EnteredColor(val colorEnvelope: ColorEnvelope): AddEditMoodColorEvent()
    data object SaveMoodColor: AddEditMoodColorEvent()
    data class DeleteMoodColor(val moodColor: MoodColor): AddEditMoodColorEvent()
}
