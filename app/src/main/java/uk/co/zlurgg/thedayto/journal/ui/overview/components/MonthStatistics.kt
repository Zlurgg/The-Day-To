package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.ui.overview.util.UiConstants

@Composable
fun MonthStatistics(
    entries: List<Entry>,
    daysInMonth: Int,
    modifier: Modifier = Modifier,
    onStatsClick: () -> Unit = {},
) {
    val entryCount = entries.size
    val completionPercentage = if (daysInMonth > 0) {
        (entryCount.toFloat() / daysInMonth * 100).toInt()
    } else 0

    Card(
        modifier = modifier.clickable { onStatsClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = UiConstants.STATS_CARD_ELEVATION
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = UiConstants.STATS_CARD_PADDING_HORIZONTAL,
                    vertical = UiConstants.STATS_CARD_PADDING_VERTICAL
                ),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatItem(
                label = "Entries",
                value = entryCount.toString()
            )
            StatItem(
                label = "Days Logged",
                value = "$completionPercentage%"
            )
            StatItem(
                label = "Days in Month",
                value = daysInMonth.toString()
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MonthStatisticsPreview() {
    TheDayToTheme {
        MonthStatistics(
            entries = listOf(
                Entry(mood = "Happy", content = "Great!", dateStamp = 1L, color = "#4CAF50", id = 1),
                Entry(mood = "Sad", content = "Not good", dateStamp = 2L, color = "#F44336", id = 2),
                Entry(mood = "Neutral", content = "Ok", dateStamp = 3L, color = "#FFC107", id = 3)
            ),
            daysInMonth = 31
        )
    }
}

@Preview(name = "Empty Stats", showBackground = true)
@Composable
private fun MonthStatisticsEmptyPreview() {
    TheDayToTheme {
        MonthStatistics(
            entries = emptyList(),
            daysInMonth = 30
        )
    }
}
