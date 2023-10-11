package com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.jbrightman.thedayto.ui.theme.paddingSmall

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
                color = Color.DarkGray
            )
        }
    }
}