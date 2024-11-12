package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryEvent
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryViewModel

@Composable
fun ContentItem(
    viewModel: AddEditEntryViewModel = koinViewModel()
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

