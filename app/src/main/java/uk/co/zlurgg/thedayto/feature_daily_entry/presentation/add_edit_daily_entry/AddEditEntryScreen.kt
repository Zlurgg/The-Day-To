package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components.ContentItem
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components.DatePickerItem
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components.MoodItem
import uk.co.zlurgg.thedayto.presentation.util.Screen
import uk.co.zlurgg.thedayto.ui.theme.paddingMedium
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddEditEntryScreen(
    navController: NavController,
    showBackButton: Boolean,
    entryDate: Long,
    viewModel: AddEditEntryViewModel = hiltViewModel(),
) {
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
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                if (showBackButton) {
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
                }
            }
        },
        floatingActionButton = {
            /** if we are putting a new mood color in hide button **/
            if (!viewModel.state.value.isMoodColorSectionVisible) {
                FloatingActionButton(
                    onClick = {
                        viewModel.onEvent(AddEditEntryEvent.SaveEntry)
                    },
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = "Save entry")
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { padding ->
            AddEditEntryScreenDisplay(
                padding,
                entryDate
            )
        }
    )
}

@Composable
fun AddEditEntryScreenDisplay(padding: PaddingValues, entryDate: Long) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(paddingMedium)
    ) {
        // Date Picker
        DatePickerItem(entryDate = entryDate)
        Spacer(modifier = Modifier.height(paddingMedium))
        // Mood
        MoodItem()
        Spacer(modifier = Modifier.height(paddingMedium))
        // Content
        ContentItem()
    }
}
/*
@Preview(showBackground = true)
@Composable
fun AddEditEntryScreenDisplayPreview() {
    */
/** create some temp variables to preview screen **//*

    val  contentPadding = PaddingValues(top = 8.dp, bottom = 8.dp)
    val date = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
    AddEditEntryScreenDisplay(contentPadding, date)
}*/
