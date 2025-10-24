package uk.co.zlurgg.thedayto.feature_mood_color.presentation.state

import androidx.compose.ui.focus.FocusState
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor

sealed interface AddEditMoodColorAction {
    data class EnteredDate(val date: Long) : AddEditMoodColorAction
    data class EnteredMood(val mood: String) : AddEditMoodColorAction
    data class ChangeMoodFocus(val focusState: FocusState) : AddEditMoodColorAction
    data class EnteredColor(val colorEnvelope: ColorEnvelope) : AddEditMoodColorAction
    data object SaveMoodColor : AddEditMoodColorAction
    data class DeleteMoodColor(val moodColor: MoodColor) : AddEditMoodColorAction
}
