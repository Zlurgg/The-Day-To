package uk.co.zlurgg.thedayto.base

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Before
import uk.co.zlurgg.thedayto.core.data.database.TheDayToDatabase
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao

/**
 * Base class for Room database instrumented tests.
 *
 * Provides:
 * - In-memory database instance (automatically cleared after each test)
 * - DAOs for testing
 * - Setup and teardown methods
 *
 * Usage:
 * ```kotlin
 * class MyRepositoryTest : DatabaseTest() {
 *     @Test
 *     fun myTest() {
 *         // Use entryDao, moodColorDao, or database
 *     }
 * }
 * ```
 */
abstract class DatabaseTest {

    protected lateinit var database: TheDayToDatabase
    protected lateinit var entryDao: EntryDao
    protected lateinit var moodColorDao: MoodColorDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            TheDayToDatabase::class.java
        )
            .allowMainThreadQueries() // For testing only - instrumented tests can run on main thread
            .build()

        entryDao = database.entryDao
        moodColorDao = database.moodColorDao
    }

    @After
    fun closeDb() {
        database.close()
    }
}
