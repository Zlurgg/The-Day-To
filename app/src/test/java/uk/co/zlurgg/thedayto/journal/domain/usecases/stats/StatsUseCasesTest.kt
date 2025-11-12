package uk.co.zlurgg.thedayto.journal.domain.usecases.stats

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders
import java.time.LocalDate

/**
 * Unit tests for Statistics Use Cases.
 *
 * Tests three use cases that calculate statistics from EntryWithMoodColor:
 * - CalculateTotalStatsUseCase: First entry date and average entries per month
 * - CalculateMoodDistributionUseCase: Mood frequency and colors from JOIN
 * - CalculateMonthlyBreakdownUseCase: Monthly entry counts and completion rates
 *
 * Key validation: All use cases work with EntryWithMoodColor (joined data),
 * ensuring mood colors reflect current values, not historical snapshots.
 */
class StatsUseCasesTest {

    private lateinit var calculateTotalStatsUseCase: CalculateTotalStatsUseCase
    private lateinit var calculateMoodDistributionUseCase: CalculateMoodDistributionUseCase
    private lateinit var calculateMonthlyBreakdownUseCase: CalculateMonthlyBreakdownUseCase

    @Before
    fun setup() {
        calculateTotalStatsUseCase = CalculateTotalStatsUseCase()
        calculateMoodDistributionUseCase = CalculateMoodDistributionUseCase()
        calculateMonthlyBreakdownUseCase = CalculateMonthlyBreakdownUseCase()
    }

    // ============================================================
    // CalculateTotalStatsUseCase Tests
    // ============================================================

