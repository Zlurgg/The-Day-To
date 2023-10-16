package com.jbrightman.thedayto.feature_mood_color.presentation

import com.jbrightman.thedayto.domain.util.OrderType
import com.jbrightman.thedayto.feature_mood_color.domain.model.MoodColor
import com.jbrightman.thedayto.feature_mood_color.domain.util.MoodColorOrder

data class MoodColorState(
    val moodColors: List<MoodColor> = emptyList(),
    val moodColorOrder: MoodColorOrder = MoodColorOrder.Date(OrderType.Descending),
)