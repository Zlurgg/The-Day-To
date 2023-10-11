package com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color

import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color.component.ColorPicker
import com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color.component.MoodCreator
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryEvent
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryViewModel
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Screen
import com.jbrightman.thedayto.ui.theme.paddingMedium
import com.jbrightman.thedayto.ui.theme.paddingSmall
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun AddEditMoodColorScreen(
    navController: NavController,
    viewModel: AddEditMoodColorViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditMoodColorViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                is AddEditMoodColorViewModel.UiEvent.SaveMoodColor -> {
                    navController.navigate(Screen.EntriesScreen.route)
                }
            }
        }
    }
    Scaffold(
        topBar = {
            Row {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    modifier = Modifier
                        .padding(paddingMedium)
                        .clickable {
                            navController.navigate(Screen.EntriesScreen.route)
                        }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(AddEditMoodColorEvent.SaveMoodColor)
                },
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Save, contentDescription = "Save entry")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(paddingMedium)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MoodCreator(viewModel = viewModel)
                    Spacer(modifier = Modifier.padding(paddingSmall))
                    ColorPicker(viewModel = viewModel)
                    viewModel.onEvent(
                        AddEditMoodColorEvent.EnteredDate(
                            LocalDate.now().atStartOfDay().toEpochSecond(
                                ZoneOffset.UTC
                            )
                        )
                    )
                }
            }
        }
    )

}