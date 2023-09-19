package com.jbrightman.thedayto.feature_thedayto.domain.util

sealed class EntryOrder(val orderType: OrderType) {
    class Title(orderType: OrderType): EntryOrder(orderType)
    class Date(orderType: OrderType): EntryOrder(orderType)
    class Color(orderType: OrderType): EntryOrder(orderType)

    fun copy(orderType: OrderType): EntryOrder {
        return when(this) {
            is Title -> Title(orderType)
            is Date -> Date(orderType)
            is Color -> Color(orderType)
        }
    }
}