package com.example.thedayto.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thedayto.data.entry.EntryRepo
import com.example.thedayto.data.entry.Entry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class EntryViewModel(private val entryRepo: EntryRepo): ViewModel() {

    /**
     * Holds current entry ui state
     */
    var entriesUiState by mutableStateOf(EntryUiState())
        private set

    /**
     * Updates the [entriesUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(entryDetails: EntryDetails) {
        entriesUiState =
            EntryUiState(entryDetails = entryDetails, isEntryValid = validateInput(entryDetails))
    }

    /**
     * Inserts an [Entry] in the Room database
     */
    suspend fun saveEntry() {
        if (validateInput()) {
            entryRepo.insertEntry(entriesUiState.entryDetails.toEntry())
        }
    }

    private fun validateInput(uiState: EntryDetails = entriesUiState.entryDetails): Boolean {
        return with(uiState) {
            date?.isNotBlank() ?: true && mood?.isNotBlank() ?: true
        }
    }

    val uiState: StateFlow<EntryUiState> =
        entryRepo.getEntryStream(id = 1)
            .filterNotNull()
            .map {
                EntryUiState(isEntryValid = true, entryDetails = it.toEntryDetails())
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = EntryUiState()
            )
    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * Extension function to convert [EntryUiState] to [Entry]
 */
data class EntryUiState(
    val entryDetails: EntryDetails = EntryDetails(),
    val isEntryValid: Boolean = false
)

data class EntryDetails(
    var id: Int = 0,
    var date: String? = "",
    var mood: String? = "",
    var note: String? = ""
)
/**
 * Extension function to convert [EntryDetails] to [Entry]
 *
 * maybe not needed as not converting their format?
 */
fun EntryDetails.toEntry(): Entry = Entry(
    id = id,
    date = date,
    mood = mood,
    note = note
)

/**
 * Extension function to convert [Entry] to [EntryDetails]
 */
fun Entry.toEntryDetails(): EntryDetails = EntryDetails(
    id = id,
    date = date,
    mood = mood,
    note = note
)