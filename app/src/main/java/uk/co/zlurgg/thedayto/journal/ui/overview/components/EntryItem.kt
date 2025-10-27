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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.core.ui.util.datestampToFormattedDate
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall

@Composable
fun EntryItem(
    entry: Entry,
    modifier: Modifier
) {
    val moodColor = getColor(entry.color)

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
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(paddingSmall))
            Text(
                text = entry.mood,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(paddingSmall))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


// Conversion method for getting color from Colour env
private fun getColor(colorString: String): Color {
    return Color(android.graphics.Color.parseColor("#$colorString"))
}