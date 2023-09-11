package com.example.thedayto.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [JournalEntry::class], version = 1, exportSchema = false)
abstract class EntryRoomDatabase: RoomDatabase() {

    abstract fun entryDao(): EntryDao

    private class EntryDatabaseCallback(
        private val scope: CoroutineScope
    ) : Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    var entryDao = database.entryDao()
                    populateDatabase(entryDao)
                }
            }
        }
    }
    companion object {
        @Volatile
        private var INSTANCE: EntryRoomDatabase? = null

        fun getDatabase(
            context: Context,
            scope: CoroutineScope
        ): EntryRoomDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EntryRoomDatabase::class.java,
                    "thedayto_database"
                )
                .addCallback(EntryDatabaseCallback(scope))
                .allowMainThreadQueries()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

suspend fun populateDatabase(entryDao: EntryDao) {
    // Delete all content here.
    entryDao.deleteAll()

    // Add sample words.
    val journalEntry = JournalEntry(1, "sample mood", "sample note", "1973-01-01")
    entryDao.insert(journalEntry)
}

