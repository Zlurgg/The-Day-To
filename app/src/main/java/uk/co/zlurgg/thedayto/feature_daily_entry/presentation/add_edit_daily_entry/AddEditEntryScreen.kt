package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.presentation.Screen
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components.ContentItem
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components.DatePickerItem
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components.MoodItem
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.state.AddEditEntryAction
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.state.AddEditEntryUiEvent
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.state.AddEditEntryUiState
import uk.co.zlurgg.thedayto.ui.theme.paddingMedium

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun AddEditEntryScreenRoot(
    navController: NavController,
    showBackButton: Boolean,
    entryDate: Long,
    viewModel: AddEditEntryViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Handle one-time UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is AddEditEntryUiEvent.ShowSnackbar -> {
                    // Handle snackbar via callback
                }
                is AddEditEntryUiEvent.SaveEntry -> {
                    navController.navigate(Screen.EntriesScreen.route)
                }
            }
        }
    }

    // Delegate to presenter
    AddEditEntryScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = { navController.navigate(Screen.EntriesScreen.route) },
        showBackButton = showBackButton,
        entryDate = entryDate
    )
}

/**
 * Presenter composable - pure UI, no ViewModel dependency
 */
@Composable
private fun AddEditEntryScreen(
    uiState: AddEditEntryUiState,
    onAction: (AddEditEntryAction) -> Unit,
    onNavigateBack: () -> Unit,
    showBackButton: Boolean,
    entryDate: Long,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
            ) {
                if (showBackButton) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        modifier = Modifier
                            .padding(paddingMedium)
                            .clickable { onNavigateBack() }
                    )
                }
            }
        },
        floatingActionButton = {
            // Hide FAB when mood color picker is visible
            if (!uiState.isMoodColorSectionVisible) {
                FloatingActionButton(
                    onClick = { onAction(AddEditEntryAction.SaveEntry) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = stringResource(R.string.save_entry)
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(paddingMedium)
        ) {
            // Date Picker
            DatePickerItem(
                selectedDate = uiState.entryDate,
                onDateSelected = { date -> onAction(AddEditEntryAction.EnteredDate(date)) }
            )
            Spacer(modifier = Modifier.height(paddingMedium))

            // Mood Selection
            MoodItem(
                selectedMood = uiState.entryMood,
                moodColors = uiState.moodColors,
                hint = if (uiState.entryDate == java.time.LocalDate.now().atStartOfDay()
                        .toEpochSecond(java.time.ZoneOffset.UTC)
                ) uiState.todayHint else uiState.previousDayHint,
                showMoodColorDialog = uiState.isMoodColorSectionVisible,
                onMoodSelected = { mood, colorHex ->
                    onAction(AddEditEntryAction.EnteredMood(mood))
                    onAction(AddEditEntryAction.EnteredColor(colorHex))
                },
                onDeleteMoodColor = { moodColor ->
                    onAction(AddEditEntryAction.DeleteMoodColor(moodColor))
                },
                onToggleMoodColorDialog = {
                    onAction(AddEditEntryAction.ToggleMoodColorSection)
                },
                onSaveMoodColor = { mood, colorHex ->
                    onAction(AddEditEntryAction.SaveMoodColor(mood, colorHex))
                }
            )
            Spacer(modifier = Modifier.height(paddingMedium))

            // Content
            ContentItem(
                content = uiState.entryContent,
                hint = uiState.contentHint,
                isHintVisible = uiState.isContentHintVisible,
                onContentChange = { content ->
                    onAction(AddEditEntryAction.EnteredContent(content))
                },
                onFocusChange = { focusState ->
                    onAction(AddEditEntryAction.ChangeContentFocus(focusState))
                }
            )
        }
    }
}
