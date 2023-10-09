package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry

import androidx.compose.ui.focus.FocusState

sealed class AddEditEntryEvent {
    data class EnteredDate(val date: Long): AddEditEntryEvent()
    data class EnteredMood(val mood: String): AddEditEntryEvent()
    data class ChangeMoodFocus(val focusState: FocusState): AddEditEntryEvent()
    data class EnteredContent(val value: String): AddEditEntryEvent()
    data class ChangeContentFocus(val focusState: FocusState): AddEditEntryEvent()
    data object SaveEntry: AddEditEntryEvent()
}
