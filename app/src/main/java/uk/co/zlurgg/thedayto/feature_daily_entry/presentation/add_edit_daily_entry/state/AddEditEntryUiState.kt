package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.state

data class AddEditEntryUiState(
    // Entry data
    val entryDate: Long = System.currentTimeMillis(),
    val entryMood: String = "",
    val entryContent: String = "",
    val entryColor: String = "#000000",
    val currentEntryId: Int? = null,

    // UI state
    val isMoodHintVisible: Boolean = true,
    val isContentHintVisible: Boolean = true,
    val isMoodColorSectionVisible: Boolean = false,

    // Hints
    val todayHint: String = "How're you feeling today?",
    val previousDayHint: String = "How're were you feeling that day?",
    val contentHint: String = "Any additional info?",

    // Loading & error states
    val isLoading: Boolean = false,
    val error: String? = null
)
