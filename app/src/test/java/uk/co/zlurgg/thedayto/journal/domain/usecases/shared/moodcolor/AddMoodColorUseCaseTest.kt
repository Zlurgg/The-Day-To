package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for AddMoodColorUseCase.
 *
 * Tests the "create or restore" logic with comprehensive validation:
 * - Input validation (mood name, color, timestamp)
 * - Create new mood colors
 * - Restore deleted mood colors
 * - Reject duplicate active moods (case-insensitive)
 * - Input sanitization
 */
class AddMoodColorUseCaseTest {

    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository
    private lateinit var addMoodColorUseCase: AddMoodColorUseCase

    @Before
    fun setup() {
        fakeMoodColorRepository = FakeMoodColorRepository()
        addMoodColorUseCase = AddMoodColorUseCase(fakeMoodColorRepository)
    }

    // ============================================================
    // Success Cases: Create New Mood
    // ============================================================

    @Test
    fun `invoke - creates new mood successfully`() = runTest {
        // Given: A valid new mood color
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "4CAF50",
            id = null  // New mood, no ID yet
        )

        // When: Adding the mood color
        addMoodColorUseCase(moodColor)

        // Then: Mood should be created
        val moods = fakeMoodColorRepository.getMoodColorsSync()
        assertEquals("Should have 1 mood", 1, moods.size)
        assertEquals("Mood name should match", "Happy", moods[0].mood)
        assertEquals("Color should match", "4CAF50", moods[0].color)
        assertFalse("Should not be deleted", moods[0].isDeleted)
        assertNotNull("Should have assigned ID", moods[0].id)
    }

    @Test
    fun `invoke - sanitizes mood name on creation`() = runTest {
        // Given: A mood with whitespace and control characters
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "  Happy  \n\t",  // Leading/trailing whitespace + control chars
            color = "4CAF50",
            id = null
        )

        // When: Adding the mood color
        addMoodColorUseCase(moodColor)

        // Then: Mood name should be sanitized
        val moods = fakeMoodColorRepository.getMoodColorsSync()
        assertEquals("Mood should be trimmed and sanitized", "Happy", moods[0].mood)
    }

    @Test
    fun `invoke - allows multiple different moods`() = runTest {
        // Given: Three different moods
        val moods = listOf(
            TestDataBuilders.createMoodColor(mood = "Happy", id = null),
            TestDataBuilders.createMoodColor(mood = "Sad", id = null),
            TestDataBuilders.createMoodColor(mood = "Angry", id = null)
        )

        // When: Adding all moods
        moods.forEach { addMoodColorUseCase(it) }

        // Then: All three should be created
        val savedMoods = fakeMoodColorRepository.getMoodColorsSync()
        assertEquals("Should have 3 moods", 3, savedMoods.size)
        assertTrue("Should contain Happy", savedMoods.any { it.mood == "Happy" })
        assertTrue("Should contain Sad", savedMoods.any { it.mood == "Sad" })
        assertTrue("Should contain Angry", savedMoods.any { it.mood == "Angry" })
    }

    // ============================================================
    // Success Cases: Restore Deleted Mood
    // ============================================================

    @Test
    fun `invoke - restores deleted mood with new color`() = runTest {
        // Given: An existing deleted mood
        val deletedMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "4CAF50",  // Old color
            isDeleted = true,
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(deletedMood)

        // When: Adding mood with same name but different color
        val newMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "FF5722",  // New color
            id = null
        )
        addMoodColorUseCase(newMood)

        // Then: Mood should be restored with new color
        val moods = fakeMoodColorRepository.getMoodColorsSync()
        assertEquals("Should have 1 mood", 1, moods.size)
        assertEquals("Mood name should match", "Happy", moods[0].mood)
        assertEquals("Should have new color", "FF5722", moods[0].color)
        assertFalse("Should not be deleted", moods[0].isDeleted)
        assertEquals("Should keep original ID", 1, moods[0].id)
    }

    @Test
    fun `invoke - restores deleted mood case-insensitively`() = runTest {
        // Given: A deleted mood "Happy"
        val deletedMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            isDeleted = true,
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(deletedMood)

        // When: Adding "happy" (lowercase)
        val newMood = TestDataBuilders.createMoodColor(
            mood = "happy",  // Different case
            color = "FF5722",
            id = null
        )
        addMoodColorUseCase(newMood)

        // Then: Should restore the same mood
        val moods = fakeMoodColorRepository.getMoodColorsSync()
        assertEquals("Should have 1 mood", 1, moods.size)
        assertFalse("Should not be deleted", moods[0].isDeleted)
    }

    // ============================================================
    // Error Cases: Validation Failures
    // ============================================================

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for blank mood name`() = runTest {
        // Given: A mood with blank name
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "   ",  // Only whitespace
            id = null
        )

        // When/Then: Should throw exception
        addMoodColorUseCase(moodColor)
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for empty mood name`() = runTest {
        // Given: A mood with empty name
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "",
            id = null
        )

        // When/Then: Should throw exception
        addMoodColorUseCase(moodColor)
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for mood name too long`() = runTest {
        // Given: A mood name exceeding 50 characters
        val longMood = "a".repeat(51)
        val moodColor = TestDataBuilders.createMoodColor(
            mood = longMood,
            id = null
        )

        // When/Then: Should throw exception
        addMoodColorUseCase(moodColor)
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for blank color`() = runTest {
        // Given: A mood with blank color
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "   ",  // Blank color
            id = null
        )

        // When/Then: Should throw exception
        addMoodColorUseCase(moodColor)
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for empty color`() = runTest {
        // Given: A mood with empty color
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "",  // Empty color
            id = null
        )

        // When/Then: Should throw exception
        addMoodColorUseCase(moodColor)
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for invalid timestamp`() = runTest {
        // Given: A mood with negative timestamp
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "Happy",
            dateStamp = -1L,  // Invalid timestamp
            id = null
        )

        // When/Then: Should throw exception
        addMoodColorUseCase(moodColor)
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for zero timestamp`() = runTest {
        // Given: A mood with zero timestamp
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "Happy",
            dateStamp = 0L,  // Invalid timestamp
            id = null
        )

        // When/Then: Should throw exception
        addMoodColorUseCase(moodColor)
    }

    // ============================================================
    // Error Cases: Duplicate Detection
    // ============================================================

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for duplicate active mood`() = runTest {
        // Given: An existing active mood "Happy"
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            isDeleted = false,
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Trying to add another "Happy"
        val duplicateMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            id = null
        )

        // Then: Should throw exception
        addMoodColorUseCase(duplicateMood)
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for duplicate mood case-insensitive`() = runTest {
        // Given: An existing active mood "Happy"
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            isDeleted = false,
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Trying to add "happy" (lowercase)
        val duplicateMood = TestDataBuilders.createMoodColor(
            mood = "happy",  // Different case
            id = null
        )

        // Then: Should throw exception
        addMoodColorUseCase(duplicateMood)
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for duplicate mood with different case variations`() = runTest {
        // Given: An existing active mood "Happy"
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            isDeleted = false,
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Trying to add "HAPPY" (uppercase)
        val duplicateMood = TestDataBuilders.createMoodColor(
            mood = "HAPPY",
            id = null
        )

        // Then: Should throw exception
        addMoodColorUseCase(duplicateMood)
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for duplicate mood with whitespace`() = runTest {
        // Given: An existing active mood "Happy"
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            isDeleted = false,
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Trying to add "  Happy  " (with whitespace)
        val duplicateMood = TestDataBuilders.createMoodColor(
            mood = "  Happy  ",
            id = null
        )

        // Then: Should throw exception (whitespace is trimmed, so it's a duplicate)
        addMoodColorUseCase(duplicateMood)
    }

    // ============================================================
    // Edge Cases
    // ============================================================

    @Test
    fun `invoke - allows mood name at maximum length`() = runTest {
        // Given: A mood name exactly 50 characters (max allowed)
        val maxLengthMood = "a".repeat(50)
        val moodColor = TestDataBuilders.createMoodColor(
            mood = maxLengthMood,
            id = null
        )

        // When: Adding the mood
        addMoodColorUseCase(moodColor)

        // Then: Should succeed
        val moods = fakeMoodColorRepository.getMoodColorsSync()
        assertEquals("Should have 1 mood", 1, moods.size)
        assertEquals("Mood length should be 50", 50, moods[0].mood.length)
    }

    @Test
    fun `invoke - handles special characters in mood name`() = runTest {
        // Given: A mood with special characters
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "Happy! 😊",
            id = null
        )

        // When: Adding the mood
        addMoodColorUseCase(moodColor)

        // Then: Should succeed
        val moods = fakeMoodColorRepository.getMoodColorsSync()
        assertEquals("Should have 1 mood", 1, moods.size)
        assertTrue("Should contain special chars", moods[0].mood.contains("!"))
    }

    @Test
    fun `invoke - handles unicode characters in mood name`() = runTest {
        // Given: A mood with unicode characters
        val moodColor = TestDataBuilders.createMoodColor(
            mood = "Café",  // French accented character
            id = null
        )

        // When: Adding the mood
        addMoodColorUseCase(moodColor)

        // Then: Should succeed
        val moods = fakeMoodColorRepository.getMoodColorsSync()
        assertEquals("Should have 1 mood", 1, moods.size)
        assertEquals("Should preserve unicode", "Café", moods[0].mood)
    }
}
