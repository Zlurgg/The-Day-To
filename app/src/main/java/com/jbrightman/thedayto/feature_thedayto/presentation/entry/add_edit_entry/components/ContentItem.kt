package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryEvent
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryViewModel

@Composable
fun ContentItem(
    viewModel: AddEditEntryViewModel = hiltViewModel()
) {
    val contentState = viewModel.entryContent.value

    TransparentHintTextField(
        text = contentState.text,
        hint = contentState.hint,
        onValueChange = {
            viewModel.onEvent(AddEditEntryEvent.EnteredContent(it))
        },
        onFocusChange = {
            viewModel.onEvent(AddEditEntryEvent.ChangeContentFocus(it))
        },
        isHintVisible = contentState.isHintVisible,
        textStyle = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.fillMaxHeight()
    )
}