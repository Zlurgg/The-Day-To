package uk.co.zlurgg.thedayto.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao
import uk.co.zlurgg.thedayto.journal.data.model.EntryEntity
import uk.co.zlurgg.thedayto.journal.data.model.MoodColorEntity
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsDao
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsEntity
import uk.co.zlurgg.thedayto.sync.data.dao.PendingSyncDeletionDao
import uk.co.zlurgg.thedayto.sync.data.model.PendingSyncDeletionEntity

@Database(
    entities = [
        EntryEntity::class,
        MoodColorEntity::class,
        PendingSyncDeletionEntity::class,
        NotificationSettingsEntity::class
    ],
    version = 1, // Release baseline - no production users yet
    exportSchema = true
)
abstract class TheDayToDatabase : RoomDatabase() {
    abstract val entryDao: EntryDao
    abstract val moodColorDao: MoodColorDao
    abstract val pendingSyncDeletionDao: PendingSyncDeletionDao
    abstract val notificationSettingsDao: NotificationSettingsDao

    companion object {
        const val DATABASE_NAME = "thedayto_db"
    }
}