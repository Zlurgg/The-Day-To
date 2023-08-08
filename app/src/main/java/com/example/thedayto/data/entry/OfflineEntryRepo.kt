package com.example.thedayto.data.entry

import kotlinx.coroutines.flow.Flow

class OfflineEntryRepo(private val entryDao: EntryDao) : EntryRepo {
    override fun getAllEntriesStream(): Flow<List<Entry>> = entryDao.getAllEntries()

    override fun getEntryStream(id: Int): Flow<Entry?> = entryDao.getEntry(id)

    override suspend fun insertEntry(entry: Entry) = entryDao.insert(entry)

    override suspend fun deleteEntry(entry: Entry) = entryDao.delete(entry)

    override suspend fun updateEntry(entry: Entry) = entryDao.update(entry)
}