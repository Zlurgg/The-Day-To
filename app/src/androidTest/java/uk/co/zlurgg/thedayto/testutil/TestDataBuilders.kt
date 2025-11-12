package uk.co.zlurgg.thedayto.testutil

import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Test data builders for creating domain model instances in tests.
 * Provides sensible defaults and builder pattern for easy customization.
 */
object TestDataBuilders {

    /**
     * Creates a test MoodColor with customizable properties.
     */
    fun createMoodColor(
        mood: String = "Happy",
        color: String = "4CAF50",
        isDeleted: Boolean = false,
        dateStamp: Long = System.currentTimeMillis(),
        id: Int? = 1
    ): MoodColor {
        return MoodColor(
            mood = mood,
            color = color,
            isDeleted = isDeleted,
            dateStamp = dateStamp,
            id = id
        )
    }

    /**
     * Creates a test Entry with customizable properties.
     */
    fun createEntry(
        moodColorId: Int = 1,
        content: String = "Test entry content",
        dateStamp: Long = getTodayEpoch(),
        id: Int? = 1
    ): Entry {
        return Entry(
            moodColorId = moodColorId,
            content = content,
            dateStamp = dateStamp,
            id = id
        )
    }

    /**
     * Creates a test EntryWithMoodColor with customizable properties.
     */
    fun createEntryWithMoodColor(
        moodColorId: Int = 1,
        moodName: String = "Happy",
        moodColor: String = "4CAF50",
        content: String = "Test entry content",
        dateStamp: Long = getTodayEpoch(),
        id: Int? = 1
    ): EntryWithMoodColor {
        return EntryWithMoodColor(
            id = id,
            moodColorId = moodColorId,
            moodName = moodName,
            moodColor = moodColor,
            content = content,
            dateStamp = dateStamp
        )
    }

    /**
     * Creates multiple test MoodColors.
     */
    fun createMoodColors(count: Int): List<MoodColor> {
        val moods = listOf("Happy", "Sad", "Angry", "Calm", "Anxious", "Excited", "Tired", "Focused")
        val colors = listOf("4CAF50", "2196F3", "F44336", "9C27B0", "FF9800", "FFEB3B", "607D8B", "00BCD4")

        return (1..count).map { i ->
            createMoodColor(
                mood = moods[i % moods.size],
                color = colors[i % colors.size],
                id = i
            )
        }
    }

    /**
     * Creates multiple test Entries.
     */
    fun createEntries(count: Int, moodColorId: Int = 1): List<Entry> {
        return (1..count).map { i ->
            createEntry(
                moodColorId = moodColorId,
                content = "Test entry $i",
                dateStamp = getTodayEpoch() - (i * 86400L), // One day apart
                id = i
            )
        }
    }

    /**
     * Creates multiple test EntriesWithMoodColor.
     */
    fun createEntriesWithMoodColor(
        count: Int,
        moodColorId: Int = 1,
        moodName: String = "Happy",
        moodColor: String = "4CAF50"
    ): List<EntryWithMoodColor> {
        return (1..count).map { i ->
            createEntryWithMoodColor(
                moodColorId = moodColorId,
                moodName = moodName,
                moodColor = moodColor,
                content = "Test entry $i",
                dateStamp = getTodayEpoch() - (i * 86400L), // One day apart
                id = i
            )
        }
    }

    /**
     * Gets epoch timestamp for start of today (00:00:00).
     */
    fun getTodayEpoch(): Long {
        return LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
    }

    /**
     * Gets epoch timestamp for start of a specific date.
     */
    fun getDateEpoch(year: Int, month: Int, day: Int): Long {
        return LocalDate.of(year, month, day).atStartOfDay().toEpochSecond(ZoneOffset.UTC)
    }

    /**
     * Gets epoch timestamp for N days ago.
     */
    fun getDaysAgoEpoch(days: Long): Long {
        return getTodayEpoch() - (days * 86400L)
    }

    /**
     * Default test mood colors for consistent testing.
     */
    val DEFAULT_MOOD_COLORS = listOf(
        createMoodColor(mood = "Happy", color = "4CAF50", id = 1),
        createMoodColor(mood = "Sad", color = "2196F3", id = 2),
        createMoodColor(mood = "Angry", color = "F44336", id = 3),
        createMoodColor(mood = "Calm", color = "9C27B0", id = 4),
        createMoodColor(mood = "Anxious", color = "FF9800", id = 5)
    )
}
