package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState

/**
 * Pure presenter component for content entry field.
 * No ViewModel dependency - receives all state and callbacks as parameters.
 *
 * @param content The current content text
 * @param hint Hint text to display when field is empty
 * @param isHintVisible Whether to show the hint
 * @param onContentChange Callback when content text changes
 * @param onFocusChange Callback when focus state changes
 * @param modifier Optional modifier
 */
@Composable
fun ContentItem(
    content: String,
    hint: String,
    isHintVisible: Boolean,
    onContentChange: (String) -> Unit,
    onFocusChange: (FocusState) -> Unit,
    modifier: Modifier = Modifier
) {
    TransparentHintTextField(
        text = content,
        hint = hint,
        onValueChange = onContentChange,
        onFocusChange = onFocusChange,
        isHintVisible = isHintVisible,
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = modifier.fillMaxHeight()
    )
}
