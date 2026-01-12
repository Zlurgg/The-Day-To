package uk.co.zlurgg.thedayto.journal.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Entry(
    val moodColorId: Int,
    val content: String,
    val dateStamp: Long,
    val id: Int? = null
)

class InvalidEntryException(message: String) : Exception(message)