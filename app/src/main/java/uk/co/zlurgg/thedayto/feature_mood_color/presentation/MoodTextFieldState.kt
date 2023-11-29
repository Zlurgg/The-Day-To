package uk.co.zlurgg.thedayto.feature_mood_color.presentation

data class MoodTextFieldState(
    val mood: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)
