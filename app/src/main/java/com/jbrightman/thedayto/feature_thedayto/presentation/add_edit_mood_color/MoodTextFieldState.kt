package com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color

data class MoodTextFieldState(
    val mood: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)
