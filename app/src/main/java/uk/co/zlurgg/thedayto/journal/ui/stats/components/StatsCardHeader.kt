package uk.co.zlurgg.thedayto.journal.ui.stats.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import uk.co.zlurgg.thedayto.R
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange

/**
 * Reusable header for stats cards
 *
 * Displays an icon and title with consistent styling used across
 * all stats cards (TotalStats, MoodDistribution, MonthlyBreakdown).
 *
 * @param icon Icon to display before the title
 * @param title Card title text
 * @param modifier Optional modifier for the row
 */
@Composable
fun StatsCardHeader(
    icon: ImageVector,
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.stats_card_header_icon),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun StatsCardHeaderPreview() {
    TheDayToTheme {
        StatsCardHeader(
            icon = Icons.Default.DateRange,
            title = "Monthly Stats"
        )
    }
}
