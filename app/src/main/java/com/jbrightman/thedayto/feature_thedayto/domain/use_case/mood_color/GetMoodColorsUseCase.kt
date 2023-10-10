package com.jbrightman.thedayto.feature_thedayto.domain.use_case.mood_color

import com.jbrightman.thedayto.feature_thedayto.domain.model.mood_color.MoodColor
import com.jbrightman.thedayto.feature_thedayto.domain.repository.mood_color.MoodColorRepository
import com.jbrightman.thedayto.feature_thedayto.domain.util.OrderType
import com.jbrightman.thedayto.feature_thedayto.domain.util.mood_color.MoodColorOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetMoodColorsUseCase (
    private val repository: MoodColorRepository
) {
    operator fun invoke(
        moodColorOrder: MoodColorOrder = MoodColorOrder.Date(OrderType.Descending)
    ): Flow<List<MoodColor>> {
        return repository.getMoodColors().map { moodColors ->
            when (moodColorOrder.orderType) {
                is OrderType.Ascending -> {
                    when (moodColorOrder) {
                        is MoodColorOrder.Mood -> moodColors.sortedBy { it.mood.lowercase() }
                        is MoodColorOrder.Date -> moodColors.sortedBy { it.dateStamp }
                    }
                }
                is OrderType.Descending -> {
                    when (moodColorOrder) {
                        is MoodColorOrder.Mood -> moodColors.sortedByDescending { it.mood.lowercase() }
                        is MoodColorOrder.Date -> moodColors.sortedByDescending { it.dateStamp }
                    }
                }
            }
        }
    }
}