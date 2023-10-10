package com.jbrightman.thedayto.feature_thedayto.domain.util.entry

import com.jbrightman.thedayto.feature_thedayto.domain.util.OrderType

sealed class EntryOrder(val orderType: OrderType) {
    class Date(orderType: OrderType): EntryOrder(orderType)
    class Mood(orderType: OrderType): EntryOrder(orderType)

    fun copy(orderType: OrderType): EntryOrder {
        return when(this) {
            is Date -> Date(orderType)
            is Mood -> Mood(orderType)
        }
    }
}