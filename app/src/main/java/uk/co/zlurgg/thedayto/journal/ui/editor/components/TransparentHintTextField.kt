package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.core.ui.theme.paddingExtraSmall
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMediumSmall
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall

/**
 * Threshold (as a fraction of maxLength) at which the character counter
 * becomes visible. Below this, the counter is hidden to reduce visual noise —
 * most journal entries are well under the 5000-char limit.
 */
private const val COUNTER_VISIBILITY_THRESHOLD = 0.8f

@Composable
fun TransparentHintTextField(
    text: String,
    hint: String,
    modifier: Modifier = Modifier,
    isHintVisible: Boolean = true,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle = TextStyle(),
    singleLine: Boolean = false,
    onFocusChange: (FocusState) -> Unit,
    maxLength: Int? = null,
    showCharacterCount: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }

    val cornerShape = RoundedCornerShape(paddingMediumSmall)

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = cornerShape,
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    },
                    shape = cornerShape,
                )
                .padding(paddingMedium),
        ) {
            BasicTextField(
                value = text,
                onValueChange = { newValue ->
                    // Enforce max length at UI level if specified
                    if (maxLength == null || newValue.length <= maxLength) {
                        onValueChange(newValue)
                    }
                },
                singleLine = singleLine,
                textStyle = textStyle.copy(color = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier
                    .fillMaxSize()
                    .onFocusChanged { focusState ->
                        isFocused = focusState.isFocused
                        onFocusChange(focusState)
                    },
            )
            if (isHintVisible) {
                Text(
                    text = hint,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        }

        // Character counter — only visible when approaching the limit.
        // Most entries are well under 5000 chars; showing "42/5000" is noise.
        if (showCharacterCount && maxLength != null &&
            text.length > maxLength * COUNTER_VISIBILITY_THRESHOLD
        ) {
            val isAtLimit = text.length >= maxLength

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = paddingExtraSmall, vertical = paddingExtraSmall),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End,
            ) {
                Text(
                    text = "${text.length}/$maxLength",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isAtLimit) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    },
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(paddingSmall),
                        )
                        .padding(horizontal = paddingSmall, vertical = paddingExtraSmall),
                )
            }
        }
    }
}
