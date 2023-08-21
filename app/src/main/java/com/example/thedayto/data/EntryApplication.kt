package com.example.thedayto.data

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class EntryApplication: Application() {
    private val applicationScope = CoroutineScope(SupervisorJob())
    private val database by lazy { EntryRoomDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { EntryRepository(database.entryDao()) }
}