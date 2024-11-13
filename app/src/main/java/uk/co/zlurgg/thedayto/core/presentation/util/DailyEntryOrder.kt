package uk.co.zlurgg.thedayto.core.presentation.util

sealed class DailyEntryOrder(val orderType: OrderType) {
    class Date(orderType: OrderType) : DailyEntryOrder(orderType)
    class Mood(orderType: OrderType) : DailyEntryOrder(orderType)

    fun copy(orderType: OrderType): DailyEntryOrder {
        return when (this) {
            is Date -> Date(orderType)
            is Mood -> Mood(orderType)
        }
    }
}