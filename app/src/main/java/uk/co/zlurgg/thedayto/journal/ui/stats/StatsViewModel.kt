package uk.co.zlurgg.thedayto.journal.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.entry.GetEntriesUseCase
import uk.co.zlurgg.thedayto.journal.domain.usecases.stats.StatsUseCases
import uk.co.zlurgg.thedayto.journal.ui.stats.state.StatsAction
import uk.co.zlurgg.thedayto.journal.ui.stats.state.StatsUiState

class StatsViewModel(
    private val getEntriesUseCase: GetEntriesUseCase,
    private val statsUseCases: StatsUseCases
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun onAction(action: StatsAction) {
        when (action) {
            is StatsAction.Refresh -> loadStats()
            is StatsAction.NavigateBack -> {
                // Navigation handled in screen composable
            }
        }
    }

    private fun loadStats() {
        viewModelScope.launch {
            Timber.d("Loading stats")
            getEntriesUseCase().collect { entries ->
                Timber.d("Calculating stats for ${entries.size} entries")

                if (entries.isEmpty()) {
                    _uiState.update {
                        it.copy(
                            isEmpty = true,
                            isLoading = false
                        )
                    }
                    return@collect
                }

                // Delegate calculations to use cases
                val totalStats = statsUseCases.calculateTotalStats(entries)
                val moodDistribution = statsUseCases.calculateMoodDistribution(entries)
                val monthlyBreakdown = statsUseCases.calculateMonthlyBreakdown(entries)

                _uiState.update {
                    it.copy(
                        totalEntries = entries.size,
                        firstEntryDate = totalStats.firstEntryDate,
                        averageEntriesPerMonth = totalStats.averageEntriesPerMonth,
                        moodDistribution = moodDistribution.map { mood ->
                            StatsUiState.MoodCount(
                                mood = mood.mood,
                                color = mood.color,
                                count = mood.count
                            )
                        },
                        monthlyBreakdown = monthlyBreakdown.map { month ->
                            StatsUiState.MonthStats(
                                month = month.month,
                                year = month.year,
                                monthValue = month.monthValue,
                                entryCount = month.entryCount,
                                completionRate = month.completionRate
                            )
                        },
                        isEmpty = false,
                        isLoading = false
                    )
                }

                Timber.d("Stats calculated successfully")
            }
        }
    }
}
