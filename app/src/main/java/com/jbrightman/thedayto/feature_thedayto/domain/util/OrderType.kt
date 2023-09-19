package com.jbrightman.thedayto.feature_thedayto.domain.util

sealed class OrderType {
    data object Ascending: OrderType()
    data object Descending: OrderType()
}
