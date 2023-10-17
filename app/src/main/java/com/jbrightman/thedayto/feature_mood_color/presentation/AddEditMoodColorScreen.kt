package com.jbrightman.thedayto.feature_mood_color.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryEvent
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryViewModel
import com.jbrightman.thedayto.feature_mood_color.presentation.component.ColorPicker
import com.jbrightman.thedayto.feature_mood_color.presentation.component.MoodCreator
import com.jbrightman.thedayto.ui.theme.paddingSmall
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun AddEditMoodColorScreen(
    mcViewModel: AddEditMoodColorViewModel = hiltViewModel(),
    viewModel: AddEditEntryViewModel = hiltViewModel()
    ) {
    Column(
        modifier = Modifier
    ) {
        MoodCreator()
        Spacer(modifier = Modifier.padding(paddingSmall))
        ColorPicker()
        mcViewModel.onEvent(
            AddEditMoodColorEvent.EnteredDate(
                LocalDate.now().atStartOfDay().toEpochSecond(
                    ZoneOffset.UTC
                )
            )
        )
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier.weight(0.2f)
            ) {
                IconButton(
                    onClick = {
                        mcViewModel.onEvent(AddEditMoodColorEvent.SaveMoodColor)
                        viewModel.onEvent(AddEditEntryEvent.ToggleMoodColorSection)
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Create new mood color"
                    )
                }
            }
            Spacer(modifier = Modifier.weight(0.6f))
            Column(
                modifier = Modifier.weight(0.2f)
            ) {
                IconButton(
                    onClick = {
                        viewModel.onEvent(AddEditEntryEvent.ToggleMoodColorSection)
                    }
                ) {
                    Icon(
                        modifier = Modifier.size(40.dp),
                        imageVector = Icons.Default.Cancel,
                        contentDescription = "Cancel mood color creation"
                    )
                }
            }
        }
    }
}
