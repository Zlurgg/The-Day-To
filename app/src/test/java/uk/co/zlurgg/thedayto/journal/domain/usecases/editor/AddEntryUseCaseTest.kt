package uk.co.zlurgg.thedayto.journal.domain.usecases.editor

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidEntryException
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for AddEntryUseCase.
 *
 * Tests the Entry schema refactor with moodColorId validation:
 * - Input validation (timestamp, content, moodColorId)
 * - Create new entries with valid moodColorId
 * - Reject entries with non-existent or deleted moodColorId
 * - Input sanitization
 * - Edge cases (max length, future dates, etc.)
 */
class AddEntryUseCaseTest {

    private lateinit var fakeEntryRepository: FakeEntryRepository
    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository
    private lateinit var addEntryUseCase: AddEntryUseCase

    @Before
    fun setup() {
        fakeEntryRepository = FakeEntryRepository()
        fakeMoodColorRepository = FakeMoodColorRepository()
        addEntryUseCase = AddEntryUseCase(fakeEntryRepository, fakeMoodColorRepository)
    }

    // ============================================================
    // Success Cases: Create Entry
    // ============================================================

    @Test
    fun `invoke - creates entry with valid moodColorId`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Adding an entry with this moodColorId
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = "Had a great day!",
            id = null
        )
        addEntryUseCase(entry)

        // Then: Entry should be created
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Should have 1 entry", 1, entries.size)
        assertEquals("MoodColorId should match", 1, entries[0].moodColorId)
        assertEquals("Content should match", "Had a great day!", entries[0].content)
        assertNotNull("Should have assigned ID", entries[0].id)
    }

    @Test
    fun `invoke - sanitizes content (removes control chars, trims)`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Adding entry with unsanitized content
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = "  Test content \u0000\u0001  ",  // Leading/trailing whitespace + control chars
            id = null
        )
        addEntryUseCase(entry)

        // Then: Content should be sanitized
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Content should be trimmed and sanitized", "Test content", entries[0].content)
    }

    @Test
    fun `invoke - allows empty content (optional field)`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Adding entry with empty content
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = "",
            id = null
        )
        addEntryUseCase(entry)

        // Then: Entry should be created with empty content
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Should have 1 entry", 1, entries.size)
        assertEquals("Content should be empty", "", entries[0].content)
    }

    @Test
    fun `invoke - handles maximum content length (5000 chars)`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Adding entry with content at max length
        val maxContent = "a".repeat(InputValidation.MAX_CONTENT_LENGTH)
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = maxContent,
            id = null
        )
        addEntryUseCase(entry)

        // Then: Entry should be created
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Should have 1 entry", 1, entries.size)
        assertEquals("Content length should be max", InputValidation.MAX_CONTENT_LENGTH, entries[0].content.length)
    }

    @Test
    fun `invoke - content with newlines and tabs is preserved`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Adding entry with newlines and tabs
        val content = "Line 1\nLine 2\n\tIndented line"
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = content,
            id = null
        )
        addEntryUseCase(entry)

        // Then: Newlines and tabs should be preserved
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Newlines and tabs should be preserved", content, entries[0].content)
    }

    @Test
    fun `invoke - allows multiple entries with same moodColorId`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Adding multiple entries with same moodColorId but different dates
        val today = System.currentTimeMillis()
        val yesterday = today - 86400000L // 1 day ago
        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, content = "Entry 1", dateStamp = today, id = null)
        val entry2 = TestDataBuilders.createEntry(moodColorId = 1, content = "Entry 2", dateStamp = yesterday, id = null)
        addEntryUseCase(entry1)
        addEntryUseCase(entry2)

        // Then: Both entries should be created
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Should have 2 entries", 2, entries.size)
        assertTrue("Both should have same moodColorId", entries.all { it.moodColorId == 1 })
    }

    @Test(expected = InvalidEntryException::class)
    fun `invoke - rejects duplicate entry for same date`() = runTest {
        // Given: A valid mood color and an existing entry for today
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        val todayEpoch = TestDataBuilders.getTodayEpoch()
        val entry1 = TestDataBuilders.createEntry(moodColorId = 1, content = "First entry", dateStamp = todayEpoch, id = null)
        addEntryUseCase(entry1)

        // When: Attempting to add another entry for the same date
        val entry2 = TestDataBuilders.createEntry(moodColorId = 1, content = "Second entry", dateStamp = todayEpoch, id = null)

        // Then: Should throw InvalidEntryException
        addEntryUseCase(entry2)
    }

    @Test
    fun `invoke - allows updating existing entry (same date, same id)`() = runTest {
        // Given: A valid mood color and an existing entry
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        val todayEpoch = TestDataBuilders.getTodayEpoch()
        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Original content", dateStamp = todayEpoch, id = 1)
        fakeEntryRepository.insertEntry(entry)

        // When: Updating the same entry (same id, same date)
        val updatedEntry = entry.copy(content = "Updated content")
        addEntryUseCase(updatedEntry)

        // Then: Entry should be updated, not duplicated
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Should still have 1 entry", 1, entries.size)
        assertEquals("Content should be updated", "Updated content", entries[0].content)
    }

    @Test
    fun `invoke - allows entry with future timestamp`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Adding entry with future timestamp
        val futureTimestamp = System.currentTimeMillis() + 86400000L // Tomorrow
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            dateStamp = futureTimestamp,
            id = null
        )
        addEntryUseCase(entry)

        // Then: Entry should be created
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Should have 1 entry", 1, entries.size)
        assertEquals("Timestamp should match future date", futureTimestamp, entries[0].dateStamp)
    }

    @Test
    fun `invoke - content exactly at max length is accepted`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Adding entry with content exactly at max length
        val exactMaxContent = "a".repeat(InputValidation.MAX_CONTENT_LENGTH)
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = exactMaxContent,
            id = null
        )
        addEntryUseCase(entry)

        // Then: Entry should be created
        val entries = fakeEntryRepository.getEntriesSync()
        assertEquals("Should have 1 entry", 1, entries.size)
        assertEquals("Content length should be exactly max", InputValidation.MAX_CONTENT_LENGTH, entries[0].content.length)
    }

    // ============================================================
    // Error Cases: Invalid MoodColorId
    // ============================================================

    @Test(expected = InvalidEntryException::class)
    fun `invoke - throws error when moodColorId does not exist`() = runTest {
        // Given: Empty mood color repository (no moods exist)

        // When: Trying to create entry with non-existent moodColorId
        val entry = TestDataBuilders.createEntry(moodColorId = 999, id = null)

        // Then: Should throw InvalidEntryException
        addEntryUseCase(entry)
    }

    @Test(expected = InvalidEntryException::class)
    fun `invoke - throws error when moodColorId points to deleted mood`() = runTest {
        // Given: A deleted mood exists
        val deletedMood = TestDataBuilders.createMoodColor(
            mood = "Sad",
            color = "2196F3",
            isDeleted = true,
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(deletedMood)

        // When: Trying to create entry with deleted moodColorId
        val entry = TestDataBuilders.createEntry(moodColorId = 1, id = null)

        // Then: Should throw InvalidEntryException
        addEntryUseCase(entry)
    }

    // ============================================================
    // Error Cases: Invalid Input
    // ============================================================

    @Test(expected = InvalidEntryException::class)
    fun `invoke - throws error for negative timestamp`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Trying to create entry with negative timestamp
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            dateStamp = -1L,
            id = null
        )

        // Then: Should throw InvalidEntryException
        addEntryUseCase(entry)
    }

    @Test(expected = InvalidEntryException::class)
    fun `invoke - throws error for zero timestamp`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Trying to create entry with zero timestamp
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            dateStamp = 0L,
            id = null
        )

        // Then: Should throw InvalidEntryException
        addEntryUseCase(entry)
    }

    @Test(expected = InvalidEntryException::class)
    fun `invoke - throws error when content exceeds max length`() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(moodColor)

        // When: Trying to create entry with content exceeding max length
        val tooLongContent = "a".repeat(InputValidation.MAX_CONTENT_LENGTH + 1)
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = tooLongContent,
            id = null
        )

        // Then: Should throw InvalidEntryException
        addEntryUseCase(entry)
    }
}
