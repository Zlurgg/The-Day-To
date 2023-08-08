package com.example.thedayto.data.entry

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Entry::class], version = 1, exportSchema = false)
abstract class EntryDatabase() : RoomDatabase() {
    abstract fun entryDao(): EntryDao

    companion object {
        @Volatile
        private var Instance: EntryDatabase? = null

        fun getDatabase(context: Context): EntryDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context = context, EntryDatabase::class.java, "entries_database")
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }
    }
}