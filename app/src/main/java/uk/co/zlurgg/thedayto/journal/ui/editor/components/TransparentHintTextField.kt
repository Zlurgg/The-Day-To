package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
    showCharacterCount: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp)
                )
                .border(
                    width = if (isFocused) 2.dp else 1.dp,
                    color = if (isFocused)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(16.dp)
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
                    }
            )
            if (isHintVisible) {
                Text(
                    text = hint,
                    style = textStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            }
        }

        // Character counter - enhanced visibility
        if (showCharacterCount && maxLength != null) {
            val isNearLimit = text.length > maxLength * 0.9
            val isAtLimit = text.length >= maxLength

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                Text(
                    text = "${text.length}/$maxLength",
                    style = MaterialTheme.typography.bodyMedium, // Increased from bodySmall
                    color = when {
                        isAtLimit -> MaterialTheme.colorScheme.error
                        isNearLimit -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    },
                    modifier = Modifier
                        .background(
                            color = if (isNearLimit) {
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                            } else {
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                            },
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        // Spacer to create bottom padding (20% of available space)
        Spacer(modifier = Modifier.weight(0.2f))
    }
}