package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state

import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor

/**
 * Actions for the Mood Color Management screen.
 * Sealed interface for exhaustive when handling.
 */
sealed interface MoodColorManagementAction {
    data object AddMoodColor : MoodColorManagementAction
    data class EditMoodColor(val moodColor: MoodColor) : MoodColorManagementAction
    data class SaveMoodColor(val moodColor: MoodColor) : MoodColorManagementAction
    data class RequestDeleteMoodColor(val moodColor: MoodColor) : MoodColorManagementAction
    data object ConfirmDelete : MoodColorManagementAction
    data object DismissDeleteDialog : MoodColorManagementAction
    data class ToggleFavorite(val id: Int, val currentValue: Boolean) : MoodColorManagementAction
    data object DismissDialog : MoodColorManagementAction
    data object ClearError : MoodColorManagementAction
}
