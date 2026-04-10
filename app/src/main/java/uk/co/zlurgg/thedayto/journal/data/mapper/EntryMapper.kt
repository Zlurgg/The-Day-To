package uk.co.zlurgg.thedayto.journal.data.mapper

import uk.co.zlurgg.thedayto.journal.data.model.EntryEntity
import uk.co.zlurgg.thedayto.journal.data.model.EntryWithMoodColorEntity
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.sync.domain.model.toSyncStatusOrDefault

fun EntryEntity.toDomain(): Entry {
    return Entry(
        moodColorId = moodColorId,
        content = content,
        dateStamp = dateStamp,
        id = id,
        syncId = syncId,
        userId = userId,
        updatedAt = updatedAt,
        syncStatus = syncStatus.toSyncStatusOrDefault(),
    )
}

fun Entry.toEntity(): EntryEntity {
    return EntryEntity(
        moodColorId = moodColorId,
        content = content,
        dateStamp = dateStamp,
        id = id,
        syncId = syncId,
        userId = userId,
        updatedAt = updatedAt,
        syncStatus = syncStatus.name,
        // isDeleted defaults to false - entries are hard-deleted
    )
}

fun EntryWithMoodColorEntity.toDomain(): EntryWithMoodColor {
    return EntryWithMoodColor(
        id = id,
        moodColorId = moodColorId,
        moodName = moodName,
        moodColor = moodColor,
        content = content,
        dateStamp = dateStamp,
    )
}
