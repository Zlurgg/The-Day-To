package uk.co.zlurgg.thedayto.journal.ui.editor.state

import androidx.compose.ui.focus.FocusState
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor

sealed interface EditorAction {
    data class EnteredDate(val date: Long) : EditorAction
    data class EnteredMood(val mood: String) : EditorAction
    data class ChangeMoodFocus(val focusState: FocusState) : EditorAction
    data class EnteredContent(val value: String) : EditorAction
    data class ChangeContentFocus(val focusState: FocusState) : EditorAction
    data class EnteredColor(val color: String) : EditorAction
    data object ToggleMoodColorSection : EditorAction
    data class SaveMoodColor(val mood: String, val colorHex: String) : EditorAction
    data class DeleteMoodColor(val moodColor: MoodColor) : EditorAction
    data object SaveEntry : EditorAction
}
