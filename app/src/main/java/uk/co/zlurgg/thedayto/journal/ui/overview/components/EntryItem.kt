package uk.co.zlurgg.thedayto.journal.ui.overview.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToFormattedDate
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.ui.util.getColor
import uk.co.zlurgg.thedayto.journal.ui.util.getContrastingTextColor

@Composable
fun EntryItem(
    entry: Entry,
    modifier: Modifier
) {
    val moodColor = getColor(entry.color)
    val textColor = moodColor.getContrastingTextColor()

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = moodColor.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingMedium)
        ) {
            Text(
                text = datestampToFormattedDate(entry.dateStamp),
                style = MaterialTheme.typography.titleLarge,
                color = textColor,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(paddingSmall))
            Text(
                text = entry.mood,
                style = MaterialTheme.typography.titleMedium,
                color = textColor,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(paddingSmall))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = textColor,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}