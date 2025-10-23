package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.presentation.util.getColor
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryEvent
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryViewModel
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.AddEditMoodColorEvent
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.AddEditMoodColorViewModel
import uk.co.zlurgg.thedayto.ui.theme.paddingMedium
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun MoodItem(
    viewModel: AddEditEntryViewModel = koinViewModel(),
    mcViewModel: AddEditMoodColorViewModel = koinViewModel()
) {
    var mMoodFieldSize by remember { mutableStateOf(Size.Zero) }
    var mExpanded by remember { mutableStateOf(false) }
    val moodState = viewModel.entryMood.value
    val mcMoodState = mcViewModel.state.value

    val hint = if (viewModel.entryDate.value.date == LocalDate.now().atStartOfDay()
            .toEpochSecond(ZoneOffset.UTC)
    ) {
        moodState.todayHint
    } else {
        moodState.previousDayHint
    }

    val moodColorState = viewModel.state.value

//    var color by remember { mutableStateOf(Color.White) }

    ExposedDropdownMenuBox(
        expanded = mExpanded,
        onExpandedChange = {
            mExpanded = !mExpanded
        }
    ) {
        OutlinedTextField(
            value = moodState.mood,
            onValueChange = { viewModel.onEvent(AddEditEntryEvent.EnteredMood(it)) },
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
//                .background(moodState.color) would require some reworking
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
            mcMoodState.moodColors.forEach { moodColors ->
                val color = getColor(moodColors.color)
                DropdownMenuItem(
                    onClick = {
                        moodState.mood = moodColors.mood
                        viewModel.onEvent(AddEditEntryEvent.EnteredMood(moodState.mood))
                        viewModel.onEvent(
                            AddEditEntryEvent.EnteredColor(
                                color.toArgb().toHexString()
                            )
                        )
                        mExpanded = false
                    },
                    text = {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                modifier = Modifier.weight(0.4f),
                                text = moodColors.mood
                            )
                            Box(
                                modifier = Modifier
                                    .background(color)
                                    .size(12.dp)
                                    .weight(0.4f)
                            )
                            IconButton(
                                modifier = Modifier.weight(0.2f),
                                onClick = {
                                    mcViewModel.onEvent(
                                        AddEditMoodColorEvent.DeleteMoodColor(
                                            moodColors
                                        )
                                    )
                                }) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = stringResource(R.string.delete_custom_mood_color)
                                )
                            }
                        }
                    }
                )
            }
            // Button to add a new mood color
            IconButton(
                modifier = Modifier.fillMaxSize(),
                onClick = {
                    viewModel.onEvent(AddEditEntryEvent.ToggleMoodColorSection)
                    mExpanded = false
                },
            ) {
                Icon(
                    imageVector = Icons.Default.AddCircle,
                    contentDescription = stringResource(R.string.add_custom_mood_color)
                )
            }
        }
    }
    // Mood color picker dialog
    MoodColorPickerDialog(
        showDialog = moodColorState.isMoodColorSectionVisible,
        onDismiss = {
            viewModel.onEvent(AddEditEntryEvent.ToggleMoodColorSection)
        },
        onSave = { mood, colorHex ->
            viewModel.onEvent(AddEditEntryEvent.SaveMoodColor(mood, colorHex))
        }
    )
}
