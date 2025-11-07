package uk.co.zlurgg.thedayto.journal.ui.editor.state

import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import java.time.LocalDate
import java.time.ZoneOffset

data class EditorUiState(
    // Entry data
    val entryDate: Long = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC),
    val entryMood: String = "",
    val entryContent: String = "",
    val entryColor: String = "",  // Empty string indicates no color selected
    val currentEntryId: Int? = null,

    // Mood colors list (for dropdown)
    val moodColors: List<MoodColor> = emptyList(),

    // UI state
    val isMoodHintVisible: Boolean = true,
    val isContentHintVisible: Boolean = true,
    val isMoodColorSectionVisible: Boolean = false,

    // Hints
    val moodHint: String = "How're you feeling today?",  // Dynamic hint based on date
    val contentHint: String = "Any additional info?",

    // Loading state
    val isLoading: Boolean = false
)
