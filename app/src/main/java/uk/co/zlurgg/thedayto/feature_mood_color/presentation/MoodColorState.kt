package uk.co.zlurgg.thedayto.feature_mood_color.presentation

import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.util.MoodColorOrder

data class MoodColorState(
    val moodColors: List<MoodColor> = emptyList(),
    val moodColorOrder: MoodColorOrder = MoodColorOrder.Date(OrderType.Descending),
)