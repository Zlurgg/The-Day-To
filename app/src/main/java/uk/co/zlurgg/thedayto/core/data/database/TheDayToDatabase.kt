package uk.co.zlurgg.thedayto.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao
import uk.co.zlurgg.thedayto.journal.data.model.EntryEntity
import uk.co.zlurgg.thedayto.journal.data.model.MoodColorEntity

@Database(
    entities = [EntryEntity::class, MoodColorEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TheDayToDatabase : RoomDatabase() {
    abstract val entryDao: EntryDao
    abstract val moodColorDao: MoodColorDao

    companion object {
        const val DATABASE_NAME = "thedayto_db"
    }
}