package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation
import androidx.core.graphics.toColorInt

/**
 * Material3 dialog for editing an existing mood color.
 * Both the mood name and color can be changed.
 *
 * @param moodColor The mood color to edit
 * @param showDialog Controls dialog visibility
 * @param onDismiss Callback when dialog is dismissed without saving
 * @param onSave Callback when user saves with new mood name and color hex
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun EditMoodColorDialog(
    moodColor: MoodColor,
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (newMood: String, newColorHex: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        // Local state - starts with current values
        var editedMood by remember(moodColor) { mutableStateOf(moodColor.mood) }
        var moodError by remember { mutableStateOf<String?>(null) }
        var selectedColor by remember(moodColor) { mutableStateOf(moodColor.color) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(R.string.edit_mood_color),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mood name field - EDITABLE
                    OutlinedTextField(
                        value = editedMood,
                        onValueChange = { newValue ->
                            if (newValue.length <= InputValidation.MAX_MOOD_LENGTH) {
                                editedMood = newValue
                                moodError = null
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.mood),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = true,
                        enabled = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineSmall,
                        isError = moodError != null,
                        supportingText = {
                            when {
                                moodError != null -> Text(
                                    text = moodError ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                                else -> Text(
                                    text = "${editedMood.length}/${InputValidation.MAX_MOOD_LENGTH}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(paddingMedium))

                    // Color picker with current color pre-selected
                    val colorPickerController = rememberColorPickerController()

                    // Set initial color on the picker
                    val initialColor = remember(moodColor) {
                        try {
                            Color("#${moodColor.color}".toColorInt())
                        } catch (_: Exception) {
                            Color.Black
                        }
                    }

                    colorPickerController.selectByColor(initialColor, fromUser = false)

                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(paddingSmall),
                        controller = colorPickerController,
                        initialColor = initialColor,
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            selectedColor = colorEnvelope.hexCode
                        }
                    )
                }
            },
            confirmButton = {
                IconButton(
                    onClick = {
                        val trimmedMood = editedMood.trim()
                        if (trimmedMood.isBlank()) {
                            moodError = "Mood name cannot be empty"
                        } else {
                            onSave(trimmedMood, selectedColor)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.save)
                    )
                }
            },
            dismissButton = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.cancel)
                    )
                }
            },
            modifier = modifier
        )
    }
}
