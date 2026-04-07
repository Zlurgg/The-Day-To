package uk.co.zlurgg.thedayto.journal.data.mapper

import uk.co.zlurgg.thedayto.journal.data.model.MoodColorEntity
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.sync.domain.model.toSyncStatusOrDefault

fun MoodColorEntity.toDomain(): MoodColor {
    return MoodColor(
        mood = mood,
        color = color,
        isDeleted = isDeleted,
        isFavorite = isFavorite,
        dateStamp = dateStamp,
        id = id,
        syncId = syncId,
        userId = userId,
        updatedAt = updatedAt,
        syncStatus = syncStatus.toSyncStatusOrDefault()
    )
}

fun MoodColor.toEntity(): MoodColorEntity {
    return MoodColorEntity(
        mood = mood,  // Preserve original case for display
        moodNormalized = mood.trim().lowercase(),  // Normalize for uniqueness
        color = color,
        isDeleted = isDeleted,
        isFavorite = isFavorite,
        dateStamp = dateStamp,
        id = id,
        syncId = syncId,
        userId = userId,
        updatedAt = updatedAt,
        syncStatus = syncStatus.name
    )
}
