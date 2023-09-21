package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry

data class EntryTextFieldState(
    val text: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)