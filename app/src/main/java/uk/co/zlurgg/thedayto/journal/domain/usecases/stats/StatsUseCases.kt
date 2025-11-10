package uk.co.zlurgg.thedayto.journal.domain.usecases.stats

/**
 * Aggregator for all stats-related use cases
 * Following the same pattern as OverviewUseCases and EditorUseCases
 */
data class StatsUseCases(
    val calculateTotalStats: CalculateTotalStatsUseCase,
    val calculateMoodDistribution: CalculateMoodDistributionUseCase,
    val calculateMonthlyBreakdown: CalculateMonthlyBreakdownUseCase
)
