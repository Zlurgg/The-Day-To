package uk.co.zlurgg.thedayto.journal.domain.model

data class Entry(
    val mood: String,
    val content: String,
    val dateStamp: Long,
    val color: String,
    val id: Int? = null
)

class InvalidEntryException(message: String) : Exception(message)