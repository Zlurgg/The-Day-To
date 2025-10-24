package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.state

import androidx.compose.ui.focus.FocusState

sealed interface AddEditEntryAction {
    data class EnteredDate(val date: Long) : AddEditEntryAction
    data class EnteredMood(val mood: String) : AddEditEntryAction
    data class ChangeMoodFocus(val focusState: FocusState) : AddEditEntryAction
    data class EnteredContent(val value: String) : AddEditEntryAction
    data class ChangeContentFocus(val focusState: FocusState) : AddEditEntryAction
    data class EnteredColor(val color: String) : AddEditEntryAction
    data object ToggleMoodColorSection : AddEditEntryAction
    data class SaveMoodColor(val mood: String, val colorHex: String) : AddEditEntryAction
    data object SaveEntry : AddEditEntryAction
}
