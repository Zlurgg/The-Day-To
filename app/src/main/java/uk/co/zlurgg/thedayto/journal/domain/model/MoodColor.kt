package uk.co.zlurgg.thedayto.journal.domain.model

data class MoodColor(
    val mood: String,
    val color: String,
    val dateStamp: Long,
    val id: Int? = null
)

class InvalidMoodColorException(message: String) : Exception(message)