package uk.co.zlurgg.thedayto.core.data.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations.
 *
 * Each migration handles schema changes between consecutive versions.
 */
object Migrations {

    /**
     * Migration 7 → 8: Add notification_settings table.
     *
     * Adds Room-based storage for notification settings, replacing SharedPreferences.
     * Data migration from SharedPreferences is handled by [NotificationMigrationService].
     */
    val MIGRATION_7_8 = object : Migration(7, 8) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS notification_settings (
                    userId TEXT PRIMARY KEY NOT NULL,
                    enabled INTEGER NOT NULL DEFAULT 0,
                    hour INTEGER NOT NULL DEFAULT 9,
                    minute INTEGER NOT NULL DEFAULT 0,
                    syncId TEXT NOT NULL,
                    syncStatus TEXT NOT NULL DEFAULT 'LOCAL_ONLY',
                    updatedAt INTEGER NOT NULL DEFAULT 0
                )
                """.trimIndent()
            )
        }
    }

    /**
     * All migrations in order.
     */
    val ALL = arrayOf(MIGRATION_7_8)
}
