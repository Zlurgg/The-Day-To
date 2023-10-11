package com.jbrightman.thedayto.feature_mood_color.presentation.add_edit_mood_color

data class MoodTextFieldState(
    val mood: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)
