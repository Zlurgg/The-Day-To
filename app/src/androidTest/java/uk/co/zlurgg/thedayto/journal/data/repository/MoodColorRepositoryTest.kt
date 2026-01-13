package uk.co.zlurgg.thedayto.journal.data.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.zlurgg.thedayto.base.DatabaseTest
import uk.co.zlurgg.thedayto.core.domain.result.getOrNull
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Instrumented tests for MoodColorRepository with real Room database.
 *
 * Critical validations:
 * - Soft delete sets isDeleted flag (not hard delete)
 * - Deleted moods still retrievable by ID (for joins)
 * - Deleted moods filtered from getMoodColors flow
 * - Case-insensitive name lookup
 * - Flow emissions on database changes
 */
@RunWith(AndroidJUnit4::class)
class MoodColorRepositoryTest : DatabaseTest() {

    private lateinit var repository: MoodColorRepositoryImpl

    @Before
    fun setupRepository() {
        repository = MoodColorRepositoryImpl(moodColorDao)
    }

    // ============================================================
    // CRUD Operations Tests
    // ============================================================

    @Test
    fun insertMoodColor_creates_new_mood_in_database() = runTest {
        // Given: A new mood color
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = null)

        // When: Inserting the mood color
        repository.insertMoodColor(moodColor)

