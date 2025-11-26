package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state

/**
 * One-time UI events for the Mood Color Management screen.
 * Used with SharedFlow for events that should only be consumed once.
 */
sealed interface MoodColorManagementUiEvent {
    data class ShowSnackbar(val message: String, val actionLabel: String? = null) : MoodColorManagementUiEvent
}
