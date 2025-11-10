package uk.co.zlurgg.thedayto.journal.ui.stats.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.ui.stats.state.StatsUiState

@Composable
fun MonthlyBreakdownCard(
    monthlyBreakdown: List<StatsUiState.MonthStats>,
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
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📅 Monthly Breakdown",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (monthlyBreakdown.isEmpty()) {
                Text(
                    text = "No monthly data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                monthlyBreakdown.forEach { monthStats ->
                    MonthStatsItem(monthStats)
                }
            }
        }
    }
}

@Composable
private fun MonthStatsItem(monthStats: StatsUiState.MonthStats) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${monthStats.month} ${monthStats.year}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${monthStats.entryCount} entries (${monthStats.completionRate}%)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LinearProgressIndicator(
            progress = { monthStats.completionRate / 100f },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MonthlyBreakdownCardPreview() {
    TheDayToTheme {
        MonthlyBreakdownCard(
            monthlyBreakdown = listOf(
                StatsUiState.MonthStats("November", 2024, 11, 10, 33),
                StatsUiState.MonthStats("October", 2024, 10, 23, 74),
                StatsUiState.MonthStats("September", 2024, 9, 28, 93),
                StatsUiState.MonthStats("August", 2024, 8, 19, 61)
            )
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun MonthlyBreakdownCardEmptyPreview() {
    TheDayToTheme {
        MonthlyBreakdownCard(monthlyBreakdown = emptyList())
    }
}
