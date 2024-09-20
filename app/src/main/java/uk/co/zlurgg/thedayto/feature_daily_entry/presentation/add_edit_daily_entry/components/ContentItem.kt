package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components

import android.util.Log
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryEvent
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryViewModel

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
        textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.primary),
        modifier = Modifier.fillMaxHeight()
    )
}

