package com.example.thedayto.domain.repository

import androidx.annotation.WorkerThread
import com.example.thedayto.data.local.TheDayToDao
import com.example.thedayto.data.local.TheDayToEntity
import kotlinx.coroutines.flow.Flow

class TheDayToRepository(private val theDayToDao: TheDayToDao) {

    val allEntries: Flow<List<TheDayToEntity>> = theDayToDao.getEntriesOrderedByDate()
    fun getEntryFromDate(date: String): TheDayToEntity = theDayToDao.getEntryFromDate(date)

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(theDayToEntity: TheDayToEntity) {
        theDayToDao.insert(theDayToEntity)
    }

}