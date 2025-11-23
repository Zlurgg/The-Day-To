package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.ui.overview.util.UiConstants
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToFormattedDate
import uk.co.zlurgg.thedayto.journal.ui.util.getColor
import uk.co.zlurgg.thedayto.journal.ui.util.getContrastingTextColor

@Composable
fun EntryItem(
    entry: EntryWithMoodColor,
    modifier: Modifier = Modifier
) {
    val moodColor = getColor(entry.moodColor)
    val textColor = moodColor.getContrastingTextColor()

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(
            defaultElevation = UiConstants.ENTRY_CARD_ELEVATION_DEFAULT,
            pressedElevation = UiConstants.ENTRY_CARD_ELEVATION_PRESSED
        ),
        colors = CardDefaults.cardColors(
            containerColor = moodColor.copy(alpha = 0.9f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(UiConstants.ENTRY_CARD_PADDING),
            verticalArrangement = Arrangement.spacedBy(UiConstants.ENTRY_ITEM_SPACING)
        ) {
            // Header: Mood (left) + Date (right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = entry.moodName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = datestampToFormattedDate(entry.dateStamp),
                    style = MaterialTheme.typography.labelLarge,
                    color = textColor.copy(alpha = 0.8f),
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Content preview (only if not blank)
            if (entry.content.isNotBlank()) {
                Text(
                    text = entry.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EntryItemPreview() {
    TheDayToTheme {
        EntryItem(
            entry = EntryWithMoodColor(
                id = 1,
                moodColorId = 1,
                moodName = "Happy",
                moodColor = "4CAF50",
                content = "Had a wonderful day with family and friends. Everything went well!",
                dateStamp = System.currentTimeMillis()
            )
        )
    }
}

@Preview(name = "No Content", showBackground = true)
@Composable
private fun EntryItemNoContentPreview() {
    TheDayToTheme {
        EntryItem(
            entry = EntryWithMoodColor(
                id = 2,
                moodColorId = 2,
                moodName = "Tired",
                moodColor = "9C27B0",
                content = "",
                dateStamp = System.currentTimeMillis()
            )
        )
    }
}