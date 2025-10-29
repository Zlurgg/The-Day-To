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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.ColorEnvelope
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.util.InputValidation

/**
 * Material3 dialog for creating custom mood-color combinations.
 * Uses callback pattern instead of ViewModel injection for better separation of concerns.
 *
 * @param showDialog Controls dialog visibility
 * @param onDismiss Callback when dialog is dismissed without saving
 * @param onSave Callback when user saves with (mood, colorHex)
 * @param modifier Optional modifier for the dialog
 */
@Composable
fun MoodColorPickerDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onSave: (mood: String, colorHex: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (showDialog) {
        // Local state - resets each time dialog opens
        var mood by remember { mutableStateOf("") }
        var selectedColor by remember { mutableStateOf("#000000") }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = stringResource(R.string.create_new_mood_color),
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Mood text field
                    OutlinedTextField(
                        value = mood,
                        onValueChange = { newValue ->
                            // Enforce max length at UI level
                            if (newValue.length <= InputValidation.MAX_MOOD_LENGTH) {
                                mood = newValue
                                // Clear error when user starts typing
                                errorMessage = null
                            }
                        },
                        label = {
                            Text(
                                text = stringResource(R.string.mood),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineSmall,
                        isError = errorMessage != null,
                        supportingText = {
                            if (errorMessage != null) {
                                Text(
                                    text = errorMessage!!,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            } else {
                                Text(
                                    text = "${mood.length}/${InputValidation.MAX_MOOD_LENGTH}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(paddingMedium))

                    // Color picker
                    val colorPickerController = rememberColorPickerController()
                    HsvColorPicker(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .padding(paddingSmall),
                        controller = colorPickerController,
                        onColorChanged = { colorEnvelope: ColorEnvelope ->
                            selectedColor = colorEnvelope.hexCode
                        }
                    )
                }
            },
            confirmButton = {
                IconButton(
                    onClick = {
                        // Validate mood before saving
                        if (mood.trim().isEmpty()) {
                            errorMessage = "Mood cannot be empty!"
                        } else {
                            onSave(mood, selectedColor)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.save_entry)
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
