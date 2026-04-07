package uk.co.zlurgg.thedayto.journal.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class MoodColorWithCount(
    val moodColor: MoodColor,
    val entryCount: Int
)
