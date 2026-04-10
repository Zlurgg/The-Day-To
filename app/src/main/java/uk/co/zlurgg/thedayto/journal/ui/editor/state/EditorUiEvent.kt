package uk.co.zlurgg.thedayto.journal.ui.editor.state

import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError

sealed interface EditorUiEvent {
    data class ShowSnackbar(val message: String) : EditorUiEvent
    data class ShowMoodColorError(val error: MoodColorError) : EditorUiEvent
}
