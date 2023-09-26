package com.jbrightman.thedayto.feature_thedayto.data.repository

import com.jbrightman.thedayto.feature_thedayto.data.data_source.TheDayToDao
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.repository.TheDayToRepository
import kotlinx.coroutines.flow.Flow

class TheDayToRepositoryImpl (
    private val dao: TheDayToDao
): TheDayToRepository {
    override fun getTheDayToEntries(): Flow<List<TheDayToEntry>> {
        return dao.getTheDayToEntries()
    }

    override suspend fun getTheDayToEntryById(id: Int): TheDayToEntry? {
        return dao.getTheDayToEntryById(id)
    }

    override suspend fun getTheDayToEntryByDate(date: Long): TheDayToEntry? {
        return dao.getTheDayToEntryByDate(date)
    }

    override suspend fun insertEntry(entry: TheDayToEntry) {
        return dao.insertEntry(entry)
    }

    override suspend fun deleteEntry(entry: TheDayToEntry) {
        return dao.deleteEntry(entry)
    }

    override suspend fun updateEntry(entry: TheDayToEntry) {
        return dao.updateEntry(entry)
    }
}