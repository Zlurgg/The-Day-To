package com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SwipeToDismiss
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryEvent
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryViewModel
import com.jbrightman.thedayto.feature_daily_entry.presentation.display_daily_entries.EntriesEvent
import com.jbrightman.thedayto.feature_mood_color.domain.model.MoodColor.Companion.defaultMoods
import com.jbrightman.thedayto.feature_mood_color.presentation.AddEditMoodColorEvent
import com.jbrightman.thedayto.feature_mood_color.presentation.AddEditMoodColorScreen
import com.jbrightman.thedayto.feature_mood_color.presentation.AddEditMoodColorViewModel
import com.jbrightman.thedayto.presentation.util.getColorFromMood
import com.jbrightman.thedayto.ui.theme.paddingMedium
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun MoodItem(
    viewModel: AddEditEntryViewModel = hiltViewModel(),
    mcViewModel: AddEditMoodColorViewModel = hiltViewModel()
) {
    var mMoodFieldSize by remember { mutableStateOf(Size.Zero) }
    var mExpanded by remember { mutableStateOf(false) }
    val moodState = viewModel.entryMood.value

    val hint = if (viewModel.entryDate.value.date == LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC))
        moodState.todayHint
    else
        moodState.previousDayHint

    val moodColorState = viewModel.state.value

    ExposedDropdownMenuBox(
        expanded = mExpanded,
        onExpandedChange = {
            mExpanded = !mExpanded
        }
    ) {
        OutlinedTextField(
            value = moodState.mood,
            onValueChange = { },
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
                .menuAnchor()
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
            defaultMoods.forEach { mood ->
                val color = getColorFromMood(mood)
                DropdownMenuItem(
                    onClick = {
                        moodState.mood = mood
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
                                text = mood
                            )
                            Box(
                                modifier = Modifier
                                    .background(color)
                                    .height(height = 12.dp)
                                    .weight(0.4f)
                            )
                            IconButton(
                                modifier = Modifier.weight(0.2f),
                                onClick = {

                            }) {
                                Icon(imageVector = Icons.Default.Delete,
                                    contentDescription = "delete default mood colors")
                            }
                        }
                    }
                )
            }
            mcViewModel.state.value.moodColors.forEach { moodColors ->
                val color  = getColor(moodColors.color)
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
                                mcViewModel.onEvent(AddEditMoodColorEvent.DeleteMoodColor(moodColors))
                            }) {
                            Icon(imageVector = Icons.Default.Delete,
                                contentDescription = "delete custom mood color")
                        }
                    }
                }
            )}
            // Button to add a new mood color
            Button(
                onClick = {
                    viewModel.onEvent(AddEditEntryEvent.ToggleMoodColorSection)
                    mExpanded = false
                },
            ) {
                Text(
                    text = "Add new Mood Color"
                )
            }
        }
    }
    AnimatedVisibility(
        visible = moodColorState.isMoodColorSectionVisible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        AddEditMoodColorScreen()
        Spacer(modifier = Modifier.height(paddingMedium))
    }
}

private fun getColor(colorString: String): Color {
    return Color(android.graphics.Color.parseColor("#$colorString"))
}
