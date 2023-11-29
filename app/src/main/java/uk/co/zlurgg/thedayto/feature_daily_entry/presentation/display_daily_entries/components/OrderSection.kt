package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import uk.co.zlurgg.thedayto.domain.util.DailyEntryOrder
import uk.co.zlurgg.thedayto.domain.util.OrderType
import uk.co.zlurgg.thedayto.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.ui.theme.paddingSmall

@Composable
fun OrderSection(
    modifier: Modifier = Modifier,
    dailyEntryOrder: DailyEntryOrder = DailyEntryOrder.Date(OrderType.Descending),
    onOrderChange: (DailyEntryOrder) -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
             text = "Mood",
             selected = dailyEntryOrder is DailyEntryOrder.Mood,
             onSelect = { onOrderChange(DailyEntryOrder.Mood(dailyEntryOrder.orderType)) }
            )
            Spacer(modifier = Modifier.width(paddingSmall))
            DefaultRadioButton(
             text = "Date",
             selected = dailyEntryOrder is DailyEntryOrder.Date,
             onSelect = { onOrderChange(DailyEntryOrder.Date(dailyEntryOrder.orderType)) }
            )
        }
        Spacer(modifier = Modifier.width(paddingMedium))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = "Ascending",
                selected = dailyEntryOrder.orderType is OrderType.Ascending,
                onSelect = {
                    onOrderChange(dailyEntryOrder.copy(OrderType.Ascending))
                }
            )
            Spacer(modifier = Modifier.width(paddingSmall))
            DefaultRadioButton(
                text = "Descending",
                selected = dailyEntryOrder.orderType is OrderType.Descending,
                onSelect = {
                    onOrderChange(dailyEntryOrder.copy(OrderType.Descending))
                }
            )
        }
        Spacer(modifier = Modifier.height(paddingMedium))
        Row(
            modifier = Modifier.fillMaxWidth().padding(paddingMedium),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onSignOut) {
                Text(text = "Sign out")
            }
        }
    }
}