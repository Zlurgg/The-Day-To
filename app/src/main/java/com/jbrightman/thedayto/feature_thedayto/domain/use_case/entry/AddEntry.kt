package com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry

import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.InvalidTheDayToEntryException
import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.repository.entry.TheDayToRepository
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
        repository.insertEntry(entry)
//        if (repository.getTheDayToEntryByDate(entry.dateStamp) == null) {
//            repository.insertEntry(entry)
//        } else {
//            throw InvalidTheDayToEntryException("Entry already exists for this date.")
//        }
    }
}