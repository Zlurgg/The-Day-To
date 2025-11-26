package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state

import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorOrder

/**
 * UI state for the Mood Color Management screen.
 * Follows Google's MAD single ViewModel per screen pattern.
 */
data class MoodColorManagementUiState(
    val moodColorsWithCount: List<MoodColorWithCount> = emptyList(),
    val sortOrder: MoodColorOrder = MoodColorOrder.Date(OrderType.Descending),
    val isLoading: Boolean = false,
    val loadError: String? = null,
    val recentlyDeletedMoodColor: MoodColor? = null,

    // Dialog state
    val showAddMoodColorDialog: Boolean = false,
    val editingMoodColor: MoodColor? = null
)

/**
 * Wrapper for MoodColor with the count of entries using this mood.
 */
data class MoodColorWithCount(
    val moodColor: MoodColor,
    val entryCount: Int
)
