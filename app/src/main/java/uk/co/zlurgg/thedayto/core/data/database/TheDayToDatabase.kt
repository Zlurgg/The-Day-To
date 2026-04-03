package uk.co.zlurgg.thedayto.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao
import uk.co.zlurgg.thedayto.journal.data.model.EntryEntity
import uk.co.zlurgg.thedayto.journal.data.model.MoodColorEntity
import uk.co.zlurgg.thedayto.sync.data.dao.PendingSyncDeletionDao
import uk.co.zlurgg.thedayto.sync.data.model.PendingSyncDeletionEntity

@Database(
    entities = [EntryEntity::class, MoodColorEntity::class, PendingSyncDeletionEntity::class],
    version = 7,  // Added pending_sync_deletion table for hard delete tracking
    exportSchema = false
)
abstract class TheDayToDatabase : RoomDatabase() {
    abstract val entryDao: EntryDao
    abstract val moodColorDao: MoodColorDao
    abstract val pendingSyncDeletionDao: PendingSyncDeletionDao

    companion object {
        const val DATABASE_NAME = "thedayto_db"
    }
}