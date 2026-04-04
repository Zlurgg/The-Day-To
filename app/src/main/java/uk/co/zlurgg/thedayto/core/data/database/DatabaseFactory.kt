package uk.co.zlurgg.thedayto.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseFactory(
    private val context: Context
) {
    @Suppress("SpreadOperator") // Required by Room API for vararg migrations
    fun create(): TheDayToDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TheDayToDatabase::class.java,
            TheDayToDatabase.DATABASE_NAME
        )
            .addMigrations(*Migrations.ALL)
            .fallbackToDestructiveMigration(dropAllTables = true)
            .addCallback(object : RoomDatabase.Callback() {
                override fun onOpen(db: SupportSQLiteDatabase) {
                    super.onOpen(db)
                    db.execSQL("PRAGMA foreign_keys=ON")
                }
            })
            .build()
    }
}
