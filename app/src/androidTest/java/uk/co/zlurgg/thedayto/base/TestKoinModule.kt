package uk.co.zlurgg.thedayto.base

import android.content.Context
import androidx.room.Room
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.core.data.database.TheDayToDatabase

/**
 * Test Koin modules for instrumented tests.
 *
 * Provides test-specific implementations:
 * - In-memory Room database
 * - Test repositories (if needed)
 * - Mocked external services
 *
 * Usage:
 * ```kotlin
 * @Before
 * fun setup() {
 *     startKoin {
 *         androidContext(ApplicationProvider.getApplicationContext())
 *         modules(testDatabaseModule)
 *     }
 * }
 *
 * @After
 * fun tearDown() {
 *     stopKoin()
 * }
 * ```
 */

/**
 * Test database module - provides in-memory database for testing
 */
val testDatabaseModule = module {
    single {
        Room.inMemoryDatabaseBuilder(
            get<Context>(),
            TheDayToDatabase::class.java
        )
            .allowMainThreadQueries() // For testing only
            .build()
    }

    single { get<TheDayToDatabase>().entryDao }
    single { get<TheDayToDatabase>().moodColorDao }
}

/**
 * Test repository module - uses test database
 */
val testRepositoryModule = module {
    // Repository implementations would go here if needed
    // For now, we'll test repositories directly in DatabaseTest subclasses
}
