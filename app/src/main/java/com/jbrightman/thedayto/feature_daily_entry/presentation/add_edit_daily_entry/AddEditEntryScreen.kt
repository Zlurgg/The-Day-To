package com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components.ContentItem
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components.DatePickerItem
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components.MoodItem
import com.jbrightman.thedayto.feature_mood_color.presentation.AddEditMoodColorScreen
import com.jbrightman.thedayto.presentation.util.Screen
import com.jbrightman.thedayto.presentation.util.getColorFromMood
import com.jbrightman.thedayto.ui.theme.paddingMedium
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddEditEntryScreen(
    navController: NavController,
    showBackButton: Boolean,
    entryDate: Long,
    viewModel: AddEditEntryViewModel = hiltViewModel(),
) {
    val moodState = viewModel.entryMood.value

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

    var moodColor = getColorFromMood(moodState.mood) ?: Color.White
//    val entryBackgroundAnimatatable = remember {
//        Animatable(
//            getColorFromMood(moodState.mood) ?: Color.White
//        )
//    }
//    var moodColor = Color(viewModel.entryColor.value.toColorInt())
//    val entryBackgroundAnimatatable = remember {
//        Animatable(
//            moodColor
//        )
//    }

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
/*        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    viewModel.onEvent(AddEditEntryEvent.SaveEntry)
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save entry")
            }
        },*/
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
//                    .background(moodColor)
                    .padding(padding)
                    .padding(paddingMedium)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier.padding(paddingMedium)
                    ) {

                        // Date Picker
                        DatePickerItem(viewModel = viewModel, entryDate = entryDate)
                        Spacer(modifier = Modifier.height(paddingMedium))
                        // Mood
                        MoodItem(viewModel = viewModel)
                        Spacer(modifier = Modifier.height(paddingMedium))
                        // Content
                        ContentItem(viewModel = viewModel)
                        Button(
                            modifier = Modifier
                                .align(Alignment.End)
                                .background(MaterialTheme.colorScheme.primary),
                            onClick = {
                                viewModel.onEvent(AddEditEntryEvent.SaveEntry)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Save entry"
                            )
                        }
                    }
                }
                // Change to be part of top bar with animated visibility
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Mood + Color Creation
                    AddEditMoodColorScreen(navController = navController)
                }
            }
        }
    )
}