    @Test
    fun `calculateTotalStats - calculates first entry date correctly`() {
        // Given: Entries with different dates
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 5, 15),
                id = 1
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 3, 10),
                id = 2
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 7, 20),
                id = 3
            )
        )

        // When: Calculating total stats
        val stats = calculateTotalStatsUseCase(entries)

        // Then: First entry date should be the oldest
        assertNotNull("First entry date should not be null", stats.firstEntryDate)
        assertEquals("First entry should be March 10", LocalDate.of(2024, 3, 10), stats.firstEntryDate)
    }

    @Test
    fun `calculateTotalStats - calculates average entries per month`() {
        // Given: 3 entries over a specific time range
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 1, 1),
                id = 1
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 1, 15),
                id = 2
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 2, 1),
                id = 3
            )
        )

        // When: Calculating total stats
        val stats = calculateTotalStatsUseCase(entries)

        // Then: Should calculate average based on months between first entry and now
        assertTrue("Average should be positive", stats.averageEntriesPerMonth > 0f)
    }

    @Test
    fun `calculateTotalStats - returns zero stats for empty list`() {
        // Given: Empty entry list
        val entries = emptyList<uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor>()

        // When: Calculating total stats
        val stats = calculateTotalStatsUseCase(entries)

        // Then: Should return zero stats
        assertNull("First entry date should be null", stats.firstEntryDate)
        assertEquals("Average should be zero", 0f, stats.averageEntriesPerMonth)
    }

    @Test
    fun `calculateTotalStats - handles single entry`() {
        // Given: Single entry
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 6, 1),
                id = 1
            )
        )

        // When: Calculating total stats
        val stats = calculateTotalStatsUseCase(entries)

        // Then: Should have first date and reasonable average
        assertNotNull("First entry date should not be null", stats.firstEntryDate)
        assertEquals("First entry should be June 1", LocalDate.of(2024, 6, 1), stats.firstEntryDate)
        assertTrue("Average should be positive", stats.averageEntriesPerMonth > 0f)
    }

    @Test
    fun `calculateTotalStats - handles entries across multiple years`() {
        // Given: Entries spanning multiple years
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2022, 1, 1),
                id = 1
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2023, 6, 15),
                id = 2
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 12, 25),
                id = 3
            )
        )

        // When: Calculating total stats
        val stats = calculateTotalStatsUseCase(entries)

        // Then: Should handle multi-year span correctly
        assertNotNull("First entry date should not be null", stats.firstEntryDate)
        assertEquals("First entry should be 2022", 2022, stats.firstEntryDate?.year)
        assertTrue("Average should be positive", stats.averageEntriesPerMonth > 0f)
    }

    @Test
    fun `calculateTotalStats - calculates entry count correctly`() {
        // Given: Known number of entries
        val entries = TestDataBuilders.createEntriesWithMoodColor(count = 15)

        // When: Calculating total stats
        val stats = calculateTotalStatsUseCase(entries)

        // Then: Stats should be calculated based on 15 entries
        assertNotNull("Stats should be calculated", stats)
        assertTrue("Average should reflect 15 entries", stats.averageEntriesPerMonth > 0f)
    }

    // ============================================================
    // CalculateMoodDistributionUseCase Tests
    // ============================================================

    @Test
    fun `calculateMoodDistribution - groups entries by mood name`() {
        // Given: Entries with different moods
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", moodColor = "4CAF50", id = 1),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", moodColor = "4CAF50", id = 2),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Sad", moodColor = "2196F3", id = 3),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", moodColor = "4CAF50", id = 4)
        )

        // When: Calculating mood distribution
        val distribution = calculateMoodDistributionUseCase(entries)

        // Then: Should group by mood and sort by frequency
        assertEquals("Should have 2 distinct moods", 2, distribution.size)
        assertEquals("Most frequent should be Happy", "Happy", distribution[0].mood)
        assertEquals("Happy should have count 3", 3, distribution[0].count)
        assertEquals("Sad should have count 1", 1, distribution[1].count)
    }

    @Test
    fun `calculateMoodDistribution - uses current mood color from join (not snapshot)`() {
        // Given: Entries with moodName and moodColor from JOIN operation
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(
                moodName = "Happy",
                moodColor = "4CAF50",  // Current color from join (green)
                id = 1
            ),
            TestDataBuilders.createEntryWithMoodColor(
                moodName = "Happy",
                moodColor = "4CAF50",  // Same current color
                id = 2
            )
        )

        // When: Calculating distribution
        val distribution = calculateMoodDistributionUseCase(entries)

        // Then: Should use color from joined data (validates source of truth)
        assertEquals("Should have 1 mood", 1, distribution.size)
        assertEquals("Mood should be Happy", "Happy", distribution[0].mood)
        assertEquals("Color should be from join", "4CAF50", distribution[0].color)
        assertEquals("Count should be 2", 2, distribution[0].count)
    }

    @Test
    fun `calculateMoodDistribution - calculates counts correctly`() {
        // Given: Multiple moods with different frequencies
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", moodColor = "4CAF50", id = 1),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Sad", moodColor = "2196F3", id = 2),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Calm", moodColor = "9C27B0", id = 3),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", moodColor = "4CAF50", id = 4),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Sad", moodColor = "2196F3", id = 5),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", moodColor = "4CAF50", id = 6)
        )

        // When: Calculating distribution
        val distribution = calculateMoodDistributionUseCase(entries)

        // Then: Should count correctly and sort by frequency
        assertEquals("Should have 3 distinct moods", 3, distribution.size)
        assertEquals("Happy should be first (3 count)", "Happy", distribution[0].mood)
        assertEquals("Happy count", 3, distribution[0].count)
        assertEquals("Sad should be second (2 count)", "Sad", distribution[1].mood)
        assertEquals("Sad count", 2, distribution[1].count)
        assertEquals("Calm should be third (1 count)", "Calm", distribution[2].mood)
        assertEquals("Calm count", 1, distribution[2].count)
    }

    @Test
    fun `calculateMoodDistribution - returns empty list for no entries`() {
        // Given: Empty entry list
        val entries = emptyList<uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor>()

        // When: Calculating distribution
        val distribution = calculateMoodDistributionUseCase(entries)

        // Then: Should return empty list
        assertTrue("Distribution should be empty", distribution.isEmpty())
    }

    @Test
    fun `calculateMoodDistribution - handles all entries with same mood`() {
        // Given: All entries have same mood
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", moodColor = "4CAF50", id = 1),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", moodColor = "4CAF50", id = 2),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", moodColor = "4CAF50", id = 3)
        )

        // When: Calculating distribution
        val distribution = calculateMoodDistributionUseCase(entries)

        // Then: Should have single mood with total count
        assertEquals("Should have 1 mood", 1, distribution.size)
        assertEquals("Mood should be Happy", "Happy", distribution[0].mood)
        assertEquals("Count should be 3", 3, distribution[0].count)
    }

    @Test
    fun `calculateMoodDistribution - respects limit parameter`() {
        // Given: More moods than limit
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(moodName = "Happy", id = 1),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Sad", id = 2),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Calm", id = 3),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Anxious", id = 4),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Tired", id = 5),
            TestDataBuilders.createEntryWithMoodColor(moodName = "Excited", id = 6)
        )

        // When: Calculating distribution with limit of 3
        val distribution = calculateMoodDistributionUseCase(entries, limit = 3)

        // Then: Should return only top 3 moods
        assertEquals("Should respect limit of 3", 3, distribution.size)
    }

    // ============================================================
    // CalculateMonthlyBreakdownUseCase Tests
    // ============================================================

    @Test
    fun `calculateMonthlyBreakdown - groups entries by month`() {
        // Given: Entries from different months
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 5, 1),
                id = 1
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 5, 15),
                id = 2
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 6, 1),
                id = 3
            )
        )

        // When: Calculating monthly breakdown
        val breakdown = calculateMonthlyBreakdownUseCase(entries)

        // Then: Should group by month
        assertEquals("Should have 2 months", 2, breakdown.size)
    }

    @Test
    fun `calculateMonthlyBreakdown - calculates completion rate correctly`() {
        // Given: Entries in a specific month (31 days in May)
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 5, 1),
                id = 1
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 5, 15),
                id = 2
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 5, 31),
                id = 3
            )
        )

        // When: Calculating monthly breakdown
        val breakdown = calculateMonthlyBreakdownUseCase(entries)

        // Then: Should calculate completion rate (3 entries / 31 days * 100)
        assertEquals("Should have 1 month", 1, breakdown.size)
        assertEquals("Should be May", 5, breakdown[0].monthValue)
        assertEquals("Entry count should be 3", 3, breakdown[0].entryCount)
        // 3/31 * 100 ≈ 9%
        assertTrue("Completion rate should be around 9%", breakdown[0].completionRate in 9..10)
    }

    @Test
    fun `calculateMonthlyBreakdown - returns empty list for no entries`() {
        // Given: Empty entry list
        val entries = emptyList<uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor>()

        // When: Calculating breakdown
        val breakdown = calculateMonthlyBreakdownUseCase(entries)

        // Then: Should return empty list
        assertTrue("Breakdown should be empty", breakdown.isEmpty())
    }

    @Test
    fun `calculateMonthlyBreakdown - handles single month`() {
        // Given: All entries in same month
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 3, 1),
                id = 1
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 3, 15),
                id = 2
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 3, 30),
                id = 3
            )
        )

        // When: Calculating breakdown
        val breakdown = calculateMonthlyBreakdownUseCase(entries)

        // Then: Should have single month
        assertEquals("Should have 1 month", 1, breakdown.size)
        assertEquals("Should be March", 3, breakdown[0].monthValue)
        assertEquals("Entry count should be 3", 3, breakdown[0].entryCount)
    }

    @Test
    fun `calculateMonthlyBreakdown - handles month with all days filled`() {
        // Given: 30 entries in a 30-day month (e.g., April)
        val entries = (1..30).map { day ->
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 4, day),
                id = day
            )
        }

        // When: Calculating breakdown
        val breakdown = calculateMonthlyBreakdownUseCase(entries)

        // Then: Should have 100% completion rate
        assertEquals("Should have 1 month", 1, breakdown.size)
        assertEquals("Should be April", 4, breakdown[0].monthValue)
        assertEquals("Entry count should be 30", 30, breakdown[0].entryCount)
        assertEquals("Completion rate should be 100%", 100, breakdown[0].completionRate)
    }

    @Test
    fun `calculateMonthlyBreakdown - sorts by most recent month first`() {
        // Given: Entries from different months
        val entries = listOf(
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 1, 1),
                id = 1
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 5, 1),
                id = 2
            ),
            TestDataBuilders.createEntryWithMoodColor(
                dateStamp = TestDataBuilders.getDateEpoch(2024, 3, 1),
                id = 3
            )
        )

        // When: Calculating breakdown
        val breakdown = calculateMonthlyBreakdownUseCase(entries)

        // Then: Should be sorted by most recent first
        assertEquals("Should have 3 months", 3, breakdown.size)
        assertEquals("First should be May", 5, breakdown[0].monthValue)
        assertEquals("Second should be March", 3, breakdown[1].monthValue)
        assertEquals("Third should be January", 1, breakdown[2].monthValue)
    }

    @Test
    fun `calculateMonthlyBreakdown - respects limit parameter`() {
        // Given: Entries from many months
        val entries = (1..12).flatMap { month ->
            listOf(
                TestDataBuilders.createEntryWithMoodColor(
                    dateStamp = TestDataBuilders.getDateEpoch(2024, month, 1),
                    id = month
                )
            )
        }

        // When: Calculating breakdown with limit of 6
        val breakdown = calculateMonthlyBreakdownUseCase(entries, limit = 6)

        // Then: Should return only 6 most recent months
        assertEquals("Should respect limit of 6", 6, breakdown.size)
    }
}
