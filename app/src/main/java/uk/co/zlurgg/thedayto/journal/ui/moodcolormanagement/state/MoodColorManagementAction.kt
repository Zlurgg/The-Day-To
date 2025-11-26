package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state

import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorOrder

/**
 * Actions for the Mood Color Management screen.
 * Sealed interface for exhaustive when handling.
 */
sealed interface MoodColorManagementAction {
    // Sorting
    data class ToggleSortOrder(val order: MoodColorOrder) : MoodColorManagementAction

    // CRUD operations
    data class DeleteMoodColor(val moodColor: MoodColor) : MoodColorManagementAction
    data object RestoreMoodColor : MoodColorManagementAction
    data object ClearRecentlyDeleted : MoodColorManagementAction

    // Add dialog actions
    data object ShowAddMoodColorDialog : MoodColorManagementAction
    data object DismissAddMoodColorDialog : MoodColorManagementAction
    data class SaveNewMoodColor(val mood: String, val colorHex: String) : MoodColorManagementAction

    // Edit dialog actions
    data class ShowEditMoodColorDialog(val moodColor: MoodColor) : MoodColorManagementAction
    data object DismissEditMoodColorDialog : MoodColorManagementAction
    data class SaveEditedMoodColor(val moodColorId: Int, val newColorHex: String) : MoodColorManagementAction

    // Error handling
    data object RetryLoadMoodColors : MoodColorManagementAction
    data object DismissLoadError : MoodColorManagementAction
}
