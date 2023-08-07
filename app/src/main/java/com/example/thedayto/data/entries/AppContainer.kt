package com.example.thedayto.data.entries

import android.content.Context

/**
 * App container for Dependency injection.
 */
interface AppContainer {
    val entriesRepo: EntriesRepo
}

/**
 * [AppContainer] implementation that provides instance of [OfflineEntriesRepo]
 */
class AppDataContainer(private val context: Context): AppContainer {
    override val entriesRepo: EntriesRepo by lazy {
        OfflineEntriesRepo(EntryDatabase.getDatabase(context).entryDao())
    }
}