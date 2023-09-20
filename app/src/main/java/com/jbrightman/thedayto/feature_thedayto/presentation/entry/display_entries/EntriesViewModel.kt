package com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry.EntryUseCases
import com.jbrightman.thedayto.feature_thedayto.domain.util.EntryOrder
import com.jbrightman.thedayto.feature_thedayto.domain.util.OrderType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntriesViewModel @Inject constructor(
    private val entryUseCase: EntryUseCases
) : ViewModel() {

    private val _state = mutableStateOf(EntriesState())
    val state: State<EntriesState> = _state

    private var recentlyDeletedEntry: TheDayToEntry? = null

    private var getEntriesJob: Job? = null

    init {
        getEntries(EntryOrder.Date(OrderType.Descending))
    }

    fun onEvent(event: EntriesEvent) {
        when(event) {
            is EntriesEvent.Order -> {
                if (state.value.entryOrder::class == event.entryOrder::class &&
                    state.value.entryOrder.orderType == event.entryOrder.orderType
                ) {
                    return
                }
                getEntries(entryOrder = event.entryOrder)
            }
            is EntriesEvent.DeleteEntry -> {
                viewModelScope.launch {
                    entryUseCase.deleteEntry(event.entry)
                    recentlyDeletedEntry = event.entry
                }
            }
            is EntriesEvent.RestoreEntry -> {
                viewModelScope.launch {
                    entryUseCase.addEntry(recentlyDeletedEntry ?: return@launch)
                    recentlyDeletedEntry = null
                }
            }
            is EntriesEvent.ToggleOrderSection -> {
                _state.value = state.value.copy(
                    isOrderSectionVisible = !state.value.isOrderSectionVisible
                )
            }
        }
    }

    private fun getEntries(entryOrder: EntryOrder) {
        getEntriesJob?.cancel()
        getEntriesJob = entryUseCase.getEntries(entryOrder)
            .onEach { entries ->
                _state.value = state.value.copy(
                    entries = entries,
                    entryOrder = entryOrder
                )
            }
            .launchIn(viewModelScope)
    }
}