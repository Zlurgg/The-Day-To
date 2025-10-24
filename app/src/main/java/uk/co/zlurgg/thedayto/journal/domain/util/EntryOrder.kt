package uk.co.zlurgg.thedayto.journal.domain.util

import uk.co.zlurgg.thedayto.core.domain.util.OrderType

sealed class EntryOrder(val orderType: OrderType) {
    class Date(orderType: OrderType) : EntryOrder(orderType)
    class Mood(orderType: OrderType) : EntryOrder(orderType)

    fun copy(orderType: OrderType): EntryOrder {
        return when (this) {
            is Date -> Date(orderType)
            is Mood -> Mood(orderType)
        }
    }
}