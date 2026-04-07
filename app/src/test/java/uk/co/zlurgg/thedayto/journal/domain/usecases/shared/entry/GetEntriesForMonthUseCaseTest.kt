package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import java.time.LocalDate
import java.time.ZoneOffset

class GetEntriesForMonthUseCaseTest {

    private lateinit var useCase: GetEntriesForMonthUseCase
    private lateinit var entryRepository: FakeEntryRepository
    private lateinit var moodColorRepository: FakeMoodColorRepository

    @Before
    fun setup() {
        moodColorRepository = FakeMoodColorRepository()
        entryRepository = FakeEntryRepository(moodColorRepository)
        useCase = GetEntriesForMonthUseCase(entryRepository)
    }

    private fun dateToEpoch(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day)
            .atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)
    }

    private suspend fun setupTestData() {
        // Add mood colors
        moodColorRepository.insertMoodColor(
            MoodColor(
                mood = "Happy",
                color = "FFA726",
                isDeleted = false,
                dateStamp = 0L,
                id = 1
            )
        )
        moodColorRepository.insertMoodColor(
            MoodColor(
                mood = "Sad",
                color = "1565C0",
                isDeleted = false,
                dateStamp = 0L,
                id = 2
            )
        )
        moodColorRepository.insertMoodColor(
            MoodColor(
                mood = "Calm",
                color = "66BB6A",
                isDeleted = false,
                dateStamp = 0L,
                id = 3
            )
        )

        // Add entries for January 2024
        entryRepository.insertEntry(
            Entry(
                moodColorId = 1,
                content = "Day 1",
                dateStamp = dateToEpoch(2024, 1, 1),
                id = 1
            )
        )
        entryRepository.insertEntry(
            Entry(
                moodColorId = 2,
                content = "Day 15",
                dateStamp = dateToEpoch(2024, 1, 15),
                id = 2
            )
        )
        entryRepository.insertEntry(
            Entry(
                moodColorId = 3,
                content = "Day 31",
                dateStamp = dateToEpoch(2024, 1, 31),
                id = 3
            )
        )

        // Add entry for February 2024 (should not be included in January query)
        entryRepository.insertEntry(
            Entry(
                moodColorId = 1,
                content = "Feb entry",
                dateStamp = dateToEpoch(2024, 2, 1),
                id = 4
            )
        )
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws on month less than 1`() = runTest {
        // When: Querying with month = 0
        useCase(month = 0, year = 2024).test {
            awaitItem()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws on month greater than 12`() = runTest {
        // When: Querying with month = 13
        useCase(month = 13, year = 2024).test {
            awaitItem()
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws on non-positive year`() = runTest {
        // When: Querying with year = 0
        useCase(month = 1, year = 0).test {
            awaitItem()
        }
    }

    @Test
    fun `invoke sorts by date ascending`() = runTest {
        // Given: Test data
        setupTestData()

        // When: Querying with date ascending order
        useCase(
            month = 1,
            year = 2024,
            entryOrder = EntryOrder.Date(OrderType.Ascending)
        ).test {
            val entries = awaitItem()

            // Then: Entries sorted by date ascending
            assertEquals(3, entries.size)
            assertEquals("Day 1", entries[0].content)
            assertEquals("Day 15", entries[1].content)
            assertEquals("Day 31", entries[2].content)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke sorts by date descending (default)`() = runTest {
        // Given: Test data
        setupTestData()

        // When: Querying with default order (date descending)
        useCase(month = 1, year = 2024).test {
            val entries = awaitItem()

            // Then: Entries sorted by date descending
            assertEquals(3, entries.size)
            assertEquals("Day 31", entries[0].content)
            assertEquals("Day 15", entries[1].content)
            assertEquals("Day 1", entries[2].content)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke sorts by mood ascending`() = runTest {
        // Given: Test data
        setupTestData()

        // When: Querying with mood ascending order
        useCase(
            month = 1,
            year = 2024,
            entryOrder = EntryOrder.Mood(OrderType.Ascending)
        ).test {
            val entries = awaitItem()

            // Then: Entries sorted by mood name ascending (Calm, Happy, Sad)
            assertEquals(3, entries.size)
            assertEquals("Calm", entries[0].moodName)
            assertEquals("Happy", entries[1].moodName)
            assertEquals("Sad", entries[2].moodName)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke sorts by mood descending`() = runTest {
        // Given: Test data
        setupTestData()

        // When: Querying with mood descending order
        useCase(
            month = 1,
            year = 2024,
            entryOrder = EntryOrder.Mood(OrderType.Descending)
        ).test {
            val entries = awaitItem()

            // Then: Entries sorted by mood name descending (Sad, Happy, Calm)
            assertEquals(3, entries.size)
            assertEquals("Sad", entries[0].moodName)
            assertEquals("Happy", entries[1].moodName)
            assertEquals("Calm", entries[2].moodName)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
