package uk.co.zlurgg.thedayto.journal.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class EntryWithMoodColor(
    val id: Int?,
    val moodColorId: Int,
    val moodName: String,
    val moodColor: String,
    val content: String,
    val dateStamp: Long
)
