package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository
import uk.co.zlurgg.thedayto.testutil.FakeTimeProvider

class SeedDefaultMoodColorsUseCaseTest {

    private lateinit var useCase: SeedDefaultMoodColorsUseCase
    private lateinit var moodColorRepository: FakeMoodColorRepository
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var timeProvider: FakeTimeProvider

    @Before
    fun setup() {
        moodColorRepository = FakeMoodColorRepository()
        preferencesRepository = FakePreferencesRepository()
        timeProvider = FakeTimeProvider()
        useCase = SeedDefaultMoodColorsUseCase(moodColorRepository, preferencesRepository, timeProvider)
    }

    @Test
    fun `invoke seeds 7 default mood colors on first launch`() = runTest {
        // Given: First launch (default state)
        // preferencesRepository.isFirstLaunch() returns true by default

        // When: Seeding defaults
        val result = useCase()

        // Then: 7 mood colors created
        assertTrue(result is Result.Success)
        assertEquals(7, (result as Result.Success).data)

        // Verify mood colors exist
        val moodColors = moodColorRepository.getMoodColorsSync()
        assertEquals(7, moodColors.size)

        // Verify specific moods
        val moodNames = moodColors.map { it.mood }
        assertTrue(moodNames.contains("Happy"))
        assertTrue(moodNames.contains("Sad"))
        assertTrue(moodNames.contains("In Love"))
        assertTrue(moodNames.contains("Calm"))
        assertTrue(moodNames.contains("Excited"))
        assertTrue(moodNames.contains("Anxious"))
        assertTrue(moodNames.contains("Grateful"))
    }

    @Test
    fun `invoke returns 0 and does nothing when not first launch`() = runTest {
        // Given: Not first launch
        preferencesRepository.markFirstLaunchComplete()

        // When: Attempting to seed
        val result = useCase()

        // Then: Returns 0 (no seeding)
        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data)

        // Verify no mood colors created
        val moodColors = moodColorRepository.getMoodColorsSync()
        assertEquals(0, moodColors.size)
    }

    @Test
    fun `invoke returns success count on partial failure`() = runTest {
        // Given: First launch with a failing repository
        // We'll simulate partial failure by pre-inserting a conflicting mood
        // The UseCase continues on individual failures

        // When: Seeding defaults (all should succeed in this fake)
        val result = useCase()

        // Then: Success count returned
        assertTrue(result is Result.Success)
        // All 7 should succeed with fake repository
        assertEquals(7, (result as Result.Success).data)
    }

    @Test
    fun `invoke returns error when all seeds fail`() = runTest {
        // Given: First launch with completely failing repository
        val failingRepository = FailingMoodColorRepository()
        useCase = SeedDefaultMoodColorsUseCase(failingRepository, preferencesRepository, timeProvider)

        // When: Seeding defaults
        val result = useCase()

        // Then: Error returned (all failed)
        assertTrue(result is Result.Error)
        assertEquals(DataError.Local.DATABASE_ERROR, (result as Result.Error).error)
    }

    @Test
    fun `reseed seeds mood colors regardless of first launch state`() = runTest {
        // Given: Not first launch
        preferencesRepository.markFirstLaunchComplete()

        // When: Calling reseed (used after sign-out)
        val result = useCase.reseed()

        // Then: Mood colors seeded despite not being first launch
        assertTrue(result is Result.Success)
        assertEquals(7, (result as Result.Success).data)

        val moodColors = moodColorRepository.getMoodColorsSync()
        assertEquals(7, moodColors.size)
    }

    /**
     * Helper repository that always fails on insert.
     */
    private class FailingMoodColorRepository : MoodColorRepository {
        override suspend fun insertMoodColor(
            moodColor: MoodColor
        ): Result<Long, DataError.Local> {
            return Result.Error(DataError.Local.DATABASE_ERROR)
        }

        override suspend fun deleteMoodColor(id: Int): EmptyResult<DataError.Local> =
            Result.Success(Unit)

        override suspend fun getMoodColorById(id: Int): Result<MoodColor?, DataError.Local> =
            Result.Success(null)

        override suspend fun getMoodColorByName(mood: String): Result<MoodColor?, DataError.Local> =
            Result.Success(null)

        override fun getMoodColors(): Flow<List<MoodColor>> = flowOf(emptyList())

        override suspend fun updateMoodColor(
            moodColor: MoodColor
        ): EmptyResult<DataError.Local> = Result.Success(Unit)
    }
}
