package uk.co.zlurgg.thedayto.journal.data.mapper

import uk.co.zlurgg.thedayto.journal.data.model.EntryEntity
import uk.co.zlurgg.thedayto.journal.domain.model.Entry

fun EntryEntity.toDomain(): Entry {
    return Entry(
        mood = mood,
        content = content,
        dateStamp = dateStamp,
        color = color,
        id = id
    )
}

fun Entry.toEntity(): EntryEntity {
    return EntryEntity(
        mood = mood,
        content = content,
        dateStamp = dateStamp,
        color = color,
        id = id
    )
}
