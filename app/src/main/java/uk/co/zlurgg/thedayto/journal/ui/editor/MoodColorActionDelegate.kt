package uk.co.zlurgg.thedayto.journal.ui.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiState

/**
 * Delegate that handles all mood color-related actions for EditorViewModel.
 * Extracted to reduce function count in the ViewModel while keeping related logic grouped.
 *
 * All persistence goes through [EditorUseCases.saveMoodColor] / [EditorUseCases.setMoodColorFavorite],
 * so validation, name normalization, the 50-color limit, and the soft-delete restore behavior
 * are shared with the Mood Color Management screen.
 */
class MoodColorActionDelegate(
    private val editorUseCases: EditorUseCases,
    private val uiState: MutableStateFlow<EditorUiState>,
    private val uiEvents: MutableSharedFlow<EditorUiEvent>,
    private val scope: CoroutineScope,
    private val onSyncRequired: () -> Unit
) {
    fun handleSelectMoodColor(moodColorId: Int) {
        uiState.update {
            it.copy(
                selectedMoodColorId = moodColorId,
                isMoodHintVisible = false,
                moodError = null
            )
        }
    }

    fun handleToggleMoodColorSection() {
        uiState.update { it.copy(isMoodColorSectionVisible = !it.isMoodColorSectionVisible) }
    }

    fun handleSaveMoodColor(mood: String, colorHex: String) {
        scope.launch {
            Timber.d("Saving new mood color: %s", mood)
            val newMoodColor = MoodColor(
                mood = mood,
                color = colorHex,
                dateStamp = System.currentTimeMillis()
            )
            when (val result = editorUseCases.saveMoodColor(newMoodColor)) {
                is Result.Success -> {
                    val savedId = result.data.id
                    Timber.d("Successfully saved mood color: %s, auto-selecting ID: %s", mood, savedId)
                    onSyncRequired()
                    uiState.update {
                        it.copy(
                            isMoodColorSectionVisible = false,
                            selectedMoodColorId = savedId,
                            isMoodHintVisible = false,
                            moodError = null
                        )
                    }
                }
                is Result.Error -> {
                    Timber.w("Failed to save mood color: %s, error=%s", mood, result.error)
                    uiEvents.emit(EditorUiEvent.ShowMoodColorError(result.error))
                }
            }
        }
    }

    fun handleToggleFavorite(moodColor: MoodColor) {
        val id = moodColor.id ?: return
        val targetState = !moodColor.isFavorite
        Timber.d("Toggling favorite for %s: %s", moodColor.mood, targetState)

        scope.launch {
            when (val result = editorUseCases.setMoodColorFavorite(id, targetState)) {
                is Result.Success -> {
                    onSyncRequired()
                }
                is Result.Error -> {
                    Timber.w("Failed to toggle favorite for id=%d, error=%s", id, result.error)
                    uiEvents.emit(EditorUiEvent.ShowMoodColorError(result.error))
                }
            }
        }
    }

    fun handleEditMoodColor(moodColor: MoodColor) {
        Timber.d("Opening edit dialog for mood: %s", moodColor.mood)
        uiState.update {
            it.copy(showEditMoodColorDialog = true, editingMoodColor = moodColor)
        }
    }

    fun handleUpdateMoodColor(moodColorId: Int, newMood: String, newColorHex: String) {
        scope.launch {
            Timber.d("Updating mood color: id=%d, newMood=%s, newColor=%s", moodColorId, newMood, newColorHex)
            // Preserve favorite state, sync fields, etc. by copying from the original.
            // Fall back to a minimal record if the dialog state somehow lost the original.
            val original = uiState.value.editingMoodColor
            val updated = original?.copy(
                id = moodColorId,
                mood = newMood,
                color = newColorHex
            ) ?: MoodColor(
                id = moodColorId,
                mood = newMood,
                color = newColorHex,
                dateStamp = System.currentTimeMillis()
            )
            when (val result = editorUseCases.saveMoodColor(updated)) {
                is Result.Success -> {
                    onSyncRequired()
                    uiState.update {
                        it.copy(
                            showEditMoodColorDialog = false,
                            editingMoodColor = null,
                            editMoodColorError = null
                        )
                    }
                    Timber.i("Successfully updated mood color: %s", result.data.mood)
                    uiEvents.emit(EditorUiEvent.ShowSnackbar("\"${result.data.mood}\" updated"))
                }
                is Result.Error -> {
                    Timber.w("Failed to update mood color id=%d, error=%s", moodColorId, result.error)
                    uiState.update { it.copy(editMoodColorError = result.error) }
                }
            }
        }
    }

    fun handleCloseEditMoodColorDialog() {
        Timber.d("Closing edit mood color dialog")
        uiState.update {
            it.copy(
                showEditMoodColorDialog = false,
                editingMoodColor = null,
                editMoodColorError = null
            )
        }
    }

    fun handleClearEditMoodColorError() {
        uiState.update { it.copy(editMoodColorError = null) }
    }
}
