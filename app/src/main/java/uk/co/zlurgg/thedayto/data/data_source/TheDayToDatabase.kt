package uk.co.zlurgg.thedayto.data.data_source

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.co.zlurgg.thedayto.feature_daily_entry.data.data_source.DailyEntryDao
import uk.co.zlurgg.thedayto.feature_mood_color.data.data_source.MoodColorDao
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry
import uk.co.zlurgg.thedayto.feature_mood_color.domain.model.MoodColor

@Database(
    entities = [DailyEntry::class, MoodColor::class],
    version = 1,
    exportSchema = false
)
abstract class TheDayToDatabase: RoomDatabase() {
    abstract val dailyEntryDao: DailyEntryDao
    abstract val moodColorDao: MoodColorDao

    companion object {
        const val DATABASE_NAME  = "thedayto_db"
    }
}

