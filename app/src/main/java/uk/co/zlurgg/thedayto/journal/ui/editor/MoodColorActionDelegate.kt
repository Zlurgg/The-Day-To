package uk.co.zlurgg.thedayto.journal.ui.editor

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import uk.co.zlurgg.thedayto.journal.domain.model.InvalidMoodColorException
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.usecases.editor.EditorUseCases
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiState
import java.time.Instant

/**
 * Delegate that handles all mood color-related actions for EditorViewModel.
 * Extracted to reduce function count in the ViewModel while keeping related logic grouped.
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
            try {
                Timber.d("Saving new mood color: %s", mood)
                val newMoodColorId = editorUseCases.addMoodColorUseCase(
                    MoodColor(
                        mood = mood,
                        color = colorHex,
                        dateStamp = Instant.now().epochSecond,
                        id = null
                    )
                )
                Timber.d("Successfully saved mood color: $mood, auto-selecting ID: $newMoodColorId")
                onSyncRequired()
                uiState.update {
                    it.copy(
                        isMoodColorSectionVisible = false,
                        selectedMoodColorId = newMoodColorId,
                        isMoodHintVisible = false,
                        moodError = null
                    )
                }
            } catch (e: InvalidMoodColorException) {
                Timber.e(e, "Failed to save mood color: $mood")
                uiEvents.emit(
                    EditorUiEvent.ShowSnackbar(message = e.message ?: "Couldn't save mood color!")
                )
            }
        }
    }

    fun handleDeleteMoodColor(moodColor: MoodColor) {
        scope.launch {
            Timber.d("Deleting mood color: ${moodColor.mood}")
            editorUseCases.deleteMoodColor(moodColor.id ?: return@launch)
            onSyncRequired()

            val currentState = uiState.value
            if (currentState.selectedMoodColorId == moodColor.id) {
                val remainingMoodColors = currentState.moodColors.filter { it.id != moodColor.id }

                if (remainingMoodColors.isNotEmpty()) {
                    val defaultMoodColor = remainingMoodColors.first()
                    Timber.d("Selected mood was deleted, switching to: ${defaultMoodColor.mood}")
                    uiState.update {
                        it.copy(selectedMoodColorId = defaultMoodColor.id, isMoodHintVisible = false)
                    }
                } else {
                    Timber.w("No mood colors remaining after deletion")
                    uiState.update {
                        it.copy(selectedMoodColorId = null, isMoodHintVisible = true)
                    }
                }
            }
        }
    }

    fun handleEditMoodColor(moodColor: MoodColor) {
        Timber.d("Opening edit dialog for mood: ${moodColor.mood}")
        uiState.update {
            it.copy(showEditMoodColorDialog = true, editingMoodColor = moodColor)
        }
    }

    fun handleUpdateMoodColor(moodColorId: Int, newMood: String, newColorHex: String) {
        scope.launch {
            try {
                val originalMood = uiState.value.editingMoodColor
                Timber.d("Updating mood color: id=$moodColorId, newMood=$newMood, newColor=$newColorHex")

                if (originalMood != null && originalMood.mood != newMood) {
                    Timber.d("Updating mood name: ${originalMood.mood} -> $newMood")
                    editorUseCases.updateMoodColorNameUseCase(id = moodColorId, newMood = newMood)
                }

                editorUseCases.updateMoodColorUseCase(id = moodColorId, newColor = newColorHex)
                onSyncRequired()

                uiState.update {
                    it.copy(
                        showEditMoodColorDialog = false,
                        editingMoodColor = null,
                        editMoodColorError = null
                    )
                }

                Timber.i("Successfully updated mood color: $newMood")
                uiEvents.emit(EditorUiEvent.ShowSnackbar("\"$newMood\" updated"))
            } catch (e: InvalidMoodColorException) {
                Timber.w(e, "Invalid mood color update")
                uiState.update { it.copy(editMoodColorError = e.message ?: "Invalid mood") }
            } catch (e: Exception) {
                Timber.e(e, "Failed to update mood color")
                uiEvents.emit(
                    EditorUiEvent.ShowSnackbar(message = "Failed to update: ${e.message}")
                )
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
