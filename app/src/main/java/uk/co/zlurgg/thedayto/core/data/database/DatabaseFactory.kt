package uk.co.zlurgg.thedayto.core.data.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

class DatabaseFactory(
    private val context: Context
) {
    fun create(): TheDayToDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            TheDayToDatabase::class.java,
            TheDayToDatabase.DATABASE_NAME
        )
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
