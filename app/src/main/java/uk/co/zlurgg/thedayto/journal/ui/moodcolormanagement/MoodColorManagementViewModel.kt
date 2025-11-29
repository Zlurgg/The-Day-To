package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement.MoodColorManagementUseCases
import uk.co.zlurgg.thedayto.journal.domain.util.MoodColorOrder
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementAction
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementUiEvent
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementUiState
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorWithCount
import java.time.Instant

/**
 * ViewModel for the Mood Color Management screen.
 * Handles loading mood colors with entry counts, and CRUD operations.
 */
class MoodColorManagementViewModel(
    private val moodColorManagementUseCases: MoodColorManagementUseCases
) : ViewModel() {

    // Single source of truth for UI state
    private val _uiState = MutableStateFlow(MoodColorManagementUiState())
    val uiState = _uiState.asStateFlow()

    // One-time UI events
    private val _uiEvents = MutableSharedFlow<MoodColorManagementUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    private var getMoodColorsJob: Job? = null

    init {
        loadMoodColors(_uiState.value.sortOrder)
    }

    fun onAction(action: MoodColorManagementAction) {
        when (action) {
            is MoodColorManagementAction.ToggleSortOrder -> {
                if (_uiState.value.sortOrder::class == action.order::class &&
                    _uiState.value.sortOrder.orderType == action.order.orderType
                ) {
                    return
                }
                loadMoodColors(action.order)
            }

            is MoodColorManagementAction.DeleteMoodColor -> {
                viewModelScope.launch {
                    try {
                        val moodColorId = action.moodColor.id
                        if (moodColorId == null) {
                            Timber.w("Cannot delete mood color with null ID: ${action.moodColor.mood}")
                            _uiEvents.emit(
                                MoodColorManagementUiEvent.ShowSnackbar(
                                    message = "Cannot delete: mood color not saved"
                                )
                            )
                            return@launch
                        }
                        moodColorManagementUseCases.deleteMoodColor(moodColorId)
                        _uiState.update { it.copy(recentlyDeletedMoodColor = action.moodColor) }
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = "\"${action.moodColor.mood}\" deleted",
                                actionLabel = "Undo"
                            )
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to delete mood color")
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = "Failed to delete: ${e.message}"
                            )
                        )
                    }
                }
            }

            is MoodColorManagementAction.RestoreMoodColor -> {
                viewModelScope.launch {
                    val deletedMoodColor = _uiState.value.recentlyDeletedMoodColor ?: return@launch
                    try {
                        // Re-add the mood color (AddMoodColorUseCase handles restoration)
                        moodColorManagementUseCases.addMoodColor(deletedMoodColor)
                        _uiState.update { it.copy(recentlyDeletedMoodColor = null) }
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = "\"${deletedMoodColor.mood}\" restored"
                            )
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to restore mood color")
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = "Failed to restore: ${e.message}"
                            )
                        )
                    }
                }
            }

            is MoodColorManagementAction.ClearRecentlyDeleted -> {
                _uiState.update { it.copy(recentlyDeletedMoodColor = null) }
            }

            is MoodColorManagementAction.ShowAddMoodColorDialog -> {
                _uiState.update { it.copy(showAddMoodColorDialog = true) }
            }

            is MoodColorManagementAction.DismissAddMoodColorDialog -> {
                _uiState.update { it.copy(showAddMoodColorDialog = false) }
            }

            is MoodColorManagementAction.SaveNewMoodColor -> {
                viewModelScope.launch {
                    try {
                        val newMoodColor = MoodColor(
                            mood = action.mood,
                            color = action.colorHex,
                            dateStamp = Instant.now().epochSecond
                        )
                        moodColorManagementUseCases.addMoodColor(newMoodColor)
                        _uiState.update { it.copy(showAddMoodColorDialog = false) }
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = "\"${action.mood}\" created"
                            )
                        )
                    } catch (e: InvalidMoodColorException) {
                        Timber.w(e, "Invalid mood color")
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = e.message ?: "Invalid mood color"
                            )
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to create mood color")
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = "Failed to create: ${e.message}"
                            )
                        )
                    }
                }
            }

            is MoodColorManagementAction.ShowEditMoodColorDialog -> {
                _uiState.update { it.copy(editingMoodColor = action.moodColor) }
            }

            is MoodColorManagementAction.DismissEditMoodColorDialog -> {
                _uiState.update { it.copy(editingMoodColor = null) }
            }

            is MoodColorManagementAction.SaveEditedMoodColor -> {
                viewModelScope.launch {
                    try {
                        val originalMood = _uiState.value.editingMoodColor

                        // Update name if changed
                        if (originalMood != null && originalMood.mood != action.newMood) {
                            Timber.d("Updating mood name: ${originalMood.mood} -> ${action.newMood}")
                            moodColorManagementUseCases.updateMoodColorName(
                                action.moodColorId,
                                action.newMood
                            )
                        }

                        // Update color
                        moodColorManagementUseCases.updateMoodColor(action.moodColorId, action.newColorHex)

                        _uiState.update { it.copy(editingMoodColor = null) }
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = "\"${action.newMood}\" updated"
                            )
                        )
                        Timber.i("Successfully updated mood color: ${action.newMood}")
                    } catch (e: InvalidMoodColorException) {
                        Timber.w(e, "Invalid mood color update")
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = e.message ?: "Invalid mood"
                            )
                        )
                    } catch (e: Exception) {
                        Timber.e(e, "Failed to update mood color")
                        _uiEvents.emit(
                            MoodColorManagementUiEvent.ShowSnackbar(
                                message = "Failed to update: ${e.message}"
                            )
                        )
                    }
                }
            }

            is MoodColorManagementAction.RetryLoadMoodColors -> {
                _uiState.update { it.copy(loadError = null) }
                loadMoodColors(_uiState.value.sortOrder)
            }

            is MoodColorManagementAction.DismissLoadError -> {
                _uiState.update { it.copy(loadError = null) }
            }
        }
    }

    /**
     * Load mood colors combined with entry counts.
     * Uses combine to merge mood colors flow with entry counts flow.
     */
    private fun loadMoodColors(sortOrder: MoodColorOrder) {
        getMoodColorsJob?.cancel()
        _uiState.update { it.copy(isLoading = true, sortOrder = sortOrder) }

        getMoodColorsJob = combine(
            moodColorManagementUseCases.getMoodColors(sortOrder),
            moodColorManagementUseCases.getMoodColorEntryCounts()
        ) { moodColors, entryCounts ->
            moodColors.map { moodColor ->
                MoodColorWithCount(
                    moodColor = moodColor,
                    entryCount = entryCounts[moodColor.id] ?: 0
                )
            }
        }
            .onEach { moodColorsWithCount ->
                _uiState.update {
                    it.copy(
                        moodColorsWithCount = moodColorsWithCount,
                        isLoading = false,
                        loadError = null
                    )
                }
            }
            .catch { exception ->
                Timber.e(exception, "Failed to load mood colors")
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = exception.message ?: "Failed to load mood colors"
                    )
                }
            }
            .launchIn(viewModelScope)
    }
}
