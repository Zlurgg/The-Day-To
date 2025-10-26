package uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorOrder
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

class GetMoodColorsUseCase(
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