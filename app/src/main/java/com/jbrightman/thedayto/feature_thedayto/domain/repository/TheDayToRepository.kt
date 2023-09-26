package com.jbrightman.thedayto.feature_thedayto.domain.repository

import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import kotlinx.coroutines.flow.Flow

interface TheDayToRepository {
    fun getTheDayToEntries(): Flow<List<TheDayToEntry>>
    suspend fun  getTheDayToEntryById(id: Int): TheDayToEntry?
    suspend fun  getTheDayToEntryByDate(date: Long): TheDayToEntry?
    suspend fun insertEntry(entry: TheDayToEntry)
    suspend fun deleteEntry(entry: TheDayToEntry)
    suspend fun  updateEntry(entry: TheDayToEntry)

}