package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import uk.co.zlurgg.thedayto.core.domain.util.DailyEntryOrder
import uk.co.zlurgg.thedayto.core.domain.util.OrderType
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.model.DailyEntry
import uk.co.zlurgg.thedayto.feature_daily_entry.domain.use_case.DailyEntryUseCases

class EntriesViewModel(
    private val entryUseCase: DailyEntryUseCases
) : ViewModel() {

    private val _state = mutableStateOf(EntriesState())
    val state: State<EntriesState> = _state

    private var recentlyDeletedEntry: DailyEntry? = null

    private var getEntriesJob: Job? = null

    /*    private val _entriesYear = mutableStateOf(
            EntriesYearFieldState(
                year = LocalDate.now().year
            )
        )
        private val entriesYear: State<EntriesYearFieldState> = _entriesYear*/

    init {
        getEntries(DailyEntryOrder.Date(OrderType.Descending))
    }

    fun onEvent(event: EntriesEvent) {
        when (event) {
            is EntriesEvent.Order -> {
                if (state.value.dailyEntryOrder::class == event.dailyEntryOrder::class &&
                    state.value.dailyEntryOrder.orderType == event.dailyEntryOrder.orderType
                ) {
                    return
                }
                getEntries(dailyEntryOrder = event.dailyEntryOrder)
            }

            is EntriesEvent.DeleteEntry -> {
                viewModelScope.launch {
                    entryUseCase.deleteEntry(event.entry)
                    recentlyDeletedEntry = event.entry
                }
            }

            is EntriesEvent.RestoreEntry -> {
                viewModelScope.launch {
                    entryUseCase.addDailyEntry(recentlyDeletedEntry ?: return@launch)
                    recentlyDeletedEntry = null
                }
            }

            is EntriesEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }
//            is EntriesEvent.ChangeYear -> {
//                _entriesYear.value = entriesYear.value.copy(
//                    year = event.year
//                )
//            }
        }
    }

    private fun getEntries(dailyEntryOrder: DailyEntryOrder) {
        getEntriesJob?.cancel()
        getEntriesJob = entryUseCase.getEntries(dailyEntryOrder)
            .onEach { entries ->
                _state.value = state.value.copy(
                    entries = entries,
                    dailyEntryOrder = dailyEntryOrder
                )
            }
            .launchIn(viewModelScope)
    }
}