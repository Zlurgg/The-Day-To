package com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry

import com.jbrightman.thedayto.feature_thedayto.domain.model.InvalidTheDayToEntryException
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.repository.TheDayToRepository
import kotlin.jvm.Throws

class AddEntry(
    private val repository: TheDayToRepository
) {
    @Throws(InvalidTheDayToEntryException::class)
    suspend operator fun invoke(entry: TheDayToEntry) {
        if (entry.dateStamp == 0L) {
            throw InvalidTheDayToEntryException("The date of the entry must be valid.")
        }
        if (entry.mood.isBlank()) {
            throw InvalidTheDayToEntryException("The mood of the entry can't be empty.")
        }
        if (entry.content.isBlank()) {
            throw InvalidTheDayToEntryException("The content of the entry can't be empty.")
        }
        repository.insertEntry(entry)
    }
}