package uk.co.zlurgg.thedayto.feature_mood_color.domain.use_case

import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor
import uk.co.zlurgg.thedayto.feature_mood_color.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.feature_mood_color.domain.util.MoodColorOrder
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