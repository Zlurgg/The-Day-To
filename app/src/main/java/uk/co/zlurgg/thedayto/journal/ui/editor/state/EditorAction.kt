package uk.co.zlurgg.thedayto.journal.ui.editor.state

import androidx.compose.ui.focus.FocusState
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor

sealed interface EditorAction {
    data class EnteredDate(val date: Long) : EditorAction
    data class SelectMoodColor(val moodColorId: Int) : EditorAction
    data class EnteredContent(val value: String) : EditorAction
    data class ChangeContentFocus(val focusState: FocusState) : EditorAction
    data object ToggleMoodColorSection : EditorAction
    data class SaveMoodColor(val mood: String, val colorHex: String) : EditorAction
    data class DeleteMoodColor(val moodColor: MoodColor) : EditorAction
    data object DismissEditorTutorial : EditorAction
    data object SaveEntry : EditorAction
}
