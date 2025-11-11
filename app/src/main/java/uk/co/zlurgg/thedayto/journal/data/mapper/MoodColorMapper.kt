package uk.co.zlurgg.thedayto.journal.data.mapper

import uk.co.zlurgg.thedayto.journal.data.model.MoodColorEntity
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor

fun MoodColorEntity.toDomain(): MoodColor {
    return MoodColor(
        mood = mood,
        color = color,
        isDeleted = isDeleted,
        dateStamp = dateStamp,
        id = id
    )
}

fun MoodColor.toEntity(): MoodColorEntity {
    return MoodColorEntity(
        mood = mood,  // Preserve original case for display
        moodNormalized = mood.trim().lowercase(),  // Normalize for uniqueness
        color = color,
        isDeleted = isDeleted,
        dateStamp = dateStamp,
        id = id
    )
}
