package com.example.thedayto.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [TheDayToEntity::class], version = 1, exportSchema = false)
abstract class EntryRoomDatabase: RoomDatabase() {

    abstract fun entryDao(): TheDayToDao

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

suspend fun populateDatabase(theDayToDao: TheDayToDao) {
    // Delete all content here.
    theDayToDao.deleteAll()

    // Add sample words.
    val theDayToEntity = TheDayToEntity(1, "sample mood", "sample note", "1973-01-01")
    theDayToDao.insert(theDayToEntity)
}

