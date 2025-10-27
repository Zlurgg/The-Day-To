package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.util.getColor

/**
 * Pure presenter component for mood selection with color picker.
 * No ViewModel dependency - receives all state and callbacks as parameters.
 *
 * @param selectedMood The currently selected mood text
 * @param moodColors List of available mood-color combinations
 * @param hint Hint text to display
 * @param showMoodColorDialog Whether to show the mood color picker dialog
 * @param onMoodSelected Callback when a mood is selected (mood, colorHex)
 * @param onDeleteMoodColor Callback to delete a mood color
 * @param onToggleMoodColorDialog Callback to toggle the mood color picker dialog
 * @param onSaveMoodColor Callback to save a new mood color (mood, colorHex)
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun MoodItem(
    selectedMood: String,
    moodColors: List<MoodColor>,
    hint: String,
    showMoodColorDialog: Boolean,
    onMoodSelected: (mood: String, colorHex: String) -> Unit,
    onDeleteMoodColor: (MoodColor) -> Unit,
    onToggleMoodColorDialog: () -> Unit,
    onSaveMoodColor: (mood: String, colorHex: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mMoodFieldSize by remember { mutableStateOf(Size.Zero) }
    var mExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = mExpanded,
        onExpandedChange = {
            mExpanded = !mExpanded
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedMood,
            onValueChange = { /* Read-only, changes via dropdown */ },
            textStyle = MaterialTheme.typography.headlineSmall,
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.surface,
                unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                focusedLabelColor = MaterialTheme.colorScheme.surface,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
            ),
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable, true)
                .onGloballyPositioned { coordinates ->
                    mMoodFieldSize = coordinates.size.toSize()
                },
            label = {
                Text(
                    text = hint,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = mExpanded)
            }
        )
        ExposedDropdownMenu(
            expanded = mExpanded,
            onDismissRequest = {
                mExpanded = false
            },
            modifier = Modifier
                .width(with(LocalDensity.current) { mMoodFieldSize.width.toDp() })
        ) {
            moodColors.forEach { moodColor ->
                val color = getColor(moodColor.color)
                DropdownMenuItem(
                    onClick = {
                        onMoodSelected(moodColor.mood, moodColor.color)
                        mExpanded = false
                    },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Color indicator - prominent circle
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .background(color, CircleShape)
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Mood text - takes remaining space
                            Text(
                                text = moodColor.mood,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Delete button
                            IconButton(
                                onClick = {
                                    onDeleteMoodColor(moodColor)
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = stringResource(R.string.delete_custom_mood_color),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
            }
            // Button to add a new mood color
            DropdownMenuItem(
                onClick = {
                    onToggleMoodColorDialog()
                    mExpanded = false
                },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.add_custom_mood_color),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    }

    // Mood color picker dialog
    MoodColorPickerDialog(
        showDialog = showMoodColorDialog,
        onDismiss = onToggleMoodColorDialog,
        onSave = { mood, colorHex ->
            onSaveMoodColor(mood, colorHex)
            // Auto-select the newly created mood color
            onMoodSelected(mood, colorHex)
        }
    )
}
