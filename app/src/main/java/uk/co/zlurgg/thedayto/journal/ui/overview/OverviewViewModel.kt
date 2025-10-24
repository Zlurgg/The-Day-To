package uk.co.zlurgg.thedayto.journal.ui.overview

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.zlurgg.thedayto.journal.domain.util.EntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.EntryUseCases
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewAction
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiState

class OverviewViewModel(
    private val entryUseCase: EntryUseCases
) : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(OverviewUiState())
    val uiState = _uiState.asStateFlow()

    private var getEntriesJob: Job? = null

    init {
        getEntries(EntryOrder.Date(OrderType.Descending))
    }

    fun onAction(action: OverviewAction) {
        when (action) {
            is OverviewAction.Order -> {
                // Check if order actually changed to avoid unnecessary fetches
                if (_uiState.value.entryOrder::class == action.entryOrder::class &&
                    _uiState.value.entryOrder.orderType == action.entryOrder.orderType
                ) {
                    return
                }
                getEntries(entryOrder = action.entryOrder)
            }

            is OverviewAction.DeleteEntry -> {
                viewModelScope.launch {
                    entryUseCase.deleteEntry(action.entry)
                    _uiState.update { it.copy(recentlyDeletedEntry = action.entry) }
                }
            }

            is OverviewAction.RestoreEntry -> {
                viewModelScope.launch {
                    val deletedEntry = _uiState.value.recentlyDeletedEntry ?: return@launch
                    entryUseCase.addEntryUseCase(deletedEntry)
                    _uiState.update { it.copy(recentlyDeletedEntry = null) }
                }
            }

            is OverviewAction.ToggleOrderSection -> {
                _uiState.update {
                    it.copy(isOrderSectionVisible = !it.isOrderSectionVisible)
                }
            }
        }
    }

    private fun getEntries(entryOrder: EntryOrder) {
        getEntriesJob?.cancel()
        getEntriesJob = entryUseCase.getEntries(entryOrder)
            .onEach { entries ->
                _uiState.update {
                    it.copy(
                        entries = entries,
                        entryOrder = entryOrder
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
