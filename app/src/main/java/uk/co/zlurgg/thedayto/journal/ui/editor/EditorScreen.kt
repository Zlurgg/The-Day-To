package uk.co.zlurgg.thedayto.journal.ui.editor

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.ui.editor.components.ContentItem
import uk.co.zlurgg.thedayto.journal.ui.editor.components.EditMoodColorDialog
import uk.co.zlurgg.thedayto.journal.ui.editor.components.EditorDatePickerDialog
import uk.co.zlurgg.thedayto.journal.ui.editor.components.MoodItem
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorAction
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiEvent
import uk.co.zlurgg.thedayto.journal.ui.editor.state.EditorUiState
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToFormattedDate
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
                is EditorUiEvent.NavigateBack -> {
                    navController.navigate(OverviewRoute) {
                        // Remove editor from back stack
                        popUpTo(OverviewRoute) { inclusive = false }
                    }
                }
            }
        }
    }

    // Delegate to presenter
    EditorScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        showBackButton = showBackButton,
        snackbarHostState = snackbarHostState
    )

    // Show editor tutorial dialog for first-time users
    if (uiState.showEditorTutorial) {
        uk.co.zlurgg.thedayto.journal.ui.editor.components.EditorTutorialDialog(
            onDismiss = { viewModel.onAction(EditorAction.DismissEditorTutorial) }
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
                    IconButton(
                        onClick = { onAction(EditorAction.RequestNavigateBack) },
                        modifier = Modifier.padding(paddingMedium)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            // Hide FAB when mood color picker is visible
            if (!uiState.isMoodColorSectionVisible) {
                FloatingActionButton(
                    onClick = { onAction(EditorAction.SaveEntry) },
                    containerColor = when {
                        uiState.isLoading -> MaterialTheme.colorScheme.secondary
                        !uiState.canSave -> MaterialTheme.colorScheme.surfaceVariant
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
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
                            contentDescription = stringResource(R.string.save_entry),
                            tint = if (uiState.canSave)
                                MaterialTheme.colorScheme.onPrimaryContainer
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
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
                .imePadding()
        ) {
            // Error banner (persistent for load failures)
            if (uiState.loadError != null) {
                LoadErrorBanner(
                    errorMessage = uiState.loadError,
                    onRetry = { onAction(EditorAction.RetryLoadEntry) },
                    onDismiss = { onAction(EditorAction.DismissLoadError) }
                )
                Spacer(modifier = Modifier.height(paddingMedium))
            }

            // Date display (clickable to open date picker)
            Row(
                modifier = Modifier.clickable {
                    onAction(EditorAction.ToggleDatePicker)
                },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = datestampToFormattedDate(uiState.entryDate),
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = stringResource(R.string.pick_a_date),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
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
                onEditMoodColor = { moodColor ->
                    onAction(EditorAction.EditMoodColor(moodColor))
                },
                onToggleMoodColorDialog = {
                    onAction(EditorAction.ToggleMoodColorSection)
                },
                onSaveMoodColor = { mood, colorHex ->
                    onAction(EditorAction.SaveMoodColor(mood, colorHex))
                }
            )

            // Show validation error if present
            uiState.moodError?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                )
            }

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
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Edit mood color dialog
    uiState.editingMoodColor?.let { editingMood ->
        if (uiState.showEditMoodColorDialog) {
            EditMoodColorDialog(
                moodColor = editingMood,
                showDialog = true,
                onDismiss = {
                    onAction(EditorAction.CloseEditMoodColorDialog)
                },
                onSave = { newColorHex ->
                    editingMood.id?.let { id ->
                        onAction(EditorAction.UpdateMoodColor(id, newColorHex))
                    }
                }
            )
        }
    }

    // Date picker dialog
    if (uiState.showDatePicker) {
        EditorDatePickerDialog(
            currentDate = java.time.Instant.ofEpochSecond(uiState.entryDate)
                .atZone(ZoneOffset.UTC)
                .toLocalDate(),
            onDismiss = {
                onAction(EditorAction.ToggleDatePicker)
            },
            onDateSelected = { selectedDate ->
                val epochSeconds = selectedDate.atStartOfDay(ZoneOffset.UTC).toEpochSecond()
                onAction(EditorAction.EnteredDate(epochSeconds))
            }
        )
    }

    // Show unsaved changes dialog
    if (uiState.showUnsavedChangesDialog) {
        UnsavedChangesDialog(
            onDiscard = { onAction(EditorAction.ConfirmDiscardChanges) },
            onKeepEditing = { onAction(EditorAction.DismissUnsavedChangesDialog) }
        )
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
            showBackButton = true,
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

/**
 * Unsaved Changes Dialog
 *
 * Displays a confirmation dialog when the user attempts to navigate back with unsaved changes.
 */
@Composable
private fun UnsavedChangesDialog(
    onDiscard: () -> Unit,
    onKeepEditing: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onKeepEditing,
        title = {
            Text(text = stringResource(R.string.unsaved_changes_title))
        },
        text = {
            Text(text = stringResource(R.string.unsaved_changes_message))
        },
        confirmButton = {
            TextButton(onClick = onDiscard) {
                Text(text = stringResource(R.string.discard))
            }
        },
        dismissButton = {
            TextButton(onClick = onKeepEditing) {
                Text(text = stringResource(R.string.keep_editing))
            }
        }
    )
}

/**
 * Load Error Banner
 *
 * Displays a persistent error banner at the top of the screen when entry loading fails.
 * Includes retry and dismiss actions.
 */
@Composable
private fun LoadErrorBanner(
    errorMessage: String,
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.errorContainer,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Error icon
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(24.dp)
            )

            // Error message
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.weight(1f)
            )

            // Retry button
            TextButton(onClick = onRetry) {
                Text(
                    text = stringResource(R.string.retry),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }

            // Dismiss button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.dismiss_error),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}
