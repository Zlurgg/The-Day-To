package uk.co.zlurgg.thedayto.journal.ui.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder

@Composable
fun EntrySortSection(
    modifier: Modifier = Modifier,
    entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending),
    onOrderChange: (EntryOrder) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sort by chips
        FilterChip(
            selected = entryOrder is EntryOrder.Date,
            onClick = { onOrderChange(EntryOrder.Date(entryOrder.orderType)) },
            label = { Text(stringResource(R.string.date)) }
        )
        Spacer(modifier = Modifier.width(paddingSmall))
        FilterChip(
            selected = entryOrder is EntryOrder.Mood,
            onClick = { onOrderChange(EntryOrder.Mood(entryOrder.orderType)) },
            label = { Text(stringResource(R.string.mood)) }
        )

        Spacer(modifier = Modifier.width(paddingSmall))

        // Order chips
        FilterChip(
            selected = entryOrder.orderType is OrderType.Ascending,
            onClick = { onOrderChange(entryOrder.copy(OrderType.Ascending)) },
            label = { Text(stringResource(R.string.ascending)) }
        )
        Spacer(modifier = Modifier.width(paddingSmall))
        FilterChip(
            selected = entryOrder.orderType is OrderType.Descending,
            onClick = { onOrderChange(entryOrder.copy(OrderType.Descending)) },
            label = { Text(stringResource(R.string.descending)) }
        )
    }
}
