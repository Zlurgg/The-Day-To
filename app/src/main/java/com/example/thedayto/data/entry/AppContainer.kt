package com.example.thedayto.data.entry

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val entryRepo: EntryRepo
}

/**
 * [AppContainer] implementation that provides instance of [OfflineEntryRepo]
 */
class AppDataContainer(private val context: Context): AppContainer {
    override val entryRepo: EntryRepo by lazy {
        OfflineEntryRepo(EntryDatabase.getDatabase(context).entryDao())
    }
}