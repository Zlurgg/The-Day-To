package uk.co.zlurgg.thedayto.journal.ui.editor.state

import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import java.time.LocalDate
import java.time.ZoneOffset

data class EditorUiState(
    // Entry data
    val entryDate: Long = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC),
    val selectedMoodColorId: Int? = null,  // null indicates no mood selected
    val entryContent: String = "",
    val currentEntryId: Int? = null,

    // Mood colors list (for dropdown)
    val moodColors: List<MoodColor> = emptyList(),

    // UI state
    val isMoodHintVisible: Boolean = true,
    val isContentHintVisible: Boolean = true,
    val isMoodColorSectionVisible: Boolean = false,
    val showEditorTutorial: Boolean = false,
    val showDatePicker: Boolean = false,

    // Edit mood color dialog state
    val showEditMoodColorDialog: Boolean = false,
    val editingMoodColor: MoodColor? = null,

    // Hints
    val moodHint: String = "How're you feeling today?",  // Dynamic hint based on date
    val contentHint: String = "Any additional info?",

    // Loading state
    val isLoading: Boolean = false
) {
    /**
     * Indicates whether the entry can be saved.
     * Save is only allowed when a mood color has been selected.
     */
    val canSave: Boolean
        get() = selectedMoodColorId != null
}
