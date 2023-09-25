package com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.graphics.ColorUtils
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.presentation.util.datestampToFormattedDate
import com.jbrightman.thedayto.ui.theme.paddingLarge
import com.jbrightman.thedayto.ui.theme.paddingMedium
import com.jbrightman.thedayto.ui.theme.paddingSmall

@Composable
fun EntryItem(
    entry: TheDayToEntry,
    modifier: Modifier,
    cornerRadius: Dp = 10.dp,
    cutCornerSize: Dp = 30.dp,
    onDeleteClick: () -> Unit
) {
    Box(
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val clipPath = Path().apply {
                lineTo(size.width - cutCornerSize.toPx(), 0f)
                lineTo(size.width , cutCornerSize.toPx())
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
            clipPath(clipPath) {
                drawRoundRect(
                    color = Color(
                        ColorUtils.blendARGB(entry.color, 0x000000, 0.3f)
                    ),
                    topLeft = Offset(size.width - cutCornerSize.toPx(), -100f),
                    size = Size(cutCornerSize.toPx() + 100f, cutCornerSize.toPx() + 100f),
                    cornerRadius = CornerRadius(cornerRadius.toPx())
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingMedium)
                .padding(end = paddingLarge)
        ) {
            Text(
                text = datestampToFormattedDate(entry.dateStamp),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(paddingSmall))
            Text(
                text = entry.mood,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(paddingSmall))
            Text(
                text = entry.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete Entry"
            )
        }
    }
}