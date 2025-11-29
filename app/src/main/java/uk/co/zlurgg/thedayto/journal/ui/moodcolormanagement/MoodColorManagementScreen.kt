package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.toColorInt
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.components.CustomSnackbarHost
import uk.co.zlurgg.thedayto.core.ui.components.LoadErrorBanner
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.core.ui.theme.paddingVeryLarge
import uk.co.zlurgg.thedayto.core.ui.theme.paddingXXSmall
import uk.co.zlurgg.thedayto.journal.ui.overview.util.UiConstants
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.ui.editor.components.EditMoodColorDialog
import uk.co.zlurgg.thedayto.journal.ui.editor.components.MoodColorPickerDialog
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.components.MoodColorSortSection
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementAction
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementUiEvent
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementUiState
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorWithCount

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun MoodColorManagementScreenRoot(
    onNavigateBack: () -> Unit,
    viewModel: MoodColorManagementViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle one-time UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is MoodColorManagementUiEvent.ShowSnackbar -> {
                    // Handle undo action or clear deleted entry on dismiss (only if actionLabel was "Undo")
                    if (event.actionLabel == "Undo") {
                        val result = snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            withDismissAction = false,
                            duration = SnackbarDuration.Short
                        )
                        when (result) {
                            SnackbarResult.ActionPerformed -> {
                                viewModel.onAction(MoodColorManagementAction.RestoreMoodColor)
                            }
                            SnackbarResult.Dismissed -> {
                                viewModel.onAction(MoodColorManagementAction.ClearRecentlyDeleted)
                            }
                        }
                    } else {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            actionLabel = event.actionLabel,
                            withDismissAction = false,
                            duration = SnackbarDuration.Short
                        )
                    }
                }
            }
        }
    }

    // Add mood color dialog
    MoodColorPickerDialog(
        showDialog = uiState.showAddMoodColorDialog,
        onDismiss = { viewModel.onAction(MoodColorManagementAction.DismissAddMoodColorDialog) },
        onSave = { mood, colorHex ->
            viewModel.onAction(MoodColorManagementAction.SaveNewMoodColor(mood, colorHex))
        }
    )

    // Edit mood color dialog
    uiState.editingMoodColor?.let { moodColor ->
        EditMoodColorDialog(
            moodColor = moodColor,
            showDialog = true,
            onDismiss = { viewModel.onAction(MoodColorManagementAction.DismissEditMoodColorDialog) },
            onSave = { newMood, newColorHex ->
                moodColor.id?.let { id ->
                    viewModel.onAction(
                        MoodColorManagementAction.SaveEditedMoodColor(
                            moodColorId = id,
                            newMood = newMood,
                            newColorHex = newColorHex
                        )
                    )
                }
            }
        )
    }

    MoodColorManagementScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState
    )
}

/**
 * Presenter composable - pure UI, no ViewModel dependency
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodColorManagementScreen(
    uiState: MoodColorManagementUiState,
    onAction: (MoodColorManagementAction) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.manage_mood_colors),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = {
            CustomSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(paddingMedium)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction(MoodColorManagementAction.ShowAddMoodColorDialog)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_mood_color)
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Error banner
            if (uiState.loadError != null) {
                LoadErrorBanner(
                    errorMessage = uiState.loadError,
                    onRetry = { onAction(MoodColorManagementAction.RetryLoadMoodColors) },
                    onDismiss = { onAction(MoodColorManagementAction.DismissLoadError) }
                )
            }

            // Sort section (hide during loading)
            if (!uiState.isLoading) {
                MoodColorSortSection(
                    modifier = Modifier.padding(horizontal = paddingMedium, vertical = paddingSmall),
                    moodColorOrder = uiState.sortOrder,
                    onOrderChange = { order ->
                        onAction(MoodColorManagementAction.ToggleSortOrder(order))
                    }
                )
            }

            // Loading state
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.moodColorsWithCount.isEmpty() && uiState.loadError == null) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingMedium),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.no_mood_colors),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(paddingSmall))
                        Text(
                            text = stringResource(R.string.tap_plus_to_add),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Mood color list
                LazyColumn(
                    contentPadding = PaddingValues(paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(paddingSmall)
                ) {
                    items(
                        items = uiState.moodColorsWithCount,
                        key = { it.moodColor.id ?: it.moodColor.mood }
                    ) { moodColorWithCount ->
                        MoodColorCard(
                            moodColorWithCount = moodColorWithCount,
                            onEdit = {
                                onAction(MoodColorManagementAction.ShowEditMoodColorDialog(moodColorWithCount.moodColor))
                            },
                            onDelete = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAction(MoodColorManagementAction.DeleteMoodColor(moodColorWithCount.moodColor))
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Card displaying a mood color with entry count and actions.
 */
@Composable
private fun MoodColorCard(
    moodColorWithCount: MoodColorWithCount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val moodColor = moodColorWithCount.moodColor
    val color = try {
        Color("#${moodColor.color}".toColorInt())
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = UiConstants.ENTRY_CARD_ELEVATION_DEFAULT)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onEdit)
                .padding(paddingMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(paddingVeryLarge)
                    .clip(CircleShape)
                    .background(color)
                    .border(
                        width = paddingXXSmall,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape
                    )
            )

            Spacer(modifier = Modifier.width(paddingMedium))

            // Mood name and entry count
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = moodColor.mood,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (moodColorWithCount.entryCount == 1) {
                        stringResource(R.string.entry_count_one)
                    } else {
                        stringResource(R.string.entry_count_many, moodColorWithCount.entryCount)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Edit button
            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_mood_color),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Delete button
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_mood_color),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

// ============== Previews ==============

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun MoodColorManagementScreenPreview() {
    TheDayToTheme {
        MoodColorManagementScreen(
            uiState = MoodColorManagementUiState(
                moodColorsWithCount = listOf(
                    MoodColorWithCount(
                        moodColor = MoodColor(
                            id = 1,
                            mood = "Happy",
                            color = "FFD700",
                            dateStamp = 0
                        ),
                        entryCount = 15
                    ),
                    MoodColorWithCount(
                        moodColor = MoodColor(
                            id = 2,
                            mood = "Sad",
                            color = "4169E1",
                            dateStamp = 0
                        ),
                        entryCount = 3
                    ),
                    MoodColorWithCount(
                        moodColor = MoodColor(
                            id = 3,
                            mood = "Excited",
                            color = "FF6347",
                            dateStamp = 0
                        ),
                        entryCount = 1
                    )
                )
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MoodColorManagementScreenEmptyPreview() {
    TheDayToTheme {
        MoodColorManagementScreen(
            uiState = MoodColorManagementUiState(),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = SnackbarHostState()
        )
    }
}
