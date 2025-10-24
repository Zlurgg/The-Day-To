package uk.co.zlurgg.thedayto.journal.ui.overview.components

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
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall

@Composable
fun OrderSection(
    modifier: Modifier = Modifier,
    entryOrder: EntryOrder = EntryOrder.Date(OrderType.Descending),
    onOrderChange: (EntryOrder) -> Unit,
    onSignOut: () -> Unit
) {
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = stringResource(R.string.mood),
                selected = entryOrder is EntryOrder.Mood,
                onSelect = { onOrderChange(EntryOrder.Mood(entryOrder.orderType)) }
            )
            Spacer(modifier = Modifier.width(paddingSmall))
            DefaultRadioButton(
                text = stringResource(R.string.date),
                selected = entryOrder is EntryOrder.Date,
                onSelect = { onOrderChange(EntryOrder.Date(entryOrder.orderType)) }
            )
        }
        Spacer(modifier = Modifier.width(paddingMedium))
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            DefaultRadioButton(
                text = stringResource(R.string.ascending),
                selected = entryOrder.orderType is OrderType.Ascending,
                onSelect = {
                    onOrderChange(entryOrder.copy(OrderType.Ascending))
                }
            )
            Spacer(modifier = Modifier.width(paddingSmall))
            DefaultRadioButton(
                text = stringResource(R.string.descending),
                selected = entryOrder.orderType is OrderType.Descending,
                onSelect = {
                    onOrderChange(entryOrder.copy(OrderType.Descending))
                }
            )
        }
        Spacer(modifier = Modifier.height(paddingMedium))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingMedium),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onSignOut) {
                Text(text = stringResource(R.string.sign_out))
            }
        }
    }
}