package com.jbrightman.thedayto.feature_thedayto.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jbrightman.thedayto.feature_thedayto.data.data_source.entry.TheDayToDao
import com.jbrightman.thedayto.feature_thedayto.data.data_source.mood_color.MoodColorDao
import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.model.mood_color.MoodColor

@Database(
    entities = [TheDayToEntry::class, MoodColor::class],
    version = 1,
    exportSchema = false
)
abstract class TheDayToDatabase: RoomDatabase() {
    abstract val theDayToDao: TheDayToDao
    abstract val moodColorDao: MoodColorDao

    companion object {
        const val DATABASE_NAME  = "thedayto_db"
    }
}

