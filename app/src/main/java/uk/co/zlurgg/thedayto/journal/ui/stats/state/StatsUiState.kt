package uk.co.zlurgg.thedayto.journal.ui.stats.state

import java.time.LocalDate

/**
 * Immutable UI state for the Stats screen
 */
data class StatsUiState(
    // All-time stats
    val totalEntries: Int = 0,
    val firstEntryDate: LocalDate? = null,
    val averageEntriesPerMonth: Float = 0f,

    // Mood insights
    val moodDistribution: List<MoodCount> = emptyList(),

    // Monthly breakdown
    val monthlyBreakdown: List<MonthStats> = emptyList(),

    // UI state
    val isLoading: Boolean = true,
    val isEmpty: Boolean = false
) {
    /**
     * Mood count with color for display
     */
    data class MoodCount(
        val mood: String,
        val color: String,
        val count: Int
    )

    /**
     * Stats for a specific month
     */
    data class MonthStats(
        val month: String,      // "October 2024"
        val year: Int,
        val monthValue: Int,    // 1-12
        val entryCount: Int,
        val completionRate: Int // percentage (0-100)
    )
}
