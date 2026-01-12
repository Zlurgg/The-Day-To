package uk.co.zlurgg.thedayto.journal.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class MoodColor(
    val mood: String,
    val color: String,
    val isDeleted: Boolean = false,
    val dateStamp: Long,
    val id: Int? = null
)

class InvalidMoodColorException(message: String) : Exception(message)