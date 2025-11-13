package uk.co.zlurgg.thedayto.journal.ui.editor

import android.content.res.Configuration
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.navigation.OverviewRoute
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToFormattedDate
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.ui.editor.components.ContentItem
import uk.co.zlurgg.thedayto.journal.ui.editor.components.MoodItem
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorAction
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiState
import java.time.LocalDate
import java.time.ZoneOffset

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
    var showEditorTutorialDialog by remember { mutableStateOf(false) }

    // Handle one-time UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collectLatest { event ->
            when (event) {
                is EditorUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is EditorUiEvent.SaveEntry -> {
                    navController.navigate(OverviewRoute) {
                        // Remove editor from back stack
                        popUpTo(OverviewRoute) { inclusive = false }
                    }
                }
                is EditorUiEvent.ShowEditorTutorial -> {
                    showEditorTutorialDialog = true
                }
            }
        }
    }

    // Delegate to presenter
    EditorScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = {
            navController.navigate(OverviewRoute) {
                // Remove editor from back stack
                popUpTo(OverviewRoute) { inclusive = false }
            }
        },
        showBackButton = showBackButton,
        snackbarHostState = snackbarHostState
    )

    // Show editor tutorial dialog for first-time users
    if (showEditorTutorialDialog) {
        uk.co.zlurgg.thedayto.journal.ui.editor.components.EditorTutorialDialog(
            onDismiss = { showEditorTutorialDialog = false }
        )
    }
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
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = modifier
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(paddingMedium)
        ) {
            // Date display (read-only)
            Text(
                text = datestampToFormattedDate(uiState.entryDate),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(paddingMedium))

            // Mood Selection
            MoodItem(
                selectedMoodColorId = uiState.selectedMoodColorId,
                moodColors = uiState.moodColors,
                hint = uiState.moodHint,
                showMoodColorDialog = uiState.isMoodColorSectionVisible,
                onMoodSelected = { moodColorId ->
                    onAction(EditorAction.SelectMoodColor(moodColorId))
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

@Preview(name = "New Entry - Light", showBackground = true)
@Preview(name = "New Entry - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditorScreenNewEntryPreview() {
    TheDayToTheme {
        EditorScreen(
            uiState = EditorUiState(
                moodColors = listOf(
                    MoodColor("Cheerful", "FFF59D", false, System.currentTimeMillis(), 1),
                    MoodColor("Calm", "4A148C", false, System.currentTimeMillis(), 2),
                    MoodColor("Motivated", "4CAF50", false, System.currentTimeMillis(), 3)
                )
            ),
            onAction = {},
            onNavigateBack = {},
            showBackButton = false,
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Edit Entry - Light", showBackground = true)
@Preview(name = "Edit Entry - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditorScreenEditEntryPreview() {
    TheDayToTheme {
        EditorScreen(
            uiState = EditorUiState(
                entryDate = LocalDate.now().minusDays(2).atStartOfDay().toEpochSecond(ZoneOffset.UTC),
                selectedMoodColorId = 1,
                entryContent = "Had a great day at work! Finished the new feature and got positive feedback from the team.",
                isMoodHintVisible = false,
                isContentHintVisible = false,
                currentEntryId = 1,
                moodColors = listOf(
                    MoodColor("Happy", "4CAF50", false, System.currentTimeMillis(), 1),
                    MoodColor("Peaceful", "2196F3", false, System.currentTimeMillis(), 2),
                    MoodColor("Motivated", "FF9800", false, System.currentTimeMillis(), 3)
                )
            ),
            onAction = {},
            onNavigateBack = {},
            showBackButton = true,
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Loading State - Light", showBackground = true)
@Preview(name = "Loading State - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun EditorScreenLoadingPreview() {
    TheDayToTheme {
        EditorScreen(
            uiState = EditorUiState(
                selectedMoodColorId = 1,
                entryContent = "Sample content",
                isLoading = true,
                moodColors = listOf(
                    MoodColor("Happy", "4CAF50", false, System.currentTimeMillis(), 1)
                )
            ),
            onAction = {},
            onNavigateBack = {},
            showBackButton = true,
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
