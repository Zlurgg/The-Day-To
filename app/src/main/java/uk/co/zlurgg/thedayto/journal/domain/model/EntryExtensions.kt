package uk.co.zlurgg.thedayto.journal.domain.model

/**
 * Domain model extension functions for Entry conversions.
 *
 * These handle domain-to-domain conversions, keeping separation from data layer mappers.
 */

/**
 * Converts EntryWithMoodColor (read/display model) to Entry (mutation model).
 *
 * Use this when you need to perform mutations (create, update, delete) on an entry
 * that was retrieved for display purposes.
 *
 * @return Entry model suitable for repository mutation operations
 */
fun EntryWithMoodColor.toEntry(): Entry {
    return Entry(
        moodColorId = moodColorId,
        content = content,
        dateStamp = dateStamp,
        id = id
    )
}
