package uk.co.zlurgg.thedayto.journal.ui.stats.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.ui.stats.StatsConstants
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun TotalStatsCard(
    totalEntries: Int,
    firstEntryDate: LocalDate?,
    averageEntriesPerMonth: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(StatsConstants.CARD_PADDING),
            verticalArrangement = Arrangement.spacedBy(StatsConstants.TOTAL_STATS_ITEM_SPACING)
        ) {
            StatsCardHeader(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = stringResource(R.string.stats_all_time_title)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                StatItem(
                    label = stringResource(R.string.stats_total_entries_label),
                    value = totalEntries.toString()
                )
                StatItem(
                    label = stringResource(R.string.stats_avg_per_month_label),
                    value = String.format(Locale.getDefault(), "%.1f", averageEntriesPerMonth)
                )
            }

            if (firstEntryDate != null) {
                Text(
                    text = stringResource(
                        R.string.stats_first_entry_format,
                        firstEntryDate.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TotalStatsCardPreview() {
    TheDayToTheme {
        TotalStatsCard(
            totalEntries = 127,
            firstEntryDate = LocalDate.of(2024, 1, 15),
            averageEntriesPerMonth = 14.2f
        )
    }
}
