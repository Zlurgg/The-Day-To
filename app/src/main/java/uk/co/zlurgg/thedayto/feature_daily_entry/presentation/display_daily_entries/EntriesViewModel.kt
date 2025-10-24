package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.zlurgg.thedayto.core.domain.util.DailyEntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.DailyEntryUseCases
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.state.EntriesAction
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.state.EntriesUiState

class EntriesViewModel(
    private val entryUseCase: DailyEntryUseCases
) : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(EntriesUiState())
    val uiState = _uiState.asStateFlow()

    private var getEntriesJob: Job? = null

    init {
        getEntries(DailyEntryOrder.Date(OrderType.Descending))
    }

    fun onAction(action: EntriesAction) {
        when (action) {
            is EntriesAction.Order -> {
                // Check if order actually changed to avoid unnecessary fetches
                if (_uiState.value.dailyEntryOrder::class == action.dailyEntryOrder::class &&
                    _uiState.value.dailyEntryOrder.orderType == action.dailyEntryOrder.orderType
                ) {
                    return
                }
                getEntries(dailyEntryOrder = action.dailyEntryOrder)
            }

            is EntriesAction.DeleteEntry -> {
                viewModelScope.launch {
                    entryUseCase.deleteEntry(action.entry)
                    _uiState.update { it.copy(recentlyDeletedEntry = action.entry) }
                }
            }

            is EntriesAction.RestoreEntry -> {
                viewModelScope.launch {
                    val deletedEntry = _uiState.value.recentlyDeletedEntry ?: return@launch
                    entryUseCase.addDailyEntry(deletedEntry)
                    _uiState.update { it.copy(recentlyDeletedEntry = null) }
                }
            }

            is EntriesAction.ToggleOrderSection -> {
                _uiState.update {
                    it.copy(isOrderSectionVisible = !it.isOrderSectionVisible)
                }
            }
        }
    }

    private fun getEntries(dailyEntryOrder: DailyEntryOrder) {
        getEntriesJob?.cancel()
        getEntriesJob = entryUseCase.getEntries(dailyEntryOrder)
            .onEach { entries ->
                _uiState.update {
                    it.copy(
                        entries = entries,
                        dailyEntryOrder = dailyEntryOrder
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
