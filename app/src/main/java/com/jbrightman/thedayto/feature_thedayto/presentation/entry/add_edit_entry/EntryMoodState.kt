package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry

data class EntryMoodState(
    val mood: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)