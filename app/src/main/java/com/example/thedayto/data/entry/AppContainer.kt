package com.example.thedayto.data.entry

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val entryRepository: EntryRepository
}

/**
 * [AppContainer] implementation that provides instance of [OfflineEntryRepository]
 */
class AppDataContainer(private val context: Context): AppContainer {
    private val applicationScope = CoroutineScope(SupervisorJob())

    override val entryRepository: EntryRepository by lazy {
        OfflineEntryRepository(EntryDatabase.getDatabase(context, applicationScope).entryDao())
    }
}