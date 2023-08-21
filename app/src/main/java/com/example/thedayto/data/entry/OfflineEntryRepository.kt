package com.example.thedayto.data.entry

import kotlinx.coroutines.flow.Flow

class OfflineEntryRepository(private val entryDao: EntryDao) : EntryRepository {
    override fun getAllEntriesStream(): Flow<List<Entry>> = entryDao.getAllEntries()

    override fun getEntryStream(id: Int): Flow<Entry?> = entryDao.getEntry(id)

    override suspend fun insertEntry(entry: Entry) = entryDao.insert(entry)

    override suspend fun deleteEntry(entry: Entry) = entryDao.delete(entry)

    override suspend fun updateEntry(entry: Entry) = entryDao.update(entry)

    override suspend fun getEntryFromDateStream(date: String): Flow<Entry?> = entryDao.getEntryFromDate(date)
}