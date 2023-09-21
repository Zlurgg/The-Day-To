package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry

import androidx.compose.ui.focus.FocusState

sealed class AddEditEntryEvent {
    data class EnteredTitle(val value: String): AddEditEntryEvent()
    data class ChangeTitleFocus(val focusState: FocusState): AddEditEntryEvent()
    data class EnteredDate(val date: Long): AddEditEntryEvent()
    data class EnteredMood(val mood: String): AddEditEntryEvent()
    data class EnteredContent(val value: String): AddEditEntryEvent()
    data class ChangeContentFocus(val focusState: FocusState): AddEditEntryEvent()
    data class ChangeColor(val color: Int): AddEditEntryEvent()
    data object SaveEntry: AddEditEntryEvent()
}