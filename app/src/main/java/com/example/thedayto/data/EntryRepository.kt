package com.example.thedayto.data

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class EntryRepository(private val entryDao: EntryDao) {

    val allEntries: Flow<List<JournalEntry>> = entryDao.getEntriesOrderedByDate()

    fun getEntryFromDate(date: String): JournalEntry = entryDao.getEntryFromDate(date)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(journalEntry: JournalEntry) {
        entryDao.insert(journalEntry)
    }

}