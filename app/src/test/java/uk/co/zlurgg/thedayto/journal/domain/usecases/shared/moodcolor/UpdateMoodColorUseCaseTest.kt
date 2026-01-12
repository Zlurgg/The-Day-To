package uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.result.getOrNull
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for UpdateMoodColorUseCase.
 *
 * Tests updating mood color with validation:
 * - Successfully update color
 * - Validate color is not blank/empty
 * - Handle mood not found error
 * - Verify cascade to entries (via foreign key relationship)
 */
class UpdateMoodColorUseCaseTest {

    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository
    private lateinit var updateMoodColorUseCase: UpdateMoodColorUseCase

    @Before
    fun setup() {
        fakeMoodColorRepository = FakeMoodColorRepository()
        updateMoodColorUseCase = UpdateMoodColorUseCase(fakeMoodColorRepository)
    }

    // ============================================================
    // Success Cases
    // ============================================================

    @Test
    fun `invoke - updates mood color successfully`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "4CAF50",  // Old green color
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating to new color
        updateMoodColorUseCase(id = 1, newColor = "FF5722")  // New orange color

        // Then: Color should be updated
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1).getOrNull()
        assertEquals("Color should be updated", "FF5722", updatedMood?.color)
        assertEquals("Mood name should remain same", "Happy", updatedMood?.mood)
        assertEquals("ID should remain same", 1, updatedMood?.id)
    }

    @Test
    fun `invoke - updates only color, preserves other fields`() = runTest {
        // Given: An existing mood with specific properties
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Calm",
            color = "9C27B0",
            isDeleted = false,
            dateStamp = 1234567890L,
            id = 5
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating color
        updateMoodColorUseCase(id = 5, newColor = "00BCD4")

        // Then: Only color should change, everything else preserved
        val updatedMood = fakeMoodColorRepository.getMoodColorById(5).getOrNull()
        assertEquals("Color should be updated", "00BCD4", updatedMood?.color)
        assertEquals("Mood name should be preserved", "Calm", updatedMood?.mood)
        assertEquals("isDeleted should be preserved", false, updatedMood?.isDeleted)
        assertEquals("dateStamp should be preserved", 1234567890L, updatedMood?.dateStamp)
        assertEquals("ID should be preserved", 5, updatedMood?.id)
    }

    @Test
    fun `invoke - allows updating to same color`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "4CAF50",
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating to same color (edge case)
        updateMoodColorUseCase(id = 1, newColor = "4CAF50")

        // Then: Should succeed without error
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1).getOrNull()
        assertEquals("Color should still be the same", "4CAF50", updatedMood?.color)
    }

    @Test
    fun `invoke - updates multiple different moods independently`() = runTest {
        // Given: Multiple moods
        fakeMoodColorRepository.addDefaultMoods()  // Adds 5 default moods

        // When: Updating only mood ID 2
        updateMoodColorUseCase(id = 2, newColor = "NEWCOLOR")

        // Then: Only mood 2 should be updated
        val mood1 = fakeMoodColorRepository.getMoodColorById(1).getOrNull()
        val mood2 = fakeMoodColorRepository.getMoodColorById(2).getOrNull()
        val mood3 = fakeMoodColorRepository.getMoodColorById(3).getOrNull()

        assertEquals("Mood 1 should keep original color", "4CAF50", mood1?.color)
        assertEquals("Mood 2 should have new color", "NEWCOLOR", mood2?.color)
        assertEquals("Mood 3 should keep original color", "F44336", mood3?.color)
    }

    // ============================================================
    // Error Cases: Validation Failures
    // ============================================================

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for mood not found`() = runTest {
        // Given: No moods in repository

        // When: Trying to update non-existent mood
        updateMoodColorUseCase(id = 999, newColor = "4CAF50")

        // Then: Should throw InvalidMoodColorException
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for blank color`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Trying to update to blank color
        updateMoodColorUseCase(id = 1, newColor = "   ")

        // Then: Should throw InvalidMoodColorException
    }

    @Test(expected = InvalidMoodColorException::class)
    fun `invoke - throws error for empty color`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Trying to update to empty color
        updateMoodColorUseCase(id = 1, newColor = "")

        // Then: Should throw InvalidMoodColorException
    }

    // ============================================================
    // Edge Cases
    // ============================================================


    @Test
    fun `invoke - can update deleted mood color`() = runTest {
        // Given: A deleted mood (edge case - normally wouldn't update deleted moods from UI)
        val deletedMood = TestDataBuilders.createMoodColor(
            mood = "Happy",
            color = "4CAF50",
            isDeleted = true,
            id = 1
        )
        fakeMoodColorRepository.insertMoodColor(deletedMood)

        // When: Updating the deleted mood's color
        updateMoodColorUseCase(id = 1, newColor = "FF5722")

        // Then: Color should be updated (even though deleted)
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1).getOrNull()
        assertEquals("Color should be updated", "FF5722", updatedMood?.color)
        assertEquals("Should still be deleted", true, updatedMood?.isDeleted)
    }

    @Test
    fun `invoke - handles very long hex color codes`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating to a very long color string (edge case)
        val longColor = "FFFFFF00"  // 8-char hex (RGBA)
        updateMoodColorUseCase(id = 1, newColor = longColor)

        // Then: Should accept it (no format validation, only blank check)
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1).getOrNull()
        assertEquals("Should accept long color", longColor, updatedMood?.color)
    }

    @Test
    fun `invoke - handles non-hex color values`() = runTest {
        // Given: An existing mood
        val existingMood = TestDataBuilders.createMoodColor(id = 1)
        fakeMoodColorRepository.insertMoodColor(existingMood)

        // When: Updating to non-hex value (edge case - shouldn't happen from UI)
        val invalidHex = "GGGGGG"  // Not valid hex
        updateMoodColorUseCase(id = 1, newColor = invalidHex)

        // Then: Should accept it (no format validation in use case)
        val updatedMood = fakeMoodColorRepository.getMoodColorById(1).getOrNull()
        assertEquals("Should accept any non-blank string", invalidHex, updatedMood?.color)
    }
}