        // Then: Should be created in database
        moodColorDao.getMoodColors().test {
            val moods = awaitItem()
            assertEquals("Should have 1 mood", 1, moods.size)
            assertEquals("Mood name should match", "Happy", moods[0].mood)
            assertEquals("Color should match", "4CAF50", moods[0].color)
            assertFalse("Should not be deleted", moods[0].isDeleted)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getMoodColors_returns_only_non_deleted_moods() = runTest {
        // Given: Multiple mood colors (some deleted, some not)
        val happy = TestDataBuilders.createMoodColor(mood = "Happy", isDeleted = false, id = 1)
        val sad = TestDataBuilders.createMoodColor(mood = "Sad", isDeleted = true, id = 2)
        val calm = TestDataBuilders.createMoodColor(mood = "Calm", isDeleted = false, id = 3)

        moodColorDao.insertMoodColor(happy.toEntity())
        moodColorDao.insertMoodColor(sad.toEntity())
        moodColorDao.insertMoodColor(calm.toEntity())

        // When: Getting mood colors via repository flow
        repository.getMoodColors().test {
            val moods = awaitItem()

            // Then: Should only return non-deleted moods
            assertEquals("Should have 2 non-deleted moods", 2, moods.size)
            assertTrue("Should include Happy", moods.any { it.mood == "Happy" })
            assertTrue("Should include Calm", moods.any { it.mood == "Calm" })
            assertFalse("Should not include Sad (deleted)", moods.any { it.mood == "Sad" })

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getMoodColorById_returns_mood_by_id() = runTest {
        // Given: A mood color
        val moodColor = TestDataBuilders.createMoodColor(mood = "Excited", color = "FFEB3B", id = 5)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        // When: Getting mood color by ID
        val retrieved = repository.getMoodColorById(5).getOrNull()

        // Then: Should return the mood color
        assertNotNull("Mood color should exist", retrieved)
        assertEquals("Mood name should match", "Excited", retrieved!!.mood)
        assertEquals("Color should match", "FFEB3B", retrieved.color)
        assertEquals("ID should match", 5, retrieved.id)
    }

    @Test
    fun getMoodColorById_returns_deleted_moods_for_joins() = runTest {
        // Given: A soft-deleted mood color
        val deletedMood = TestDataBuilders.createMoodColor(
            mood = "Sad",
            color = "2196F3",
            isDeleted = true,
            id = 1
        )
        moodColorDao.insertMoodColor(deletedMood.toEntity())

        // When: Getting deleted mood by ID
        val retrieved = repository.getMoodColorById(1).getOrNull()

        // Then: Should still be retrievable (important for joins!)
        assertNotNull("Deleted mood should still be retrievable by ID", retrieved)
        assertEquals("Mood name should match", "Sad", retrieved!!.mood)
        assertTrue("Should be marked as deleted", retrieved.isDeleted)
    }

    @Test
    fun getMoodColorByName_case_insensitive_lookup_works() = runTest {
        // Given: A mood color with specific casing
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        // When: Looking up with different casing
        val upperCase = repository.getMoodColorByName("HAPPY").getOrNull()
        val lowerCase = repository.getMoodColorByName("happy").getOrNull()
        val mixedCase = repository.getMoodColorByName("HaPpY").getOrNull()

        // Then: All should find the mood (case-insensitive)
        assertNotNull("Upper case should find mood", upperCase)
        assertNotNull("Lower case should find mood", lowerCase)
        assertNotNull("Mixed case should find mood", mixedCase)

        assertEquals("All should return same mood", 1, upperCase!!.id)
        assertEquals("All should return same mood", 1, lowerCase!!.id)
        assertEquals("All should return same mood", 1, mixedCase!!.id)
    }

    // ============================================================
    // Soft Delete Tests
    // ============================================================

    @Test
    fun deleteMoodColor_sets_isDeleted_flag_not_hard_delete() = runTest {
        // Given: A mood color
        val moodColor = TestDataBuilders.createMoodColor(mood = "Anxious", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        // Verify it's not deleted initially
        val beforeDelete = repository.getMoodColorById(1).getOrNull()
        assertNotNull("Mood should exist", beforeDelete)
        assertFalse("Should not be deleted initially", beforeDelete!!.isDeleted)

        // When: Soft-deleting the mood
        repository.deleteMoodColor(1)

        // Then: Should still exist but with isDeleted = true
        val afterDelete = repository.getMoodColorById(1).getOrNull()
        assertNotNull("Mood should still exist in database", afterDelete)
        assertTrue("Should be marked as deleted", afterDelete!!.isDeleted)
    }

    @Test
    fun deleteMoodColor_deleted_mood_not_in_getMoodColors_flow() = runTest {
        // Given: Two mood colors
        val happy = TestDataBuilders.createMoodColor(mood = "Happy", id = 1)
        val sad = TestDataBuilders.createMoodColor(mood = "Sad", id = 2)
        moodColorDao.insertMoodColor(happy.toEntity())
        moodColorDao.insertMoodColor(sad.toEntity())

        // When: Deleting one mood
        repository.deleteMoodColor(2)

        // Then: Deleted mood should not appear in getMoodColors flow
        repository.getMoodColors().test {
            val moods = awaitItem()

            assertEquals("Should have 1 non-deleted mood", 1, moods.size)
            assertEquals("Should only have Happy", "Happy", moods[0].mood)
            assertFalse("Should not include Sad", moods.any { it.mood == "Sad" })

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun deleteMoodColor_and_entry_join_still_works() = runTest {
        // Given: A mood color and an entry
        val mood = TestDataBuilders.createMoodColor(mood = "Tired", color = "607D8B", id = 1)
        moodColorDao.insertMoodColor(mood.toEntity())

        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Need sleep", id = 1)
        entryDao.insertEntry(entry.toEntity())

        // When: Soft-deleting the mood
        repository.deleteMoodColor(1)

        // Then: Entry join should still work (validates soft delete)
        val entryWithMood = entryDao.getEntryWithMoodColorById(1)
        assertNotNull("Entry with deleted mood should still be retrievable", entryWithMood)
        assertEquals("Join should return mood name", "Tired", entryWithMood!!.moodName)
        assertEquals("Join should return mood color", "607D8B", entryWithMood.moodColor)

        // And: Mood should be marked deleted
        val deletedMood = moodColorDao.getMoodColorById(1)
        assertTrue("Mood should be marked deleted", deletedMood!!.isDeleted)
    }

    @Test
    fun updateMoodColor_modifies_existing_mood() = runTest {
        // Given: A mood color
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        // When: Updating the mood color
        val updatedMood = moodColor.copy(mood = "Joyful", color = "00FF00")
        repository.updateMoodColor(updatedMood)

        // Then: Mood should be updated
        val retrieved = repository.getMoodColorById(1).getOrNull()
        assertNotNull("Mood should exist", retrieved)
        assertEquals("Mood name should be updated", "Joyful", retrieved!!.mood)
        assertEquals("Color should be updated", "00FF00", retrieved.color)
    }

    // ============================================================
    // Flow Emission Tests
    // ============================================================

    @Test
    fun getMoodColors_emits_updates_when_mood_added() = runTest {
        // When: Collecting mood colors flow and adding a new mood
        repository.getMoodColors().test {
            // Initially empty
            val initialMoods = awaitItem()
            assertEquals("Should start empty", 0, initialMoods.size)

            // Add a mood
            val newMood = TestDataBuilders.createMoodColor(mood = "Calm", id = 1)
            repository.insertMoodColor(newMood)

            // Then: Should emit updated list
            val updatedMoods = awaitItem()
            assertEquals("Should have 1 mood after insert", 1, updatedMoods.size)
            assertEquals("Mood should be Calm", "Calm", updatedMoods[0].mood)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
