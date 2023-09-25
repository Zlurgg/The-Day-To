package com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.presentation.util.datestampToFormattedDay

@Composable
fun CalenderDay(
    entry: TheDayToEntry,
    modifier: Modifier,
    cornerRadius: Dp = 10.dp,
) {
    Box(
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val clipPath = Path().apply {
                lineTo(size.width, 0f)
                lineTo(size.width , size.width)
                lineTo(size.width , size.height)
                lineTo(0f , size.height)
                close()
            }
            clipPath(clipPath) {
                drawRoundRect(
                    color = Color(entry.color),
                    size = size,
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
        }
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = datestampToFormattedDay(entry.dateStamp),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}