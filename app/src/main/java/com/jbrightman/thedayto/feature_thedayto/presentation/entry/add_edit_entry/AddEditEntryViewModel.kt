package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.InvalidTheDayToEntryException
import com.jbrightman.thedayto.feature_thedayto.domain.model.entry.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.domain.use_case.entry.EntryUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class AddEditEntryViewModel @Inject constructor(
    private val entryUseCases: EntryUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _entryDate = mutableStateOf(
        EntryDateFieldState(
            date = 0L
        )
    )
    val entryDate: State<EntryDateFieldState> = _entryDate

    private val _entryMood = mutableStateOf(
        EntryMoodState(
            todayHint = "How're you feeling today?",
            previousDayHint = "How're were you feeling that day?"
        )
    )
    val entryMood: State<EntryMoodState> = _entryMood

    private val _entryContent = mutableStateOf(
        EntryContentFieldState(
            hint = "Any additional info?"
        )
    )
    val entryContent: State<EntryContentFieldState> = _entryContent

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentEntryId: Int? = null

    init {
        savedStateHandle.get<Int>("entryId")?.let { entryId ->
            if (entryId != -1) {
                viewModelScope.launch(Dispatchers.IO) {
                    entryUseCases.getEntry(entryId)?.also { entry ->
                        withContext(Dispatchers.Main) {
                            currentEntryId = entry.id
                            _entryDate.value = entryDate.value.copy(
                                    date = entry.dateStamp,
                                )
                            }
                            _entryMood.value = entryMood.value.copy(
                                mood = entry.mood,
                                isHintVisible = false
                            )
                            _entryContent.value = entryContent.value.copy(
                                text = entry.content,
                                isHintVisible = false
                            )
                        }
                    }
                }
            }
        }

    fun onEvent(event: AddEditEntryEvent) {
        when (event) {
            is AddEditEntryEvent.EnteredDate -> {
                _entryDate.value = entryDate.value.copy(
                    date = event.date
                )
            }
            is AddEditEntryEvent.EnteredMood -> {
                _entryMood.value = entryMood.value.copy(
                    mood = event.mood
                )
            }
            is AddEditEntryEvent.ChangeMoodFocus -> {
                _entryMood.value = entryMood.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            entryMood.value.mood.isBlank()
                )
            }
            is AddEditEntryEvent.EnteredContent -> {
                _entryContent.value = _entryContent.value.copy(
                    text = event.value
                )
            }
            is AddEditEntryEvent.ChangeContentFocus -> {
                _entryContent.value = _entryContent.value.copy(
                    isHintVisible = !event.focusState.isFocused &&
                            _entryContent.value.text.isBlank()
                )
            }
            is AddEditEntryEvent.SaveEntry -> {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        entryUseCases.addEntry(
                            TheDayToEntry(
                                content = entryContent.value.text,
                                dateStamp = entryDate.value.date,
                                mood = entryMood.value.mood,
                                id = currentEntryId
                            )
                        )
                        _eventFlow.emit(UiEvent.SaveEntry)
                    } catch (e: InvalidTheDayToEntryException) {
                        _eventFlow.emit(
                            UiEvent.ShowSnackbar(
                                message = e.message ?: "Couldn't save entry"
                            )
                        )
                    }
                }
            }
        }
    }

    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
        data object SaveEntry : UiEvent()
    }
}