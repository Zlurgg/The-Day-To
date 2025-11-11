package uk.co.zlurgg.thedayto.journal.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.EntryRepository

class EntryRepositoryImpl(
    private val dao: EntryDao
) : EntryRepository {
    override fun getEntries(): Flow<List<Entry>> {
        return dao.getEntries().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getEntriesWithMoodColors(): Flow<List<EntryWithMoodColor>> {
        return dao.getEntriesWithMoodColors().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getEntryById(id: Int): Entry? {
        return dao.getEntryById(id)?.toDomain()
    }

    override suspend fun getEntryWithMoodColorById(id: Int): EntryWithMoodColor? {
        return dao.getEntryWithMoodColorById(id)?.toDomain()
    }

    override suspend fun getEntryByDate(date: Long): Entry? {
        return dao.getEntryByDate(date)?.toDomain()
    }

    override suspend fun getEntryWithMoodColorByDate(date: Long): EntryWithMoodColor? {
        return dao.getEntryWithMoodColorByDate(date)?.toDomain()
    }

    override suspend fun insertEntry(entry: Entry) {
        return dao.insertEntry(entry.toEntity())
    }

    override suspend fun deleteEntry(entry: Entry) {
        return dao.deleteEntry(entry.toEntity())
    }

    override suspend fun updateEntry(entry: Entry) {
        return dao.updateEntry(entry.toEntity())
    }
}