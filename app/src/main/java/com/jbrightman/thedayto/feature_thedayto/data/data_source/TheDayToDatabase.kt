package com.jbrightman.thedayto.feature_thedayto.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry

@Database(
    entities = [TheDayToEntry::class],
    version = 1,
    exportSchema = false
)
abstract class TheDayToDatabase: RoomDatabase() {
    abstract val theDayToDao: TheDayToDao

    companion object {
        const val DATABASE_NAME  = "thedayto_db"
    }
}

