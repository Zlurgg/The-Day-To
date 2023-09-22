package com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jbrightman.thedayto.feature_thedayto.domain.util.EntryOrder
import com.jbrightman.thedayto.feature_thedayto.domain.util.OrderType
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Dimensions

@Composable
fun OrderSection(
    modifier: Modifier = Modifier,
    entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending),
    onOrderChange: (EntryOrder) -> Unit,
    dimensions: Dimensions
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
             text = "Mood",
             selected = entryOrder is EntryOrder.Mood,
             onSelect = { onOrderChange(EntryOrder.Mood(entryOrder.orderType)) }
            )
            Spacer(modifier = Modifier.width(dimensions.paddingSmall))
            DefaultRadioButton(
             text = "Date",
             selected = entryOrder is EntryOrder.Date,
             onSelect = { onOrderChange(EntryOrder.Date(entryOrder.orderType)) }
            )
            Spacer(modifier = Modifier.width(dimensions.paddingSmall))
            DefaultRadioButton(
             text = "Color",
             selected = entryOrder is EntryOrder.Color,
             onSelect = { onOrderChange(EntryOrder.Color(entryOrder.orderType)) }
            )
        }
        Spacer(modifier = Modifier.width(dimensions.paddingMedium))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = "Ascending",
                selected = entryOrder.orderType is OrderType.Ascending,
                onSelect = {
                    onOrderChange(entryOrder.copy(OrderType.Ascending))
                }
            )
            Spacer(modifier = Modifier.width(dimensions.paddingSmall))
            DefaultRadioButton(
                text = "Descending",
                selected = entryOrder.orderType is OrderType.Descending,
                onSelect = {
                    onOrderChange(entryOrder.copy(OrderType.Descending))
                }
            )
        }
    }
}