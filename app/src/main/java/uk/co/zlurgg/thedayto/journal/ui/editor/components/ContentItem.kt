package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation

/**
 * Pure presenter component for content entry field.
 * No ViewModel dependency - receives all state and callbacks as parameters.
 *
 * Enforces input validation:
 * - Maximum length limit to prevent DoS/memory issues
 * - Character counter to guide users
 * - Visual feedback when approaching limit
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
        maxLength = InputValidation.MAX_CONTENT_LENGTH,
        showCharacterCount = true,
        modifier = modifier
            .fillMaxWidth()
    )
}
