package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry

data class EntryMoodState(
    var mood: String = "",
    val todayHint: String = "",
    val previousDayHint: String = "",
    val isHintVisible: Boolean = true,
)