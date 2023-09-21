package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import com.jbrightman.thedayto.feature_thedayto.domain.model.TheDayToEntry
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryEvent
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodItem(
    viewModel: AddEditEntryViewModel,
) {
    var mMoodFieldSize by remember { mutableStateOf(Size.Zero) }
    var mExpanded by remember { mutableStateOf(false) }
    val moodState = viewModel.entryMood.value
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    OutlinedTextField(
        value = moodState.mood,
        onValueChange = { viewModel.onEvent(AddEditEntryEvent.EnteredMood(it)) },
        textStyle = MaterialTheme.typography.headlineSmall,
        colors = OutlinedTextFieldDefaults.colors(
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedBorderColor = MaterialTheme.colorScheme.background,
            unfocusedBorderColor = MaterialTheme.colorScheme.background,
            focusedLabelColor = MaterialTheme.colorScheme.background,
        ),
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .onGloballyPositioned { coordinates ->
                mMoodFieldSize = coordinates.size.toSize()
            },
        label = {
            Text(
                text = moodState.hint,
                color = MaterialTheme.colorScheme.primary
            )
        },
        trailingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = "mood dropdown button",
                modifier = Modifier.clickable { mExpanded = !mExpanded })
        }
    )
    DropdownMenu(
        expanded = mExpanded,
        onDismissRequest = {
            mExpanded = false
        },
        modifier = Modifier
            .width(with(LocalDensity.current) { mMoodFieldSize.width.toDp() })
    ) {
        TheDayToEntry.defaultMoods.forEach { mood ->
            DropdownMenuItem(
                onClick = {
                    moodState.mood = mood
                    viewModel.onEvent(AddEditEntryEvent.EnteredMood(moodState.mood))
                    mExpanded = false
                },
                text = { Text(text = mood) }
            )
        }
    }
}