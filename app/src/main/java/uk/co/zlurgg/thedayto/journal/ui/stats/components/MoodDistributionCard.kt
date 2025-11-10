package uk.co.zlurgg.thedayto.journal.ui.stats.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.core.graphics.toColorInt
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.ui.stats.StatsConstants
import uk.co.zlurgg.thedayto.journal.ui.stats.state.StatsUiState

@Composable
fun MoodDistributionCard(
    moodDistribution: List<StatsUiState.MoodCount>,
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
            verticalArrangement = Arrangement.spacedBy(StatsConstants.MOOD_ITEM_SPACING)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Mood,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Most Common Moods",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            if (moodDistribution.isEmpty()) {
                Text(
                    text = "No mood data yet",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                moodDistribution.forEach { moodCount ->
                    MoodDistributionItem(moodCount)
                }
            }
        }
    }
}

@Composable
private fun MoodDistributionItem(moodCount: StatsUiState.MoodCount) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(StatsConstants.MOOD_ITEM_SPACING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(StatsConstants.COLOR_INDICATOR_SIZE)
                    .background(
                        color = try {
                            Color(moodCount.color.toColorInt())
                        } catch (e: IllegalArgumentException) {
                            timber.log.Timber.w(e, "Invalid color format: ${moodCount.color}, falling back to primary")
                            MaterialTheme.colorScheme.primary
                        },
                        shape = CircleShape
                    )
            )
            Text(
                text = moodCount.mood,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Text(
            text = "${moodCount.count}×",
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MoodDistributionCardPreview() {
    TheDayToTheme {
        MoodDistributionCard(
            moodDistribution = listOf(
                StatsUiState.MoodCount("Happy", "#4CAF50", 34),
                StatsUiState.MoodCount("Relaxed", "#2196F3", 28),
                StatsUiState.MoodCount("Neutral", "#FFC107", 19),
                StatsUiState.MoodCount("Sad", "#F44336", 12),
                StatsUiState.MoodCount("Anxious", "#9C27B0", 8)
            )
        )
    }
}

@Preview(name = "Empty State", showBackground = true)
@Composable
private fun MoodDistributionCardEmptyPreview() {
    TheDayToTheme {
        MoodDistributionCard(moodDistribution = emptyList())
    }
}
