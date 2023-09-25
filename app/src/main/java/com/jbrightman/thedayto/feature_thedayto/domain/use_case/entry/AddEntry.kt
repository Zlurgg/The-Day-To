package com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry

import com.jbrightman.thedayto.feature_thedayto.domain.model.InvalidTheDayToEntryException
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.repository.TheDayToRepository
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
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
        if (repository.getTheDayToEntryByDate(entry.dateStamp) == null) {
            repository.insertEntry(entry)
        } else {
            throw InvalidTheDayToEntryException("Entry already exists for this date.")
        }
    }
}