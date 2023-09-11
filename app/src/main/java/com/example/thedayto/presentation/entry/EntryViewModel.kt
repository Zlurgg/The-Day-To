package com.example.thedayto.presentation.entry

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.thedayto.domain.repository.TheDayToRepository
import com.example.thedayto.data.local.TheDayToEntity
import kotlinx.coroutines.launch

private const val TAG = "EntryViewModel"

class EntryViewModel(private val theDayToRepository: TheDayToRepository): ViewModel() {

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

    val allEntries: LiveData<List<TheDayToEntity>> = theDayToRepository.allEntries.asLiveData()

    /**
     * Inserts an [Entry] in the Room database
     */
    fun insert(theDayToEntity: TheDayToEntity) = viewModelScope.launch {
        theDayToRepository.insert(theDayToEntity)
    }
    suspend fun saveEntry() {
        if (validateInput()) {
            theDayToRepository.insert(entriesUiState.entryDetails.toEntry())
        } else {
            Log.i(TAG, "Input not valid: " + entriesUiState.entryDetails);
        }
    }

    private fun validateInput(uiState: EntryDetails = entriesUiState.entryDetails): Boolean {
        return with(uiState) {
            date.isNotBlank() && mood.isNotBlank()
        }
    }

    fun entryFromDate(date: String) = viewModelScope.launch {
        theDayToRepository.getEntryFromDate(date)
    }
}

class EntryViewModelFactory(private val repository: TheDayToRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EntryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EntryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
