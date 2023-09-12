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

    override suspend fun insertNote(entry: TheDayToEntry) {
        return dao.insertEntry(entry)
    }

    override suspend fun deleteNote(entry: TheDayToEntry) {
        return dao.deleteEntry(entry)
    }
}