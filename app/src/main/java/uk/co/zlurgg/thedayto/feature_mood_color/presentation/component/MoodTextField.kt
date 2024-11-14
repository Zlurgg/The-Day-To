package uk.co.zlurgg.thedayto.feature_mood_color.presentation.component

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.PreviewLightDark
import uk.co.zlurgg.thedayto.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.ui.theme.paddingSmall

@Composable
fun MoodTextField(
    mood: String,
    hint: String,
    modifier: Modifier = Modifier,
    isHintVisible: Boolean = true,
    onValueChange: (String) -> Unit,
    textStyle: TextStyle = TextStyle(),
    singleLine: Boolean = false,
    onFocusChange: (FocusState) -> Unit
) {
    val contentColor = if(isSystemInDarkTheme()) {
        Color.White
    } else {
        Color.Black
    }
    Box(
        modifier = modifier
    ) {
        OutlinedTextField(
            value = mood,
            onValueChange = onValueChange,
            singleLine = singleLine,
            textStyle = textStyle,
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged {
                    onFocusChange(it)
                }
        )
        if (isHintVisible) {
            Text(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(horizontal = paddingSmall),
                text = hint,
                style = textStyle,
                color = contentColor
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun MoodTextFieldPreview() {
    TheDayToTheme {
        MoodTextField(
            mood = "Good",
            hint = "Enter Mood",
            isHintVisible = false,
            onValueChange = {},
            textStyle = TextStyle(),
            singleLine = false,
            onFocusChange = {},
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background),
        )
    }
}