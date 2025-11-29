package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for UpdateMoodColorNameUseCase.
 *
 * Tests updating mood color names with validation:
 * - Successfully update name
 * - Validate name is not blank/empty
 * - Validate name length limit
 * - Handle mood not found error
 * - Handle duplicate name detection (case-insensitive)
 * - Allow case changes for same name
 */
class UpdateMoodColorNameUseCaseTest {

    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository
    private lateinit var updateMoodColorNameUseCase: UpdateMoodColorNameUseCase

    @Before
    fun setup() {
        fakeMoodColorRepository = FakeMoodColorRepository()
        updateMoodColorNameUseCase = UpdateMoodColorNameUseCase(fakeMoodColorRepository)
    }

    // ============================================================
    // Success Cases
    // ============================================================

    @Test
    fun `invoke - updates mood name successfully`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "4CAF50",
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating to new name
        updateMoodColorNameUseCase(id = 1, newMood = "Joyful")

        // Then: Name should be updated
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1)
        assertEquals("Name should be updated", "Joyful", updatedMood?.mood)
        assertEquals("Color should remain same", "4CAF50", updatedMood?.color)
        assertEquals("ID should remain same", 1, updatedMood?.id)
    }

    @Test
    fun `invoke - updates only name, preserves other fields`() = runTest {
        // Given: An existing mood with specific properties
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Calm",
            color = "9C27B0",
            isDeleted = false,
            dateStamp = 1234567890L,
            id = 5
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating name
        updateMoodColorNameUseCase(id = 5, newMood = "Peaceful")

        // Then: Only name should change, everything else preserved
        val updatedMood = fakeMoodColorRepository.getMoodColorById(5)
        assertEquals("Name should be updated", "Peaceful", updatedMood?.mood)
        assertEquals("Color should be preserved", "9C27B0", updatedMood?.color)
        assertEquals("isDeleted should be preserved", false, updatedMood?.isDeleted)
        assertEquals("dateStamp should be preserved", 1234567890L, updatedMood?.dateStamp)
        assertEquals("ID should be preserved", 5, updatedMood?.id)
    }

    @Test
    fun `invoke - allows case change for same name`() = runTest {
        // Given: An existing mood with lowercase name
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "happy",
            color = "4CAF50",
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating to same name with different case
        updateMoodColorNameUseCase(id = 1, newMood = "Happy")

        // Then: Case should be updated
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1)
        assertEquals("Name case should be updated", "Happy", updatedMood?.mood)
    }

    @Test
    fun `invoke - no-op for unchanged name`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "4CAF50",
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating to exact same name
        updateMoodColorNameUseCase(id = 1, newMood = "Happy")

        // Then: Should succeed without error, name unchanged
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1)
        assertEquals("Name should remain same", "Happy", updatedMood?.mood)
    }

    @Test
    fun `invoke - trims whitespace from name`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "4CAF50",
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating with whitespace around name
        updateMoodColorNameUseCase(id = 1, newMood = "  Joyful  ")

        // Then: Name should be trimmed
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1)
        assertEquals("Name should be trimmed", "Joyful", updatedMood?.mood)
    }

    @Test
    fun `invoke - updates multiple moods independently`() = runTest {
        // Given: Multiple moods
        fakeMoodColorRepository.addDefaultMoods()

        // When: Updating only mood ID 2
        updateMoodColorNameUseCase(id = 2, newMood = "Melancholy")

        // Then: Only mood 2 should be updated
        val mood1 = fakeMoodColorRepository.getMoodColorById(1)
        val mood2 = fakeMoodColorRepository.getMoodColorById(2)
        val mood3 = fakeMoodColorRepository.getMoodColorById(3)

        assertEquals("Mood 1 should keep original name", "Happy", mood1?.mood)
        assertEquals("Mood 2 should have new name", "Melancholy", mood2?.mood)
        assertEquals("Mood 3 should keep original name", "Angry", mood3?.mood)
    }

    // ============================================================
    // Error Cases: Validation Failures
    // ============================================================

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for mood not found`() = runTest {
        // Given: No moods in repository

        // When: Trying to update non-existent mood
        updateMoodColorNameUseCase(id = 999, newMood = "Joyful")

        // Then: Should throw InvalidMoodColorException
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for blank name`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Trying to update to blank name
        updateMoodColorNameUseCase(id = 1, newMood = "   ")

        // Then: Should throw InvalidMoodColorException
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for empty name`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Trying to update to empty name
        updateMoodColorNameUseCase(id = 1, newMood = "")

        // Then: Should throw InvalidMoodColorException
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for name too long`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Trying to update to name exceeding 50 characters
        val longName = "A".repeat(51)
        updateMoodColorNameUseCase(id = 1, newMood = longName)

        // Then: Should throw InvalidMoodColorException
    }

    @Test
    fun `invoke - accepts name at max length`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating to name exactly at 50 character limit
        val maxLengthName = "A".repeat(50)
        updateMoodColorNameUseCase(id = 1, newMood = maxLengthName)

        // Then: Should succeed
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1)
        assertEquals("Should accept max length name", maxLengthName, updatedMood?.mood)
    }

    // ============================================================
    // Duplicate Detection
    // ============================================================

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for duplicate name with active mood`() = runTest {
        // Given: Two existing moods
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)

        // When: Trying to rename mood2 to "Happy" (already exists)
        updateMoodColorNameUseCase(id = 2, newMood = "Happy")

        // Then: Should throw InvalidMoodColorException
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for duplicate name case-insensitive`() = runTest {
        // Given: Two existing moods
        val mood1 = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val mood2 = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
        fakeMoodColorRepository.insertMoodColor(mood1)
        fakeMoodColorRepository.insertMoodColor(mood2)

        // When: Trying to rename mood2 to "HAPPY" (case-insensitive duplicate)
        updateMoodColorNameUseCase(id = 2, newMood = "HAPPY")

        // Then: Should throw InvalidMoodColorException
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for duplicate name with deleted mood`() = runTest {
        // Given: One active and one deleted mood with different names
        val activeMood = TestDataBuilders.createMoodColor(mood = "Sad", id = 1)
        val deletedMood = TestDataBuilders.createMoodColor(mood = "Happy", isDeleted = true, id = 2)
        fakeMoodColorRepository.insertMoodColor(activeMood)
        fakeMoodColorRepository.insertMoodColor(deletedMood)

        // When: Trying to rename active mood to deleted mood's name
        updateMoodColorNameUseCase(id = 1, newMood = "Happy")

        // Then: Should throw InvalidMoodColorException (deleted mood still exists)
    }

    @Test
    fun `invoke - allows renaming to unique name when others exist`() = runTest {
        // Given: Multiple existing moods
        fakeMoodColorRepository.addDefaultMoods()

        // When: Renaming to a unique name
        updateMoodColorNameUseCase(id = 1, newMood = "Ecstatic")

        // Then: Should succeed
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1)
        assertEquals("Should allow unique name", "Ecstatic", updatedMood?.mood)
    }

    // ============================================================
    // Edge Cases
    // ============================================================

    @Test
    fun `invoke - sanitizes control characters from name`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating with control characters in name
        updateMoodColorNameUseCase(id = 1, newMood = "Joy\nful\tMood")

        // Then: Control characters should be removed
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1)
        assertEquals("Should sanitize control chars", "JoyfulMood", updatedMood?.mood)
    }

    @Test
    fun `invoke - can update deleted mood name`() = runTest {
        // Given: A deleted mood (edge case)
        val deletedMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            isDeleted = true,
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(deletedMood)

        // When: Updating the deleted mood's name
        updateMoodColorNameUseCase(id = 1, newMood = "Joyful")

        // Then: Name should be updated, still deleted
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1)
        assertEquals("Name should be updated", "Joyful", updatedMood?.mood)
        assertEquals("Should still be deleted", true, updatedMood?.isDeleted)
    }
}
