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
    data class EditMoodColor(val moodColor: MoodColor) : EditorAction
    data class UpdateMoodColor(
        val moodColorId: Int,
        val newMood: String,
        val newColorHex: String
    ) : EditorAction
    data object CloseEditMoodColorDialog : EditorAction
    data object ClearEditMoodColorError : EditorAction
    data object DismissEditorTutorial : EditorAction
    data object ToggleDatePicker : EditorAction
    data object SaveEntry : EditorAction
    data object RequestNavigateBack : EditorAction
    data object ConfirmDiscardChanges : EditorAction
    data object DismissUnsavedChangesDialog : EditorAction
    data object RetryLoadEntry : EditorAction
    data object DismissLoadError : EditorAction
}
