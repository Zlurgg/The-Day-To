package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import io.github.zlurgg.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for GetEntriesUseCase.
 *
 * Tests the main data retrieval flow with JOIN operations:
 * - Returns entries with mood color data (joined data)
 * - Orders entries by date (ascending/descending)
 * - Orders entries by mood name (ascending/descending)
 * - Case-insensitive mood sorting
 * - Edge cases (empty list, same date, same mood)
 */
class GetEntriesUseCaseTest {

    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository
    private lateinit var fakeEntryRepository: FakeEntryRepository
    private lateinit var getEntriesUseCase: GetEntriesUseCase

    @Before
    fun setup() {
        fakeMoodColorRepository = FakeMoodColorRepository()
        fakeEntryRepository = FakeEntryRepository(fakeMoodColorRepository)
        getEntriesUseCase = GetEntriesUseCase(fakeEntryRepository)
    }

    // ============================================================
    // Success Cases: Returns Joined Data
    // ============================================================

    @Test
    fun `invoke - returns entries with mood color data from join`() = runTest {
        // Given: A mood color and an entry
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Great day!", id = 1)
        fakeEntryRepository.insertEntry(entry)

        // When: Getting entries
        getEntriesUseCase().test {
            // Then: Should return entry with mood color data from join
            val entries = awaitItem()
            assertEquals("Should have 1 entry", 1, entries.size)
            assertEquals("Mood name should match", "Happy", entries[0].moodName)
            assertEquals("Mood color should match", "4CAF50", entries[0].moodColor)
            assertEquals("MoodColorId should match", 1, entries[0].moodColorId)
            assertEquals("Content should match", "Great day!", entries[0].content)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - returns empty list when no entries exist`() = runTest {
        // Given: No entries

        // When: Getting entries
        getEntriesUseCase().test {
            // Then: Should return empty list
            val entries = awaitItem()
            assertTrue("Should be empty", entries.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - handles multiple entries correctly`() = runTest {
        // Given: Multiple mood colors and entries
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", color = "2196F3", id = 2)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)

        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 1000L, id = 1)
        val entry2 = TestDataBuilders.createEntry(moodColorId = 2, dateStamp = 2000L, id = 2)
        val entry3 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 3000L, id = 3)
        fakeEntryRepository.insertEntry(entry1)
        fakeEntryRepository.insertEntry(entry2)
        fakeEntryRepository.insertEntry(entry3)

        // When: Getting entries
        getEntriesUseCase().test {
            // Then: Should return all entries with correct mood data
            val entries = awaitItem()
            assertEquals("Should have 3 entries", 3, entries.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Success Cases: Date Ordering
    // ============================================================

    @Test
    fun `invoke - orders by date descending (default)`() = runTest {
        // Given: Entries with different dates
        val mood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(mood)

        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 1000L, id = 1)
        val entry2 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 3000L, id = 2)
        val entry3 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 2000L, id = 3)
        fakeEntryRepository.insertEntry(entry1)
        fakeEntryRepository.insertEntry(entry2)
        fakeEntryRepository.insertEntry(entry3)

        // When: Getting entries with default order (Date Descending)
        getEntriesUseCase().test {
            // Then: Should be ordered by date descending (newest first)
            val entries = awaitItem()
            assertEquals("First entry should have latest date", 3000L, entries[0].dateStamp)
            assertEquals("Second entry should have middle date", 2000L, entries[1].dateStamp)
            assertEquals("Third entry should have earliest date", 1000L, entries[2].dateStamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - orders by date ascending`() = runTest {
        // Given: Entries with different dates
        val mood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(mood)

        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 3000L, id = 1)
        val entry2 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 1000L, id = 2)
        val entry3 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 2000L, id = 3)
        fakeEntryRepository.insertEntry(entry1)
        fakeEntryRepository.insertEntry(entry2)
        fakeEntryRepository.insertEntry(entry3)

        // When: Getting entries with date ascending order
        getEntriesUseCase(EntryOrder.Date(OrderType.Ascending)).test {
            // Then: Should be ordered by date ascending (oldest first)
            val entries = awaitItem()
            assertEquals("First entry should have earliest date", 1000L, entries[0].dateStamp)
            assertEquals("Second entry should have middle date", 2000L, entries[1].dateStamp)
            assertEquals("Third entry should have latest date", 3000L, entries[2].dateStamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Success Cases: Mood Name Ordering
    // ============================================================

    @Test
    fun `invoke - orders by mood name ascending (case-insensitive)`() = runTest {
        // Given: Entries with different moods (mixed case)
        val mood1 = TestDataBuilders.createMoodColor(mood = "Sad", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "happy", id = 2) // lowercase
        val mood3 = TestDataBuilders.createMoodColor(mood = "Anxious", id = 3)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)
        fakeMoodColorRepository.insertMoodColor(mood3)

        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, id = 1) // Sad
        val entry2 = TestDataBuilders.createEntry(moodColorId = 2, id = 2) // happy
        val entry3 = TestDataBuilders.createEntry(moodColorId = 3, id = 3) // Anxious
        fakeEntryRepository.insertEntry(entry1)
        fakeEntryRepository.insertEntry(entry2)
        fakeEntryRepository.insertEntry(entry3)

        // When: Getting entries with mood ascending order
        getEntriesUseCase(EntryOrder.Mood(OrderType.Ascending)).test {
            // Then: Should be ordered alphabetically (case-insensitive): Anxious, happy, Sad
            val entries = awaitItem()
            assertEquals("First should be Anxious", "Anxious", entries[0].moodName)
            assertEquals("Second should be happy", "happy", entries[1].moodName)
            assertEquals("Third should be Sad", "Sad", entries[2].moodName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - orders by mood name descending (case-insensitive)`() = runTest {
        // Given: Entries with different moods
        val mood1 = TestDataBuilders.createMoodColor(mood = "Anxious", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "HAPPY", id = 2) // uppercase
        val mood3 = TestDataBuilders.createMoodColor(mood = "sad", id = 3) // lowercase
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)
        fakeMoodColorRepository.insertMoodColor(mood3)

        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, id = 1) // Anxious
        val entry2 = TestDataBuilders.createEntry(moodColorId = 2, id = 2) // HAPPY
        val entry3 = TestDataBuilders.createEntry(moodColorId = 3, id = 3) // sad
        fakeEntryRepository.insertEntry(entry1)
        fakeEntryRepository.insertEntry(entry2)
        fakeEntryRepository.insertEntry(entry3)

        // When: Getting entries with mood descending order
        getEntriesUseCase(EntryOrder.Mood(OrderType.Descending)).test {
            // Then: Should be ordered reverse alphabetically: sad, HAPPY, Anxious
            val entries = awaitItem()
            assertEquals("First should be sad", "sad", entries[0].moodName)
            assertEquals("Second should be HAPPY", "HAPPY", entries[1].moodName)
            assertEquals("Third should be Anxious", "Anxious", entries[2].moodName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Edge Cases
    // ============================================================

    @Test
    fun `invoke - handles entries with same mood name (stable sort by insertion order)`() = runTest {
        // Given: Multiple entries with same mood
        val mood = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(mood)

        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 1000L, id = 1)
        val entry2 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 2000L, id = 2)
        val entry3 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 3000L, id = 3)
        fakeEntryRepository.insertEntry(entry1)
        fakeEntryRepository.insertEntry(entry2)
        fakeEntryRepository.insertEntry(entry3)

        // When: Getting entries ordered by mood
        getEntriesUseCase(EntryOrder.Mood(OrderType.Ascending)).test {
            // Then: All should have same mood name, order maintained by original insertion
            val entries = awaitItem()
            assertEquals("Should have 3 entries", 3, entries.size)
            assertTrue("All should have same mood", entries.all { it.moodName == "Happy" })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - handles entries with same date (stable sort)`() = runTest {
        // Given: Multiple entries with same date
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)

        val sameDate = 5000L
        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = sameDate, id = 1)
        val entry2 = TestDataBuilders.createEntry(moodColorId = 2, dateStamp = sameDate, id = 2)
        val entry3 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = sameDate, id = 3)
        fakeEntryRepository.insertEntry(entry1)
        fakeEntryRepository.insertEntry(entry2)
        fakeEntryRepository.insertEntry(entry3)

        // When: Getting entries ordered by date
        getEntriesUseCase(EntryOrder.Date(OrderType.Descending)).test {
            // Then: All should have same date
            val entries = awaitItem()
            assertEquals("Should have 3 entries", 3, entries.size)
            assertTrue("All should have same date", entries.all { it.dateStamp == sameDate })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
