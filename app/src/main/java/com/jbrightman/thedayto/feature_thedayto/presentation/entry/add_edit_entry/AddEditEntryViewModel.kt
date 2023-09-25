package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jbrightman.thedayto.feature_thedayto.domain.model.InvalidTheDayToEntryException
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
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
        EntryDateState(
            date = 0L
        )
    )
    val entryDate: State<EntryDateState> = _entryDate

    private val _entryMood = mutableStateOf(
        EntryMoodState(
            hint = "How're you feeling today?"
        )
    )
    val entryMood: State<EntryMoodState> = _entryMood

    private val _entryContent = mutableStateOf(
        EntryTextFieldState(
            hint = "Anything else to add?"
        )
    )
    val entryContent: State<EntryTextFieldState> = _entryContent

    private val _entryColor = mutableIntStateOf(TheDayToEntry.entryColors.random().toArgb())
    val entryColor: State<Int> = _entryColor

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
                            _entryColor.value = entry.color
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
            is AddEditEntryEvent.ChangeColor -> {
                _entryColor.intValue = event.color
            }
            is AddEditEntryEvent.SaveEntry -> {
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        println("AddEditEntryEvent Save: ${entryDate.value.date}")

                        entryUseCases.addEntry(
                            TheDayToEntry(
                                content = entryContent.value.text,
                                color = entryColor.value,
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
        object SaveEntry : UiEvent()
    }
}