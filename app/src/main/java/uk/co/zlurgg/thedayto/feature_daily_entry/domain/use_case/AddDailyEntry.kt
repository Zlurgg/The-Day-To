package uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case

import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.InvalidDailyEntryException
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.repository.DailyEntryRepository
import kotlin.jvm.Throws

class AddDailyEntry(
    private val repository: DailyEntryRepository
) {
    @Throws(InvalidDailyEntryException::class)
    suspend operator fun invoke(entry: DailyEntry) {
        if (entry.dateStamp == 0L) {
            throw InvalidDailyEntryException("The date of the entry must be valid.")
        }
        if (entry.mood.isBlank()) {
            throw InvalidDailyEntryException("The mood of the entry can't be empty.")
        }
        if (entry.color.isBlank()) {
            throw InvalidDailyEntryException("The color of the entry can't be empty.")
        }
        repository.insertDailyEntry(entry)
//        if (repository.getTheDayToEntryByDate(entry.dateStamp) == null) {
//            repository.insertEntry(entry)
//        } else {
//            throw InvalidTheDayToEntryException("Entry already exists for this date.")
//        }
    }
}