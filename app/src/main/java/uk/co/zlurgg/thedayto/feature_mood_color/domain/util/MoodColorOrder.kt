package uk.co.zlurgg.thedayto.feature_mood_color.domain.util

import uk.co.zlurgg.thedayto.core.domain.util.OrderType

sealed class MoodColorOrder(val orderType: OrderType) {
    class Date(orderType: OrderType) : MoodColorOrder(orderType)
    class Mood(orderType: OrderType) : MoodColorOrder(orderType)

    fun copy(orderType: OrderType): MoodColorOrder {
        return when (this) {
            is Date -> Date(orderType)
            is Mood -> Mood(orderType)
        }
    }
}