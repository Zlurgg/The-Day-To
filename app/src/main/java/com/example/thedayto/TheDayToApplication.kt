package com.example.thedayto

import android.app.Application
import com.example.thedayto.data.local.EntryRoomDatabase
import com.example.thedayto.domain.repository.TheDayToRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

@HiltAndroidApp
class TheDayToApplication: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { EntryRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TheDayToRepository(database.entryDao()) }
}