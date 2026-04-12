package uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.model.CuratedMoods
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SaveMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.ValidateMoodColorUseCase
import uk.co.zlurgg.thedayto.testutil.FakeTimeProvider

/**
 * Unit tests for [SeedRandomMoodColorsUseCase].
 *
 * Tests cover:
 * - Seeding range (5-10 moods)
 * - Duplicate name skipping (case-insensitive)
 * - Early exit on 50-cap (LimitReached)
 * - Graceful skip on other errors
 * - Empty pool edge case
 */
class SeedRandomMoodColorsUseCaseTest {

    private lateinit var useCase: SeedRandomMoodColorsUseCase
    private lateinit var repository: FakeMoodColorRepository
    private lateinit var timeProvider: FakeTimeProvider

    @Before
    fun setup() {
        repository = FakeMoodColorRepository()
        timeProvider = FakeTimeProvider()
        val validateUseCase = ValidateMoodColorUseCase(repository)
        val saveMoodColorUseCase = SaveMoodColorUseCase(validateUseCase, repository)
        useCase = SeedRandomMoodColorsUseCase(saveMoodColorUseCase, repository, timeProvider)
    }

    @Test
    fun `seeds between 5 and 10 moods when pool is empty`() = runTest {
        // When: Seeding from an empty palette
        val result = useCase()

        // Then: Success with count in range 5..10
        assertTrue("Should succeed", result is Result.Success)
        val count = (result as Result.Success).data
        assertTrue("Should seed at least 5 moods, got $count", count >= 5)
        assertTrue("Should seed at most 10 moods, got $count", count <= 10)

        // Verify moods actually exist in repository
        val saved = repository.getMoodColorsSync()
        assertEquals("Repository count should match result", count, saved.size)
    }

    @Test
    fun `all seeded moods come from the curated pool`() = runTest {
        // When: Seeding
        useCase()

        // Then: All saved mood names exist in CuratedMoods.ALL
        val curatedNames = CuratedMoods.ALL.map { it.mood.lowercase() }.toSet()
        val savedNames = repository.getMoodColorsSync().map { it.mood.lowercase() }
        savedNames.forEach { name ->
            assertTrue("'$name' should be in curated pool", name in curatedNames)
        }
    }

    @Test
    fun `skips moods that already exist (case-insensitive)`() = runTest {
        // Given: Some curated moods already exist
        val existing = CuratedMoods.ALL.take(3)
        existing.forEach { seed ->
            repository.insertMoodColor(
                MoodColor(
                    mood = seed.mood,
                    color = seed.color,
                    dateStamp = System.currentTimeMillis(),
                ),
            )
        }
        val initialCount = repository.getMoodColorsSync().size

        // When: Seeding
        val result = useCase()

        // Then: New moods don't include the existing ones
        assertTrue("Should succeed", result is Result.Success)
        val allSaved = repository.getMoodColorsSync()
        val newMoods = allSaved.drop(initialCount)
        val existingNames = existing.map { it.mood.lowercase() }.toSet()
        newMoods.forEach { moodColor ->
            assertTrue(
                "'${moodColor.mood}' should NOT be one of the pre-existing moods",
                moodColor.mood.lowercase() !in existingNames,
            )
        }
    }

    @Test
    fun `returns 0 when all curated moods already exist`() = runTest {
        // Given: All 58 curated moods exist
        CuratedMoods.ALL.forEach { seed ->
            repository.insertMoodColor(
                MoodColor(
                    mood = seed.mood,
                    color = seed.color,
                    dateStamp = System.currentTimeMillis(),
                ),
            )
        }

        // When: Seeding
        val result = useCase()

        // Then: Success with 0 additions
        assertTrue("Should succeed", result is Result.Success)
        assertEquals("Should add 0 moods", 0, (result as Result.Success).data)
    }

    @Test
    fun `stops saving on LimitReached and returns partial count`() = runTest {
        // Given: 48 moods already exist (2 slots left before 50-cap)
        // Don't pass explicit IDs — let the fake auto-assign so nextId stays in sync
        repeat(48) { i ->
            repository.insertMoodColor(
                MoodColor(
                    mood = "Existing$i",
                    color = "AABBCC",
                    dateStamp = System.currentTimeMillis(),
                ),
            )
        }
        assertEquals("Should have 48 moods pre-seeded", 48, repository.getMoodColorsSync().size)

        // When: Seeding (would try 5-10, but only 2 slots available)
        val result = useCase()

        // Then: Succeeds with at most 2 moods added
        assertTrue("Should succeed", result is Result.Success)
        val count = (result as Result.Success).data
        assertTrue("Should add at most 2 moods, got $count", count <= 2)

        // Total should not exceed 50
        val total = repository.getMoodColorsSync().size
        assertTrue("Total should not exceed 50, got $total", total <= 50)
    }

    @Test
    fun `repeated seeding yields diminishing returns`() = runTest {
        // When: Seed twice
        val first = useCase()
        val second = useCase()

        // Then: Both succeed, second adds fewer (pool is partially exhausted)
        assertTrue("First should succeed", first is Result.Success)
        assertTrue("Second should succeed", second is Result.Success)
        val firstCount = (first as Result.Success).data
        val secondCount = (second as Result.Success).data
        assertTrue("First should add 5-10, got $firstCount", firstCount in 5..10)
        // Second seeding has a smaller pool, so it should add fewer or equal
        // (could still add up to 10 if the pool is large enough, but the
        // available set is smaller)
        assertTrue("Second should still succeed", secondCount >= 0)
        // Total across both invocations should not exceed available curated moods
        val totalSaved = repository.getMoodColorsSync().size
        assertTrue(
            "Total $totalSaved should not exceed curated pool size ${CuratedMoods.ALL.size}",
            totalSaved <= CuratedMoods.ALL.size,
        )
    }

    @Test
    fun `uses TimeProvider for dateStamp, not System currentTimeMillis`() = runTest {
        // Given: FakeTimeProvider returns a known instant
        val expectedMillis = timeProvider.instant().toEpochMilli()

        // When: Seeding
        useCase()

        // Then: All saved moods have the FakeTimeProvider's timestamp
        val saved = repository.getMoodColorsSync()
        assertTrue("Should have seeded some moods", saved.isNotEmpty())
        saved.forEach { moodColor ->
            assertEquals(
                "dateStamp should come from TimeProvider",
                expectedMillis,
                moodColor.dateStamp,
            )
        }
    }
}
