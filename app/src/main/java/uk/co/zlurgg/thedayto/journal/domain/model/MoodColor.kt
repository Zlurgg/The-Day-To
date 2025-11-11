package uk.co.zlurgg.thedayto.journal.domain.model

data class MoodColor(
    val mood: String,
    val color: String,
    val isDeleted: Boolean = false,
    val dateStamp: Long,
    val id: Int = 0
)

class InvalidMoodColorException(message: String) : Exception(message)