package uk.co.zlurgg.thedayto.journal.ui.editor

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.Screen
import uk.co.zlurgg.thedayto.journal.ui.editor.components.ContentItem
import uk.co.zlurgg.thedayto.journal.ui.editor.components.DatePickerItem
import uk.co.zlurgg.thedayto.journal.ui.editor.components.MoodItem
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorAction
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiState
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun EditorScreenRoot(
    navController: NavController,
    showBackButton: Boolean,
    viewModel: EditorViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-time UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is EditorUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EditorUiEvent.SaveEntry -> {
                    navController.navigate(Screen.OverviewScreen.route)
                }
            }
        }
    }

    // Delegate to presenter
    EditorScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = { navController.navigate(Screen.OverviewScreen.route) },
        showBackButton = showBackButton,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Presenter composable - pure UI, no ViewModel dependency
 */
@Composable
private fun EditorScreen(
    uiState: EditorUiState,
    onAction: (EditorAction) -> Unit,
    onNavigateBack: () -> Unit,
    showBackButton: Boolean,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {

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
                    onClick = { onAction(EditorAction.SaveEntry) },
                    containerColor = if (uiState.isLoading)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onSecondary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = stringResource(R.string.save_entry)
                        )
                    }
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
                onDateSelected = { date -> onAction(EditorAction.EnteredDate(date)) }
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
                    onAction(EditorAction.EnteredMood(mood))
                    onAction(EditorAction.EnteredColor(colorHex))
                },
                onDeleteMoodColor = { moodColor ->
                    onAction(EditorAction.DeleteMoodColor(moodColor))
                },
                onToggleMoodColorDialog = {
                    onAction(EditorAction.ToggleMoodColorSection)
                },
                onSaveMoodColor = { mood, colorHex ->
                    onAction(EditorAction.SaveMoodColor(mood, colorHex))
                }
            )
            Spacer(modifier = Modifier.height(paddingMedium))

            // Content
            ContentItem(
                content = uiState.entryContent,
                hint = uiState.contentHint,
                isHintVisible = uiState.isContentHintVisible,
                onContentChange = { content ->
                    onAction(EditorAction.EnteredContent(content))
                },
                onFocusChange = { focusState ->
                    onAction(EditorAction.ChangeContentFocus(focusState))
                }
            )
        }
    }
}
