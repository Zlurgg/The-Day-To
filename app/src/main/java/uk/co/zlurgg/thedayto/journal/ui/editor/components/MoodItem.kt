package uk.co.zlurgg.thedayto.journal.ui.editor.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.ui.util.getColor

/**
 * Pure presenter component for mood selection with color picker.
 * No ViewModel dependency - receives all state and callbacks as parameters.
 *
 * @param selectedMoodColorId The currently selected mood color ID (null if none selected)
 * @param moodColors List of available mood-color combinations
 * @param hint Hint text to display
 * @param showMoodColorDialog Whether to show the mood color picker dialog
 * @param onMoodSelected Callback when a mood is selected (moodColorId)
 * @param onDeleteMoodColor Callback to delete a mood color
 * @param onEditMoodColor Callback to edit a mood color
 * @param onToggleMoodColorDialog Callback to toggle the mood color picker dialog
 * @param onSaveMoodColor Callback to save a new mood color (mood, colorHex)
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun MoodItem(
    selectedMoodColorId: Int?,
    moodColors: List<MoodColor>,
    hint: String,
    showMoodColorDialog: Boolean,
    onMoodSelected: (moodColorId: Int) -> Unit,
    onDeleteMoodColor: (MoodColor) -> Unit,
    onEditMoodColor: (MoodColor) -> Unit,
    onToggleMoodColorDialog: () -> Unit,
    onSaveMoodColor: (mood: String, colorHex: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var mMoodFieldSize by remember { mutableStateOf(Size.Zero) }
    var mExpanded by remember { mutableStateOf(false) }

    // Find the selected mood color by ID
    val selectedMoodColor = moodColors.find { it.id == selectedMoodColorId }
    val displayText = selectedMoodColor?.mood ?: ""

    ExposedDropdownMenuBox(
        expanded = mExpanded,
        onExpandedChange = {
            mExpanded = !mExpanded
        },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = displayText,
            onValueChange = { /* Read-only, changes via dropdown */ },
            textStyle = MaterialTheme.typography.headlineSmall,
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.primary,
                // Use primary color border when mood is selected, outline when not
                focusedBorderColor = if (selectedMoodColorId != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                unfocusedBorderColor = if (selectedMoodColorId != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                focusedLabelColor = MaterialTheme.colorScheme.primary,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
            ),
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                .onGloballyPositioned { coordinates ->
                    mMoodFieldSize = coordinates.size.toSize()
                },
            label = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = hint,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "*",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            },
            leadingIcon = {
                // Show color indicator when a mood is selected
                selectedMoodColor?.let { mood ->
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(getColor(mood.color), CircleShape)
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                    )
                }
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
                .heightIn(max = 300.dp) // Allow scroll for large mood lists
        ) {
            // Show empty state message when no moods exist
            if (moodColors.isEmpty()) {
                DropdownMenuItem(
                    onClick = { /* No action - just informational */ },
                    enabled = false,
                    text = {
                        Text(
                            text = stringResource(R.string.empty_mood_list_message),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }

            // Existing mood colors
            moodColors.forEach { moodColor ->
                val color = getColor(moodColor.color)
                DropdownMenuItem(
                    onClick = {
                        moodColor.id?.let { onMoodSelected(it) }
                        mExpanded = false
                    },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Color indicator - prominent circle with border
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Mood text - takes remaining space
                            Text(
                                text = moodColor.mood,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            // Edit button
                            IconButton(
                                onClick = {
                                    onEditMoodColor(moodColor)
                                    mExpanded = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_mood_color_button),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

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

            // Button to add a new mood color - ALWAYS shown
            // More prominent when list is empty (highlighted background)
            DropdownMenuItem(
                onClick = {
                    onToggleMoodColorDialog()
                    mExpanded = false
                },
                text = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (moodColors.isEmpty()) {
                                    Modifier.background(
                                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                        shape = MaterialTheme.shapes.small
                                    )
                                } else {
                                    Modifier
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(R.string.add_custom_mood_color),
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
            // Note: Auto-selection is now handled by ViewModel after save completes
        }
    )
}

/**
 * Preview showing the empty dropdown menu content directly
 * This preview shows what users see when they open the dropdown with no moods
 */
@Preview(name = "Empty Dropdown Content - Light", showBackground = true)
@Preview(name = "Empty Dropdown Content - Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun EmptyDropdownContentPreview() {
    TheDayToTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Empty state message
            DropdownMenuItem(
                onClick = { },
                enabled = false,
                text = {
                    Text(
                        text = stringResource(R.string.empty_mood_list_message),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            )

            // Highlighted + Add button (when empty)
            DropdownMenuItem(
                onClick = { },
                text = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                Modifier.background(
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                    shape = MaterialTheme.shapes.small
                                )
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(R.string.add_custom_mood_color),
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
}

/**
 * Preview showing the dropdown menu with existing moods
 * Shows normal + Add button without highlight
 */
@Preview(name = "Dropdown with Moods - Light", showBackground = true)
@Preview(name = "Dropdown with Moods - Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DropdownWithMoodsPreview() {
    TheDayToTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            val moodColors = listOf(
                MoodColor(id = 1, mood = "Happy", color = "FFD700", dateStamp = System.currentTimeMillis()),
                MoodColor(id = 2, mood = "Calm", color = "87CEEB", dateStamp = System.currentTimeMillis()),
                MoodColor(id = 3, mood = "Energetic", color = "FF6347", dateStamp = System.currentTimeMillis())
            )

            // Existing mood colors
            moodColors.forEach { moodColor ->
                val color = getColor(moodColor.color)
                DropdownMenuItem(
                    onClick = { },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Color indicator - prominent circle with border
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(color, CircleShape)
                                    .border(
                                        width = 1.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = moodColor.mood,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = { }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = stringResource(R.string.edit_mood_color_button),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(onClick = { }) {
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

            // Normal + Add button (no highlight when moods exist)
            DropdownMenuItem(
                onClick = { },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = stringResource(R.string.add_custom_mood_color),
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
}
