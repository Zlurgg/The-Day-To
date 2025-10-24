package uk.co.zlurgg.thedayto.journal.domain.usecases.entry

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidEntryException
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

class AddEntryUseCase(
    private val repository: EntryRepository
) {
    @Throws(InvalidEntryException::class)
    suspend operator fun invoke(entry: Entry) {
        if (entry.dateStamp == 0L) {
            throw InvalidEntryException("The date of the entry must be valid.")
        }
        if (entry.mood.isBlank()) {
            throw InvalidEntryException("The mood of the entry can't be empty.")
        }
        if (entry.color.isBlank()) {
            throw InvalidEntryException("The color of the entry can't be empty.")
        }
        repository.insertEntry(entry)
//        if (repository.getTheDayToEntryByDate(entry.dateStamp) == null) {
//            repository.insertEntry(entry)
//        } else {
//            throw InvalidTheDayToEntryException("Entry already exists for this date.")
//        }
    }
}