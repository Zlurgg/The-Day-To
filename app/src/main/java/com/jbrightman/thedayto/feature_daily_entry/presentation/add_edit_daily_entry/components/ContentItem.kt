package com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryViewModel

@Composable
fun ContentItem(
    viewModel: AddEditEntryViewModel = hiltViewModel()
) {
    val contentState = viewModel.entryContent.value

    TransparentHintTextField(
        text = contentState.text,
        hint = contentState.hint,
        onValueChange = {
            viewModel.onEvent(com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryEvent.EnteredContent(it))
        },
        onFocusChange = {
            viewModel.onEvent(com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryEvent.ChangeContentFocus(it))
        },
        isHintVisible = contentState.isHintVisible,
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxHeight()
    )
}