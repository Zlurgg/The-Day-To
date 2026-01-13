package uk.co.zlurgg.thedayto.journal.ui.editor.state

sealed interface EditorUiEvent {
    data class ShowSnackbar(val message: String) : EditorUiEvent
}
