package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation
import android.content.res.Configuration
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme

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

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ContentItemPreview() {
    TheDayToTheme {
        ContentItem(
            content = "Had a wonderful day today!",
            hint = "How was your day?",
            isHintVisible = false,
            onContentChange = {},
            onFocusChange = {}
        )
    }
}

@Preview(name = "Empty with Hint", showBackground = true)
@Composable
private fun ContentItemEmptyPreview() {
    TheDayToTheme {
        ContentItem(
            content = "",
            hint = "How was your day?",
            isHintVisible = true,
            onContentChange = {},
            onFocusChange = {}
        )
    }
}
