package com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry

data class EntryMoodState(
    var mood: String = "",
    val todayHint: String = "",
    val previousDayHint: String = "",
    val isHintVisible: Boolean = true
)