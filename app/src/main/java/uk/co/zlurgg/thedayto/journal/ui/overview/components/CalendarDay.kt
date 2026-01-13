package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToDay
import uk.co.zlurgg.thedayto.journal.ui.util.getColorSafe
import uk.co.zlurgg.thedayto.journal.ui.util.getContrastingTextColor

@Composable
fun CalendarDay(
    entry: EntryWithMoodColor,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp
) {
    val color = getColorSafe(entry.moodColor)
    val dayNumber = datestampToDay(entry.dateStamp)
    val dayDescription = stringResource(
        R.string.calendar_day_with_entry,
        dayNumber,
        entry.moodName
    )
    Box(
        modifier = modifier.semantics { contentDescription = dayDescription }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val clipPath = Path().apply {
                lineTo(size.width, 0f)
                lineTo(size.width, size.width)
                lineTo(size.width, size.height)
                lineTo(0f, size.height)
                close()
            }
            clipPath(clipPath) {
                drawRoundRect(
                    color = color,
                    size = size,
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = color.getContrastingTextColor(),
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalendarDayPreview() {
    TheDayToTheme {
        CalendarDay(
            entry = EntryWithMoodColor(
                id = 1,
                moodColorId = 1,
                moodName = "Happy",
                moodColor = "4CAF50",
                content = "Great day!",
                dateStamp = System.currentTimeMillis()
            ),
            modifier = Modifier.size(48.dp)
        )
    }
}
