package uk.co.zlurgg.thedayto.journal.ui.editor.components

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import timber.log.Timber
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingLarge
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.AddMoodColorDialog
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.ColorWheelButton
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorRow
import uk.co.zlurgg.thedayto.journal.ui.util.getColorSafe

/**
 * Pure presenter component for mood selection with color picker.
 * No ViewModel dependency - receives all state and callbacks as parameters.
 *
 * Dropdown shows mood colors with:
 * - Star toggle for favorites (sorted to top)
 * - Color circle with edit icon
 * - No delete (use Management screen for that)
 *
 * @param selectedMoodColorId The currently selected mood color ID (null if none selected)
 * @param moodColors List of available mood-color combinations (sorted by favorites)
 * @param showMoodColorDialog Whether to show the mood color picker dialog
 * @param onMoodSelected Callback when a mood is selected (moodColorId)
 * @param onToggleFavorite Callback to toggle mood color favorite status
 * @param onEditMoodColor Callback to edit a mood color
 * @param onToggleMoodColorDialog Callback to toggle the mood color picker dialog
 * @param onSaveMoodColor Callback to save a new mood color (mood, colorHex)
 * @param modifier Optional modifier
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodItem(
    selectedMoodColorId: Int?,
    moodColors: List<MoodColor>,
    showMoodColorDialog: Boolean,
    onMoodSelected: (moodColorId: Int) -> Unit,
    onToggleFavorite: (MoodColor) -> Unit,
    onEditMoodColor: (MoodColor) -> Unit,
    onToggleMoodColorDialog: () -> Unit,
    onSaveMoodColor: (mood: String, colorHex: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var mMoodFieldSize by remember { mutableStateOf(Size.Zero) }
    var mExpanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // Find the selected mood color by ID
    val selectedMoodColor = moodColors.find { it.id == selectedMoodColorId }
    val displayText = selectedMoodColor?.mood ?: ""

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(paddingSmall),
    ) {
        ExposedDropdownMenuBox(
            expanded = mExpanded,
            onExpandedChange = {
                mExpanded = !mExpanded
            },
            modifier = Modifier.weight(1f),
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
                    Text(
                        text = stringResource(R.string.mood),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                },
                leadingIcon = {
                    // Show color indicator when a mood is selected
                    selectedMoodColor?.let { mood ->
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(getColorSafe(mood.color), CircleShape)
                                .border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = CircleShape,
                                ),
                        )
                    }
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = mExpanded)
                },
            )
            ExposedDropdownMenu(
                expanded = mExpanded,
                onDismissRequest = {
                    mExpanded = false
                },
                modifier = Modifier
                    .width(with(LocalDensity.current) { mMoodFieldSize.width.toDp() })
                    .heightIn(max = 300.dp), // Allow scroll for large mood lists
            ) {
                // Show empty state callout when no moods exist.
                // Using a plain Column (not a disabled DropdownMenuItem) because the
                // empty state isn't meant to be interactive — tapping it would just
                // collapse the menu with no action.
                if (moodColors.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = paddingMedium,
                                vertical = paddingLarge,
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.empty_mood_list_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Spacer(modifier = Modifier.height(paddingSmall))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(paddingSmall),
                        ) {
                            Text(
                                text = stringResource(R.string.empty_mood_list_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp),
                            )
                        }
                    }
                }

                // Mood colors with unified MoodColorRow.
                // Zero out DropdownMenuItem's default 12dp horizontal padding so
                // MoodColorRow's own start/end padding is the sole source. This keeps
                // the star and edit circle visually close to the dropdown edges and
                // matches the spacing used on the Management screen.
                moodColors.forEach { moodColor ->
                    DropdownMenuItem(
                        contentPadding = PaddingValues(horizontal = 0.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            moodColor.id?.let { onMoodSelected(it) }
                                ?: Timber.w("MoodColor has null ID: %s", moodColor.mood)
                            mExpanded = false
                        },
                        text = {
                            MoodColorRow(
                                moodColor = moodColor,
                                onToggleFavorite = { onToggleFavorite(moodColor) },
                                onEdit = {
                                    onEditMoodColor(moodColor)
                                    mExpanded = false
                                },
                            )
                        },
                    )
                }
            }
        }

        // Color wheel button - always visible for easy access to create new mood colors
        ColorWheelButton(
            onClick = onToggleMoodColorDialog,
        )
    }

    // Mood color picker dialog
    AddMoodColorDialog(
        showDialog = showMoodColorDialog,
        onDismiss = onToggleMoodColorDialog,
        onSave = { mood, colorHex ->
            onSaveMoodColor(mood, colorHex)
            // Note: Auto-selection is now handled by ViewModel after save completes
        },
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
                .padding(
                    horizontal = paddingMedium,
                    vertical = paddingLarge,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.empty_mood_list_title),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.height(paddingSmall))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(paddingSmall),
            ) {
                Text(
                    text = stringResource(R.string.empty_mood_list_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp),
                )
            }
        }
    }
}

/**
 * Preview showing the dropdown menu with existing moods using unified MoodColorRow
 */
@Preview(name = "Dropdown with Moods - Light", showBackground = true)
@Preview(name = "Dropdown with Moods - Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun DropdownWithMoodsPreview() {
    TheDayToTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface),
        ) {
            val moodColors = listOf(
                MoodColor(
                    id = 1,
                    mood = "Happy",
                    color = "FFD700",
                    isFavorite = true,
                    dateStamp = System.currentTimeMillis(),
                ),
                MoodColor(
                    id = 2,
                    mood = "Calm",
                    color = "87CEEB",
                    isFavorite = false,
                    dateStamp = System.currentTimeMillis(),
                ),
                MoodColor(
                    id = 3,
                    mood = "Energetic",
                    color = "FF6347",
                    isFavorite = false,
                    dateStamp = System.currentTimeMillis(),
                ),
            )

            // Mood colors with unified MoodColorRow
            moodColors.forEach { moodColor ->
                DropdownMenuItem(
                    onClick = { },
                    text = {
                        MoodColorRow(
                            moodColor = moodColor,
                            onToggleFavorite = { },
                            onEdit = { },
                        )
                    },
                )
            }
        }
    }
}
