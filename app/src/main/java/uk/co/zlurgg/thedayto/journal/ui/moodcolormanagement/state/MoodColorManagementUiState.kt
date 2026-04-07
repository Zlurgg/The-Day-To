package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state

import androidx.compose.runtime.Stable
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorWithCount

/**
 * UI state for the Mood Color Management screen.
 * Follows Google's MAD single ViewModel per screen pattern.
 */
@Stable
data class MoodColorManagementUiState(
    val moodColors: List<MoodColorWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val editingMoodColor: MoodColor? = null,
    val dialogError: MoodColorError? = null,
    val pendingDelete: MoodColor? = null,
    val pendingFavorites: Map<Int, Boolean> = emptyMap()
)
