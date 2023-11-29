package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry

data class EntryContentFieldState(
    val text: String = "",
    val hint: String = "",
    val isHintVisible: Boolean = true
)