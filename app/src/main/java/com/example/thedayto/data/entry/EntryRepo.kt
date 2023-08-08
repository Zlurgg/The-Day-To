package com.example.thedayto.data.entry

import kotlinx.coroutines.flow.Flow

interface EntryRepo {
    /**
     * Retrieve all the entries from the the given data source.
     */
    fun getAllEntriesStream(): Flow<List<Entry>>

    /**
     * Retrieve an entry from the given data source that matches with the [id].
     */
    fun getEntryStream(id: Int): Flow<Entry?>

    /**
     * Insert entry in the data source
     */
    suspend fun insertEntry(entry: Entry)

    /**
     * Delete entry from the data source
     */
    suspend fun deleteEntry(entry: Entry)

    /**
     * Update entry in the data source
     */
    suspend fun updateEntry(entry: Entry)
}