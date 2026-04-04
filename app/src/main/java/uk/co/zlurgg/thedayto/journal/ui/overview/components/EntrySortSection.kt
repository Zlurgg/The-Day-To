package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder

@Composable
fun EntrySortSection(
    modifier: Modifier = Modifier,
    entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending),
    onOrderChange: (EntryOrder) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isAscending = entryOrder.orderType is OrderType.Ascending

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sort by chips
        FilterChip(
            selected = entryOrder is EntryOrder.Date,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onOrderChange(EntryOrder.Date(entryOrder.orderType))
            },
            label = { Text(stringResource(R.string.date)) }
        )
        Spacer(modifier = Modifier.width(paddingSmall))
        FilterChip(
            selected = entryOrder is EntryOrder.Mood,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onOrderChange(EntryOrder.Mood(entryOrder.orderType))
            },
            label = { Text(stringResource(R.string.mood)) }
        )

        Spacer(modifier = Modifier.weight(1f))

        // Sort direction toggle
        IconButton(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                val newOrderType = if (isAscending) OrderType.Descending else OrderType.Ascending
                onOrderChange(entryOrder.copy(newOrderType))
            }
        ) {
            Icon(
                imageVector = if (isAscending) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                contentDescription = stringResource(
                    if (isAscending) R.string.sort_descending else R.string.sort_ascending
                )
            )
        }
    }
}

@Preview(name = "Light Mode - Date Descending", showBackground = true)
@Preview(name = "Dark Mode - Date Descending", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EntrySortSectionDatePreview() {
    TheDayToTheme {
        EntrySortSection(
            entryOrder = EntryOrder.Date(OrderType.Descending),
            onOrderChange = {}
        )
    }
}

@Preview(name = "Mood Ascending", showBackground = true)
@Composable
private fun EntrySortSectionMoodPreview() {
    TheDayToTheme {
        EntrySortSection(
            entryOrder = EntryOrder.Mood(OrderType.Ascending),
            onOrderChange = {}
        )
    }
}
