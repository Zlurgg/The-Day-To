package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.usecases.moodcolormanagement.MoodColorManagementUseCases
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementAction
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementUiState
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorEvent
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.revertOptimisticFavorite
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.withOptimisticFavorite
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler

/**
 * ViewModel for the Mood Color Management screen.
 * Handles loading mood colors with entry counts, and CRUD operations.
 */
class MoodColorManagementViewModel(
    private val useCases: MoodColorManagementUseCases,
    private val syncScheduler: SyncScheduler
) : ViewModel() {

    private val _state = MutableStateFlow(MoodColorManagementUiState())
    val state = _state.asStateFlow()

    private val _events = MutableSharedFlow<MoodColorEvent>()
    val events = _events.asSharedFlow()

    init {
        // Observe sorted mood colors - Room Flow auto-updates on any write
        useCases.getSortedMoodColors()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { sorted ->
                _state.update { it.copy(moodColors = sorted, isLoading = false) }
            }
            .catch { e ->
                Timber.e(e, "Failed to load mood colors")
                _state.update { it.copy(isLoading = false) }
            }
            .launchIn(viewModelScope)
    }

    fun onAction(action: MoodColorManagementAction) {
        when (action) {
            is MoodColorManagementAction.AddMoodColor -> showAddDialog()
            is MoodColorManagementAction.EditMoodColor -> showEditDialog(action.moodColor)
            is MoodColorManagementAction.SaveMoodColor -> save(action.moodColor)
            is MoodColorManagementAction.RequestDeleteMoodColor ->
                showDeleteConfirmation(action.moodColor)
            is MoodColorManagementAction.ConfirmDelete -> confirmDelete()
            is MoodColorManagementAction.DismissDeleteDialog -> dismissDeleteDialog()
            is MoodColorManagementAction.ToggleFavorite ->
                toggleFavorite(action.id, action.currentValue)
            is MoodColorManagementAction.DismissDialog -> dismissDialog()
            is MoodColorManagementAction.ClearError -> clearError()
        }
    }

    private fun showAddDialog() {
        _state.update { it.copy(editingMoodColor = MoodColor.empty()) }
    }

    private fun showEditDialog(moodColor: MoodColor) {
        _state.update { it.copy(editingMoodColor = moodColor) }
    }

    private fun dismissDialog() {
        _state.update { it.copy(editingMoodColor = null, dialogError = null) }
    }

    private fun clearError() {
        _state.update { it.copy(dialogError = null) }
    }

    private fun save(moodColor: MoodColor) {
        viewModelScope.launch {
            when (val result = useCases.saveMoodColor(moodColor)) {
                is Result.Success -> {
                    _state.update { it.copy(editingMoodColor = null, dialogError = null) }
                    syncScheduler.requestImmediateSync()
                }
                is Result.Error -> {
                    _state.update { it.copy(dialogError = result.error) }
                }
            }
        }
    }

    private fun showDeleteConfirmation(moodColor: MoodColor) {
        _state.update { it.copy(pendingDelete = moodColor) }
    }

    private fun confirmDelete() {
        val moodColor = _state.value.pendingDelete ?: return
        val id = moodColor.id ?: return
        viewModelScope.launch {
            when (val result = useCases.deleteMoodColor(id)) {
                is Result.Success -> {
                    _state.update { it.copy(pendingDelete = null) }
                    syncScheduler.requestImmediateSync()
                }
                is Result.Error -> {
                    _state.update { it.copy(pendingDelete = null) }
                    _events.emit(MoodColorEvent.ShowError(result.error))
                }
            }
        }
    }

    private fun dismissDeleteDialog() {
        _state.update { it.copy(pendingDelete = null) }
    }

    private fun toggleFavorite(id: Int, currentValue: Boolean) {
        val newValue = !currentValue

        // Store original value for potential rollback
        // Don't overwrite if already pending (preserves true original)
        _state.update { state ->
            state.copy(
                moodColors = state.moodColors.withOptimisticFavorite(id, newValue),
                pendingFavorites = if (id in state.pendingFavorites) {
                    state.pendingFavorites // Keep true original
                } else {
                    state.pendingFavorites + (id to currentValue)
                }
            )
        }

        viewModelScope.launch {
            when (val result = useCases.setFavorite(id, newValue)) {
                is Result.Success -> {
                    _state.update {
                        it.copy(pendingFavorites = it.pendingFavorites - id)
                    }
                    syncScheduler.requestImmediateSync()
                }
                is Result.Error -> {
                    // Revert using stored original value
                    val originalValue = _state.value.pendingFavorites[id] ?: currentValue
                    _state.update { state ->
                        state.copy(
                            moodColors = state.moodColors.revertOptimisticFavorite(id, originalValue),
                            pendingFavorites = state.pendingFavorites - id
                        )
                    }
                    _events.emit(MoodColorEvent.ShowError(result.error))
                }
            }
        }
    }
}
