package uk.co.zlurgg.thedayto.feature_daily_entry.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry

interface DailyEntryRepository {
    fun getDailyEntries(): Flow<List<DailyEntry>>
    suspend fun getDailyEntryById(id: Int): DailyEntry?
    suspend fun getDailyEntryByDate(date: Long): DailyEntry?
    suspend fun insertDailyEntry(entry: DailyEntry)
    suspend fun deleteDailyEntry(entry: DailyEntry)
    suspend fun updateDailyEntry(entry: DailyEntry)

}