package uk.co.zlurgg.thedayto.journal.ui.editor.state

sealed interface EditorUiEvent {
    data class ShowSnackbar(val message: String) : EditorUiEvent
    data object SaveEntry : EditorUiEvent
    data object NavigateBack : EditorUiEvent
}
