package com.jbrightman.thedayto.feature_mood_color.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        Button(
            onClick = {
                mcViewModel.onEvent(AddEditMoodColorEvent.SaveMoodColor)
                viewModel.onEvent(AddEditEntryEvent.ToggleMoodColorSection)
            },
            modifier = Modifier
                .background(MaterialTheme.colorScheme.primary)
                .align(Alignment.End)
        ) {
            Icon(imageVector = Icons.Default.Save, contentDescription = "Save entry")
        }
    }
}
