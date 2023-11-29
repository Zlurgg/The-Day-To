package uk.co.zlurgg.thedayto.feature_daily_entry.data.repository

import uk.co.zlurgg.thedayto.feature_daily_entry.data.data_source.DailyEntryDao
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.repository.DailyEntryRepository
import kotlinx.coroutines.flow.Flow

class DailyEntryRepositoryImpl (
    private val dao: DailyEntryDao
): DailyEntryRepository {
    override fun getDailyEntries(): Flow<List<DailyEntry>> {
        return dao.getDailyEntries()
    }

    override suspend fun getDailyEntryById(id: Int): DailyEntry? {
        return dao.getDailyEntryById(id)
    }

    override suspend fun getDailyEntryByDate(date: Long): DailyEntry? {
        return dao.getDailyEntryByDate(date)
    }

    override suspend fun insertDailyEntry(entry: DailyEntry) {
        return dao.insertDailyEntry(entry)
    }

    override suspend fun deleteDailyEntry(entry: DailyEntry) {
        return dao.deleteDailyEntry(entry)
    }

    override suspend fun updateDailyEntry(entry: DailyEntry) {
        return dao.updateDailyEntry(entry)
    }
}