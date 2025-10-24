package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.state

sealed interface AddEditEntryUiEvent {
    data class ShowSnackbar(val message: String) : AddEditEntryUiEvent
    data object SaveEntry : AddEditEntryUiEvent
}
