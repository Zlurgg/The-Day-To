package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.ui.overview.util.UiConstants

@Composable
fun MonthStatistics(
    entries: List<EntryWithMoodColor>,
    daysInMonth: Int,
    modifier: Modifier = Modifier,
    onStatsClick: () -> Unit = {},
) {
    val entryCount = entries.size
    val completionPercentage = if (daysInMonth > 0) {
        (entryCount.toFloat() / daysInMonth * 100).toInt()
    } else 0

    val statsDescription = stringResource(R.string.stats_card_description)

    Card(
        modifier = modifier
            .semantics { contentDescription = statsDescription }
            .clickable(onClick = onStatsClick),
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatItem(
                    label = stringResource(R.string.stats_entries_label),
                    value = entryCount.toString()
                )
                StatItem(
                    label = stringResource(R.string.stats_days_logged_label),
                    value = "$completionPercentage%"
                )
                StatItem(
                    label = stringResource(R.string.stats_days_in_month_label),
                    value = daysInMonth.toString()
                )
            }

            Spacer(modifier = Modifier.width(paddingSmall))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = stringResource(R.string.view_more_stats),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
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
            style = MaterialTheme.typography.labelLarge,
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
                EntryWithMoodColor(id = 1, moodColorId = 1, moodName = "Happy", moodColor = "4CAF50", content = "Great!", dateStamp = 1L),
                EntryWithMoodColor(id = 2, moodColorId = 2, moodName = "Sad", moodColor = "F44336", content = "Not good", dateStamp = 2L),
                EntryWithMoodColor(id = 3, moodColorId = 3, moodName = "Neutral", moodColor = "FFC107", content = "Ok", dateStamp = 3L)
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
