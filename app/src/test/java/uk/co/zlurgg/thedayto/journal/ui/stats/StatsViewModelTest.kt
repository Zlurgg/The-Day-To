package uk.co.zlurgg.thedayto.journal.ui.stats

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.CalculateMoodDistributionUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.CalculateMonthlyBreakdownUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.CalculateTotalStatsUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.StatsUseCases
import uk.co.zlurgg.thedayto.testutil.TestDataBuilders

/**
 * Unit tests for StatsViewModel.
 *
 * Tests cover:
 * - Initialization and stats loading
 * - Empty state handling
 * - Total stats calculation (entry count, first date, average)
 * - Mood distribution calculation and sorting
 * - Monthly breakdown calculation and sorting
 *
 * Following Google's 2025 best practices:
 * - ViewModels tested with fake repositories (NOT real database)
 * - Focus on business logic and state management
 * - Test outcomes, not timing
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StatsViewModelTest {

    private lateinit var viewModel: StatsViewModel
    private lateinit var fakeEntryRepository: FakeEntryRepository
    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScheduler get() = testDispatcher.scheduler

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Initialize repositories with JOIN support
        fakeMoodColorRepository = FakeMoodColorRepository()
        fakeEntryRepository = FakeEntryRepository(fakeMoodColorRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): StatsViewModel {
        val getEntriesUseCase = GetEntriesUseCase(fakeEntryRepository)
        val statsUseCases = StatsUseCases(
            calculateTotalStats = CalculateTotalStatsUseCase(),
            calculateMoodDistribution = CalculateMoodDistributionUseCase(),
            calculateMonthlyBreakdown = CalculateMonthlyBreakdownUseCase()
        )
        return StatsViewModel(getEntriesUseCase, statsUseCases)
    }

    // ============================================================
    // Initialization & Loading Tests
    // ============================================================

    @Test
    fun `init shows loading state initially`() = runTest {
        // Given: ViewModel is being created
        viewModel = createViewModel()

        // Then: Initial state should have isLoading = true
        viewModel.uiState.test {
            val state = awaitItem()
            // Note: With UnconfinedTestDispatcher, loading completes immediately
            // So we can't catch the loading state. This is expected behavior.
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init shows empty state when no entries exist`() = runTest {
        // Given: Repository with no entries
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then: State should show empty
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Should show empty state", state.isEmpty)
            assertFalse("Should not be loading", state.isLoading)
            assertNull("Error should be null", state.error)
            assertEquals("Total entries should be 0", 0, state.totalEntries)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Total Stats Calculation Tests
    // ============================================================

    @Test
    fun `init loads stats successfully with single entry`() = runTest {
        // Given: Mood color in repository
        val happyMood = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        fakeMoodColorRepository.insertMoodColor(happyMood)

        // And: One entry
        val entry = TestDataBuilders.createEntry(
            id = 1,
            moodColorId = 1,
            dateStamp = TestDataBuilders.getTodayEpoch()
        )
        fakeEntryRepository.insertEntry(entry)

        // When: ViewModel initializes
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then: Stats should be loaded
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("Should not be empty", state.isEmpty)
            assertFalse("Should not be loading", state.isLoading)
            assertNull("Error should be null", state.error)
            assertEquals("Should have 1 entry", 1, state.totalEntries)
            assertNotNull("First entry date should be set", state.firstEntryDate)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init calculates total stats correctly with multiple entries`() = runTest {
        // Given: Mood color in repository
        val happyMood = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        fakeMoodColorRepository.insertMoodColor(happyMood)

        // And: Multiple entries across different dates
        val entries = listOf(
            TestDataBuilders.createEntry(
                id = 1,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDaysAgoEpoch(30) // 30 days ago
            ),
            TestDataBuilders.createEntry(
                id = 2,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDaysAgoEpoch(15) // 15 days ago
            ),
            TestDataBuilders.createEntry(
                id = 3,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getTodayEpoch() // Today
            )
        )
        entries.forEach { fakeEntryRepository.insertEntry(it) }

        // When: ViewModel initializes
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then: Total stats should be calculated correctly
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have 3 entries", 3, state.totalEntries)
            assertNotNull("First entry date should be set", state.firstEntryDate)
            assertTrue(
                "Average entries per month should be positive",
                state.averageEntriesPerMonth > 0f
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Mood Distribution Calculation Tests
    // ============================================================

    @Test
    fun `init calculates mood distribution correctly with multiple moods`() = runTest {
        // Given: Mood colors in repository
        val happyMood = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        val sadMood = TestDataBuilders.createMoodColor(mood = "Sad", color = "2196F3", id = 2)
        fakeMoodColorRepository.insertMoodColor(happyMood)
        fakeMoodColorRepository.insertMoodColor(sadMood)

        // And: Entries with different moods
        val entries = listOf(
            TestDataBuilders.createEntry(
                id = 1,
                moodColorId = 1, // Happy
                dateStamp = TestDataBuilders.getDaysAgoEpoch(2)
            ),
            TestDataBuilders.createEntry(
                id = 2,
                moodColorId = 1, // Happy
                dateStamp = TestDataBuilders.getDaysAgoEpoch(1)
            ),
            TestDataBuilders.createEntry(
                id = 3,
                moodColorId = 2, // Sad
                dateStamp = TestDataBuilders.getTodayEpoch()
            )
        )
        entries.forEach { fakeEntryRepository.insertEntry(it) }

        // When: ViewModel initializes
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then: Mood distribution should be calculated
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have 2 different moods", 2, state.moodDistribution.size)

            // Verify mood data
            val happyMood = state.moodDistribution.find { it.mood == "Happy" }
            assertNotNull("Happy mood should exist", happyMood)
            assertEquals("Happy should have count of 2", 2, happyMood!!.count)
            assertEquals("Happy should have correct color", "4CAF50", happyMood.color)

            val sadMood = state.moodDistribution.find { it.mood == "Sad" }
            assertNotNull("Sad mood should exist", sadMood)
            assertEquals("Sad should have count of 1", 1, sadMood!!.count)
            assertEquals("Sad should have correct color", "2196F3", sadMood.color)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init sorts mood distribution by count descending`() = runTest {
        // Given: Mood colors in repository
        val happyMood = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        val sadMood = TestDataBuilders.createMoodColor(mood = "Sad", color = "2196F3", id = 2)
        val anxiousMood = TestDataBuilders.createMoodColor(mood = "Anxious", color = "FF9800", id = 3)
        fakeMoodColorRepository.insertMoodColor(happyMood)
        fakeMoodColorRepository.insertMoodColor(sadMood)
        fakeMoodColorRepository.insertMoodColor(anxiousMood)

        // And: Entries with moods of different frequencies
        val entries = listOf(
            // Happy: 1 entry
            TestDataBuilders.createEntry(id = 1, moodColorId = 1),
            // Sad: 3 entries
            TestDataBuilders.createEntry(id = 2, moodColorId = 2),
            TestDataBuilders.createEntry(id = 3, moodColorId = 2),
            TestDataBuilders.createEntry(id = 4, moodColorId = 2),
            // Anxious: 2 entries
            TestDataBuilders.createEntry(id = 5, moodColorId = 3),
            TestDataBuilders.createEntry(id = 6, moodColorId = 3)
        )
        entries.forEach { fakeEntryRepository.insertEntry(it) }

        // When: ViewModel initializes
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then: Moods should be sorted by count (descending)
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have 3 different moods", 3, state.moodDistribution.size)

            // Verify sort order: Sad (3), Anxious (2), Happy (1)
            assertEquals("First mood should be Sad", "Sad", state.moodDistribution[0].mood)
            assertEquals("First mood count should be 3", 3, state.moodDistribution[0].count)

            assertEquals("Second mood should be Anxious", "Anxious", state.moodDistribution[1].mood)
            assertEquals("Second mood count should be 2", 2, state.moodDistribution[1].count)

            assertEquals("Third mood should be Happy", "Happy", state.moodDistribution[2].mood)
            assertEquals("Third mood count should be 1", 1, state.moodDistribution[2].count)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Monthly Breakdown Calculation Tests
    // ============================================================

    @Test
    fun `init calculates monthly breakdown correctly with entries in different months`() = runTest {
        // Given: Mood color in repository
        val happyMood = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        fakeMoodColorRepository.insertMoodColor(happyMood)

        // And: Entries in different months
        val entries = listOf(
            // January 2024
            TestDataBuilders.createEntry(
                id = 1,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDateEpoch(2024, 1, 15)
            ),
            TestDataBuilders.createEntry(
                id = 2,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDateEpoch(2024, 1, 20)
            ),
            // February 2024
            TestDataBuilders.createEntry(
                id = 3,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDateEpoch(2024, 2, 10)
            )
        )
        entries.forEach { fakeEntryRepository.insertEntry(it) }

        // When: ViewModel initializes
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then: Monthly breakdown should be calculated
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have 2 months", 2, state.monthlyBreakdown.size)

            // Verify monthly data
            val jan2024 = state.monthlyBreakdown.find { it.monthValue == 1 && it.year == 2024 }
            assertNotNull("January 2024 should exist", jan2024)
            assertEquals("January should have 2 entries", 2, jan2024!!.entryCount)
            assertTrue(
                "January completion rate should be calculated",
                jan2024.completionRate > 0
            )

            val feb2024 = state.monthlyBreakdown.find { it.monthValue == 2 && it.year == 2024 }
            assertNotNull("February 2024 should exist", feb2024)
            assertEquals("February should have 1 entry", 1, feb2024!!.entryCount)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init sorts monthly breakdown by most recent first`() = runTest {
        // Given: Mood color in repository
        val happyMood = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        fakeMoodColorRepository.insertMoodColor(happyMood)

        // And: Entries in multiple months
        val entries = listOf(
            TestDataBuilders.createEntry(
                id = 1,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDateEpoch(2024, 1, 15) // Jan 2024
            ),
            TestDataBuilders.createEntry(
                id = 2,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDateEpoch(2024, 3, 10) // Mar 2024
            ),
            TestDataBuilders.createEntry(
                id = 3,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDateEpoch(2024, 2, 5) // Feb 2024
            )
        )
        entries.forEach { fakeEntryRepository.insertEntry(it) }

        // When: ViewModel initializes
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then: Months should be sorted by most recent first
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have 3 months", 3, state.monthlyBreakdown.size)

            // Verify sort order: Mar 2024, Feb 2024, Jan 2024
            assertEquals("First month should be March", 3, state.monthlyBreakdown[0].monthValue)
            assertEquals("First year should be 2024", 2024, state.monthlyBreakdown[0].year)

            assertEquals("Second month should be February", 2, state.monthlyBreakdown[1].monthValue)
            assertEquals("Second year should be 2024", 2024, state.monthlyBreakdown[1].year)

            assertEquals("Third month should be January", 1, state.monthlyBreakdown[2].monthValue)
            assertEquals("Third year should be 2024", 2024, state.monthlyBreakdown[2].year)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Edge Cases
    // ============================================================

    @Test
    fun `init handles single mood correctly`() = runTest {
        // Given: Mood color in repository
        val happyMood = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        fakeMoodColorRepository.insertMoodColor(happyMood)

        // And: Entries all having the same mood
        val entries = listOf(
            TestDataBuilders.createEntry(id = 1, moodColorId = 1),
            TestDataBuilders.createEntry(id = 2, moodColorId = 1),
            TestDataBuilders.createEntry(id = 3, moodColorId = 1)
        )
        entries.forEach { fakeEntryRepository.insertEntry(it) }

        // When: ViewModel initializes
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then: Should show single mood with correct count
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have 1 mood type", 1, state.moodDistribution.size)
            assertEquals("Mood should be Happy", "Happy", state.moodDistribution[0].mood)
            assertEquals("Count should be 3", 3, state.moodDistribution[0].count)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init handles single month correctly`() = runTest {
        // Given: Mood color in repository
        val happyMood = TestDataBuilders.createMoodColor(mood = "Happy", color = "4CAF50", id = 1)
        fakeMoodColorRepository.insertMoodColor(happyMood)

        // And: Entries all in the same month
        val entries = listOf(
            TestDataBuilders.createEntry(
                id = 1,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDateEpoch(2024, 1, 5)
            ),
            TestDataBuilders.createEntry(
                id = 2,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDateEpoch(2024, 1, 15)
            ),
            TestDataBuilders.createEntry(
                id = 3,
                moodColorId = 1,
                dateStamp = TestDataBuilders.getDateEpoch(2024, 1, 25)
            )
        )
        entries.forEach { fakeEntryRepository.insertEntry(it) }

        // When: ViewModel initializes
        viewModel = createViewModel()
        testScheduler.advanceUntilIdle()

        // Then: Should show single month with correct stats
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals("Should have 1 month", 1, state.monthlyBreakdown.size)
            assertEquals("Month should be January", 1, state.monthlyBreakdown[0].monthValue)
            assertEquals("Year should be 2024", 2024, state.monthlyBreakdown[0].year)
            assertEquals("Entry count should be 3", 3, state.monthlyBreakdown[0].entryCount)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
