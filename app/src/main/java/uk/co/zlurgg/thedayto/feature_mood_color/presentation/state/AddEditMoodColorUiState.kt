package uk.co.zlurgg.thedayto.feature_mood_color.presentation.state

import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.util.MoodColorOrder

data class AddEditMoodColorUiState(
    // Mood color data
    val date: Long = 0L,
    val mood: String = "",
    val color: String = "#000000",
    val currentMoodColorId: Int? = null,

    // All mood colors list
    val moodColors: List<MoodColor> = emptyList(),
    val moodColorOrder: MoodColorOrder = MoodColorOrder.Date(OrderType.Descending),
    val recentlyDeletedMoodColor: MoodColor? = null,

    // UI state
    val isMoodHintVisible: Boolean = true,
    val hint: String = "Enter a new mood",

    // Loading & error states
    val isLoading: Boolean = false,
    val error: String? = null
)
