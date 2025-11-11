package uk.co.zlurgg.thedayto.journal.data.model

data class EntryWithMoodColorEntity(
    val id: Int?,
    val moodColorId: Int,
    val moodName: String,
    val moodColor: String,
    val content: String,
    val dateStamp: Long
)
