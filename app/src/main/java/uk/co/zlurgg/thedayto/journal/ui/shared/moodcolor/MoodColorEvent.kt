package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorError

sealed interface MoodColorEvent {
    data object ShowUndoSnackbar : MoodColorEvent
    data class ShowError(val error: MoodColorError) : MoodColorEvent
}
