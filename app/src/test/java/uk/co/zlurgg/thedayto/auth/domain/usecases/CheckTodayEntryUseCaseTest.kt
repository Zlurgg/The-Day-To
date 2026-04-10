package uk.co.zlurgg.thedayto.auth.domain.usecases

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Unit tests for CheckTodayEntryUseCase.
 *
 * Tests cover:
 * - Returns timestamp when entry exists for today
 * - Returns null when no entry exists for today
 */
class CheckTodayEntryUseCaseTest {

    private lateinit var useCase: CheckTodayEntryUseCase
    private lateinit var fakeEntryRepository: FakeEntryRepository

    @Before
    fun setup() {
        fakeEntryRepository = FakeEntryRepository()
        useCase = CheckTodayEntryUseCase(fakeEntryRepository)
    }

    @Test
    fun `invoke returns timestamp when entry exists for today`() = runTest {
        // Given: Entry exists for today
        val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
        val entry = Entry(
            moodColorId = 1,
            content = "Test entry",
            dateStamp = todayStart,
            id = 1,
        )
        fakeEntryRepository.insertEntry(entry)

        // When: Checking for today's entry
        val result = useCase()

        // Then: Returns today's timestamp
        assertNotNull(result)
        assertEquals(todayStart, result)
    }

    @Test
    fun `invoke returns null when no entry exists for today`() = runTest {
        // Given: No entry for today (empty repository)

        // When: Checking for today's entry
        val result = useCase()

        // Then: Returns null
        assertNull(result)
    }

    @Test
    fun `invoke returns null when repository returns error`() = runTest {
        // Given: Repository configured to return error
        fakeEntryRepository.shouldReturnError = true

        // When: Checking for today's entry
        val result = useCase()

        // Then: Returns null (getOrNull returns null on error)
        assertNull(result)
    }
}
