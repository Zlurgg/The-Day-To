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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color.component.ColorPicker
import com.jbrightman.thedayto.feature_thedayto.presentation.add_edit_mood_color.component.MoodCreator
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Screen
import com.jbrightman.thedayto.ui.theme.paddingMedium
import com.jbrightman.thedayto.ui.theme.paddingSmall

@Composable
fun AddEditMoodColorScreen(
    navController: NavController,
//    viewModel: AddEditMoodColorViewModel = hiltViewModel()
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var entryBackgroundAnimatatable = remember {
        Animatable(
            Color.White
        )
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
//                    viewModel.onEvent(AddEditMoodColorViewModel.SaveEntry)
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
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ColorPicker()
                    Spacer(modifier = Modifier.padding(paddingSmall))
                    MoodCreator()
                }
            }
        }
    )

}