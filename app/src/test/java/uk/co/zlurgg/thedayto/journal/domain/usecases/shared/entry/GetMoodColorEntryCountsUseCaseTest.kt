package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for GetMoodColorEntryCountsUseCase.
 *
 * Tests the counting of entries per mood color:
 * - Returns empty map when no entries exist
 * - Counts entries per mood color correctly
 * - Updates reactively when entries change
 */
class GetMoodColorEntryCountsUseCaseTest {

    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository
    private lateinit var fakeEntryRepository: FakeEntryRepository
    private lateinit var getMoodColorEntryCountsUseCase: GetMoodColorEntryCountsUseCase

    @Before
    fun setup() {
        fakeMoodColorRepository = FakeMoodColorRepository()
        fakeEntryRepository = FakeEntryRepository(fakeMoodColorRepository)
        getMoodColorEntryCountsUseCase = GetMoodColorEntryCountsUseCase(fakeEntryRepository)
    }

    @Test
    fun `invoke - returns empty map when no entries exist`() = runTest {
        // Given: No entries

        // When: Getting entry counts
        getMoodColorEntryCountsUseCase().test {
            // Then: Should return empty map
            val counts = awaitItem()
            assertTrue("Should be empty", counts.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - returns correct count for single mood color`() = runTest {
        // Given: One mood color with multiple entries
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 1000L, id = 1)
        val entry2 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 2000L, id = 2)
        val entry3 = TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 3000L, id = 3)
        fakeEntryRepository.insertEntry(entry1)
        fakeEntryRepository.insertEntry(entry2)
        fakeEntryRepository.insertEntry(entry3)

        // When: Getting entry counts
        getMoodColorEntryCountsUseCase().test {
            // Then: Should return count of 3 for mood color 1
            val counts = awaitItem()
            assertEquals("Should have 1 mood color in map", 1, counts.size)
            assertEquals("Mood color 1 should have 3 entries", 3, counts[1])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - returns correct counts for multiple mood colors`() = runTest {
        // Given: Multiple mood colors with different entry counts
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
        val mood3 = TestDataBuilders.createMoodColor(mood = "Angry", id = 3)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)
        fakeMoodColorRepository.insertMoodColor(mood3)

        // 3 entries for Happy
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 1000L, id = 1))
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 2000L, id = 2))
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 3000L, id = 3))

        // 1 entry for Sad
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 2, dateStamp = 4000L, id = 4))

        // 2 entries for Angry
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 3, dateStamp = 5000L, id = 5))
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 3, dateStamp = 6000L, id = 6))

        // When: Getting entry counts
        getMoodColorEntryCountsUseCase().test {
            // Then: Should return correct counts for each mood color
            val counts = awaitItem()
            assertEquals("Should have 3 mood colors in map", 3, counts.size)
            assertEquals("Happy (1) should have 3 entries", 3, counts[1])
            assertEquals("Sad (2) should have 1 entry", 1, counts[2])
            assertEquals("Angry (3) should have 2 entries", 2, counts[3])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - mood color with zero entries not in map`() = runTest {
        // Given: Two mood colors, but only one has entries
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)

        // Only add entries for Happy
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 1000L, id = 1))

        // When: Getting entry counts
        getMoodColorEntryCountsUseCase().test {
            // Then: Only Happy should be in map, Sad should not appear
            val counts = awaitItem()
            assertEquals("Should have 1 mood color in map", 1, counts.size)
            assertEquals("Happy (1) should have 1 entry", 1, counts[1])
            assertEquals("Sad (2) should not be in map (null)", null, counts[2])
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `invoke - updates reactively when entry added`() = runTest {
        // Given: One mood color with one entry
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)
        fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 1000L, id = 1))

        // When: Getting entry counts and adding another entry
        getMoodColorEntryCountsUseCase().test {
            // Then: Initial count should be 1
            val initialCounts = awaitItem()
            assertEquals("Initial count should be 1", 1, initialCounts[1])

            // When: Adding another entry
            fakeEntryRepository.insertEntry(TestDataBuilders.createEntry(moodColorId = 1, dateStamp = 2000L, id = 2))

            // Then: Count should update to 2
            val updatedCounts = awaitItem()
            assertEquals("Updated count should be 2", 2, updatedCounts[1])

            cancelAndIgnoreRemainingEvents()
        }
    }
}
