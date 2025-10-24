package uk.co.zlurgg.thedayto.feature_mood_color.presentation.state

sealed interface AddEditMoodColorUiEvent {
    data class ShowSnackbar(val message: String) : AddEditMoodColorUiEvent
    data object SaveMoodColor : AddEditMoodColorUiEvent
}
