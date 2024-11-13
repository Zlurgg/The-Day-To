package uk.co.zlurgg.thedayto.core.presentation.util

sealed class OrderType {
    data object Ascending : OrderType()
    data object Descending : OrderType()
}
