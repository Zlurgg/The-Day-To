package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.ui.overview.util.CalendarConstants

@Composable
fun DayOfWeekHeader(
    daySize: Dp,
    modifier: Modifier = Modifier
) {
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = CalendarConstants.CALENDAR_HORIZONTAL_PADDING,
                end = CalendarConstants.CALENDAR_HORIZONTAL_PADDING,
                top = 0.dp,
                bottom = CalendarConstants.DAY_HEADER_BOTTOM_PADDING
            ),
        horizontalArrangement = Arrangement.spacedBy(CalendarConstants.CALENDAR_DAY_SPACING)
    ) {
        daysOfWeek.forEach { day ->
            Box(
                modifier = Modifier.size(daySize),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DayOfWeekHeaderPreview() {
    TheDayToTheme {
        DayOfWeekHeader(daySize = 48.dp)
    }
}
