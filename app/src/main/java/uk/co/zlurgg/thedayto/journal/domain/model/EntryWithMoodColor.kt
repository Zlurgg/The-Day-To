package uk.co.zlurgg.thedayto.journal.domain.model

data class EntryWithMoodColor(
    val id: Int?,
    val moodColorId: Int,
    val moodName: String,
    val moodColor: String,
    val content: String,
    val dateStamp: Long
)
