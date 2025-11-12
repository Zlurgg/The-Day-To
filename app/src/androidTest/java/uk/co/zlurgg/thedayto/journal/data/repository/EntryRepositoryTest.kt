package uk.co.zlurgg.thedayto.journal.data.repository

import android.database.sqlite.SQLiteConstraintException
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.zlurgg.thedayto.base.DatabaseTest
import uk.co.zlurgg.thedayto.journal.data.mapper.toDomain
import uk.co.zlurgg.thedayto.journal.data.mapper.toEntity
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Instrumented tests for EntryRepository with real Room database.
 *
 * Critical validations:
 * - Foreign key constraints (moodColorId must exist)
 * - JOIN queries return correct mood color data
 * - Soft-deleted mood colors still work in JOINs
 * - Flow emissions on database changes
 */
@RunWith(AndroidJUnit4::class)
class EntryRepositoryTest : DatabaseTest() {

    private lateinit var repository: EntryRepositoryImpl

    @Before
    fun setupRepository() {
        repository = EntryRepositoryImpl(entryDao)
    }

    // ============================================================
    // JOIN Query Tests
    // ============================================================

    @Test
    fun getEntriesWithMoodColors_returns_correct_joined_data() = runTest {
        // Given: A mood color and an entry
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Great day!", id = 1)
        entryDao.insertEntry(entry.toEntity())

        // When: Getting entries with mood colors
        repository.getEntriesWithMoodColors().test {
            val entries = awaitItem()

            // Then: Should return entry with mood color data from JOIN
            assertEquals("Should have 1 entry", 1, entries.size)
            assertEquals("Mood name should match", "Happy", entries[0].moodName)
            assertEquals("Mood color should match", "4CAF50", entries[0].moodColor)
            assertEquals("MoodColorId should match", 1, entries[0].moodColorId)
            assertEquals("Content should match", "Great day!", entries[0].content)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getEntriesWithMoodColors_handles_deleted_mood_colors_in_join() = runTest {
        // Given: A soft-deleted mood color and an entry
        val deletedMood = TestDataBuilders.createMoodColor(
            mood = "Sad",
            color = "2196F3",
            isDeleted = true,
            id = 1
        )
        moodColorDao.insertMoodColor(deletedMood.toEntity())

        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Feeling down", id = 1)
        entryDao.insertEntry(entry.toEntity())

        // When: Getting entries with mood colors
        repository.getEntriesWithMoodColors().test {
            val entries = awaitItem()

            // Then: JOIN should still work with deleted mood color
            assertEquals("Should have 1 entry", 1, entries.size)
            assertEquals("Mood name should match deleted mood", "Sad", entries[0].moodName)
            assertEquals("Mood color should match deleted mood", "2196F3", entries[0].moodColor)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getEntryWithMoodColorById_returns_joined_data_for_single_entry() = runTest {
        // Given: A mood color and an entry
        val moodColor = TestDataBuilders.createMoodColor(mood = "Calm", color = "9C27B0", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Peaceful day", id = 5)
        entryDao.insertEntry(entry.toEntity())

        // When: Getting entry by ID with mood color
        val entryWithMoodColor = repository.getEntryWithMoodColorById(5)

        // Then: Should return entry with mood color data
        assertNotNull("Entry should exist", entryWithMoodColor)
        assertEquals("Mood name should match", "Calm", entryWithMoodColor!!.moodName)
        assertEquals("Mood color should match", "9C27B0", entryWithMoodColor.moodColor)
        assertEquals("Content should match", "Peaceful day", entryWithMoodColor.content)
    }

    @Test
    fun getEntryWithMoodColorByDate_date_lookup_with_join_works() = runTest {
        // Given: A mood color and an entry with specific date
        val moodColor = TestDataBuilders.createMoodColor(mood = "Excited", color = "FFEB3B", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val specificDate = TestDataBuilders.getDateEpoch(2024, 11, 12)
        val entry = TestDataBuilders.createEntry(
            moodColorId = 1,
            content = "Birthday!",
            dateStamp = specificDate,
            id = 1
        )
        entryDao.insertEntry(entry.toEntity())

        // When: Getting entry by date with mood color
        val entryWithMoodColor = repository.getEntryWithMoodColorByDate(specificDate)

        // Then: Should find entry with mood color data
        assertNotNull("Entry should exist", entryWithMoodColor)
        assertEquals("Mood name should match", "Excited", entryWithMoodColor!!.moodName)
        assertEquals("Content should match", "Birthday!", entryWithMoodColor.content)
    }

    // ============================================================
    // CRUD Operations Tests
    // ============================================================

    @Test
    fun insertEntry_creates_entry_with_foreign_key_constraint() = runTest {
        // Given: A valid mood color exists
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        // When: Inserting entry with valid moodColorId
        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Test entry", id = 1)
        repository.insertEntry(entry)

        // Then: Entry should be inserted
        val retrievedEntry = entryDao.getEntryById(1)?.toDomain()
        assertNotNull("Entry should exist", retrievedEntry)
        assertEquals("MoodColorId should match", 1, retrievedEntry!!.moodColorId)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun insertEntry_fails_when_moodColorId_does_not_exist() = runTest {
        // Given: Empty mood color table (no moods exist)

        // When: Trying to insert entry with non-existent moodColorId
        val entry = TestDataBuilders.createEntry(moodColorId = 999, content = "Test", id = 1)

        // Then: Should throw SQLiteConstraintException (foreign key violation)
        repository.insertEntry(entry)
    }

    @Test
    fun deleteEntry_removes_entry_from_database() = runTest {
        // Given: A mood color and an entry
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry = TestDataBuilders.createEntry(moodColorId = 1, id = 1)
        repository.insertEntry(entry)

        // Verify entry exists
        val beforeDelete = repository.getEntryById(1)
        assertNotNull("Entry should exist before delete", beforeDelete)

        // When: Deleting the entry
        repository.deleteEntry(entry)

        // Then: Entry should be removed
        val afterDelete = repository.getEntryById(1)
        assertNull("Entry should not exist after delete", afterDelete)
    }

    @Test
    fun updateEntry_modifies_existing_entry() = runTest {
        // Given: A mood color and an entry
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry = TestDataBuilders.createEntry(moodColorId = 1, content = "Original", id = 1)
        repository.insertEntry(entry)

        // When: Updating the entry content
        val updatedEntry = entry.copy(content = "Updated content")
        repository.updateEntry(updatedEntry)

        // Then: Entry should be updated
        val retrieved = repository.getEntryById(1)
        assertNotNull("Entry should exist", retrieved)
        assertEquals("Content should be updated", "Updated content", retrieved!!.content)
    }

    // ============================================================
    // Flow Emission Tests
    // ============================================================

    @Test
    fun getEntriesWithMoodColors_emits_updates_when_entry_added() = runTest {
        // Given: A mood color
        val moodColor = TestDataBuilders.createMoodColor(id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        // When: Collecting entries flow and adding a new entry
        repository.getEntriesWithMoodColors().test {
            // Initially empty
            val initialEntries = awaitItem()
            assertEquals("Should start empty", 0, initialEntries.size)

            // Add an entry
            val entry = TestDataBuilders.createEntry(moodColorId = 1, id = 1)
            repository.insertEntry(entry)

            // Then: Should emit updated list
            val updatedEntries = awaitItem()
            assertEquals("Should have 1 entry after insert", 1, updatedEntries.size)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun getEntriesWithMoodColors_emits_updates_when_mood_color_updated() = runTest {
        // Given: A mood color and an entry
        val moodColor = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        moodColorDao.insertMoodColor(moodColor.toEntity())

        val entry = TestDataBuilders.createEntry(moodColorId = 1, id = 1)
        entryDao.insertEntry(entry.toEntity())

        // When: Collecting entries and updating mood color
        repository.getEntriesWithMoodColors().test {
            // Initial emission
            val initialEntries = awaitItem()
            assertEquals("Initial mood should be Happy", "Happy", initialEntries[0].moodName)
            assertEquals("Initial color should be 4CAF50", "4CAF50", initialEntries[0].moodColor)

            // Update mood color name and color
            val updatedMood = moodColor.copy(mood = "Joyful", color = "00FF00")
            moodColorDao.updateMoodColor(updatedMood.toEntity())

            // Then: Should emit updated entry with new mood data (from JOIN)
            val updatedEntries = awaitItem()
            assertEquals("Updated mood should be Joyful", "Joyful", updatedEntries[0].moodName)
            assertEquals("Updated color should be 00FF00", "00FF00", updatedEntries[0].moodColor)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
