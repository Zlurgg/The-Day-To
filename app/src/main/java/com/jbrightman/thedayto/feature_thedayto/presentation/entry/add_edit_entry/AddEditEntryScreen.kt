package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry

import androidx.compose.animation.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.components.ColorSelector
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.components.ContentItem
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.components.DatePickerItem
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.components.MoodItem
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Screen
import com.jbrightman.thedayto.ui.theme.paddingMedium
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddEditEntryScreen(
    navController: NavController,
    entryColor: Int,
    showBackButton: Boolean,
    entryDate: Long,
    viewModel: AddEditEntryViewModel = hiltViewModel(),
) {
    val entryBackgroundAnimatatable = remember {
        Animatable(
            Color(if (entryColor != -1) entryColor else viewModel.entryColor.value)
        )
    }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AddEditEntryViewModel.UiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message
                    )
                }
                is AddEditEntryViewModel.UiEvent.SaveEntry -> {
                    navController.navigate(Screen.EntriesScreen.route)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            if (showBackButton) {
                Row{
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
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(AddEditEntryEvent.SaveEntry)
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save entry")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(entryBackgroundAnimatatable.value)
                    .padding(padding)
                    .padding(paddingMedium)
            ) {
                // Entry Color Selection
                ColorSelector(
                    entryBackgroundAnimatatable =  entryBackgroundAnimatatable,
                    viewModel = viewModel
                )
                Spacer(modifier = Modifier.height(paddingMedium))
                // Date Picker
                DatePickerItem(viewModel = viewModel, entryDate = entryDate)
                Spacer(modifier = Modifier.height(paddingMedium))
                // Mood
                MoodItem(viewModel = viewModel)
                Spacer(modifier = Modifier.height(paddingMedium))
                // Content
                ContentItem(viewModel = viewModel)
            }
        }
    )
}