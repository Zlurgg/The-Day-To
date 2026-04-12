package uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement

import android.content.res.Configuration
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.components.CustomSnackbarHost
import uk.co.zlurgg.thedayto.core.ui.components.JournalCard
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorErrorFormatter
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColorWithCount
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SaveMoodColorUseCase
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.components.DeleteMoodColorConfirmDialog
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.components.SeedRandomMoodColorsDialog
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementAction
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.state.MoodColorManagementUiState
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.EditMoodColorDialog
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorEvent
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorRow

/**
 * Show the "X / 50" counter in the app bar once the user is close to the cap.
 * 80% of MAX_MOOD_COLORS keeps the counter out of the way for typical users
 * while giving power users a heads-up before they hit LimitReached.
 */
private const val MOOD_COLOR_COUNTER_THRESHOLD = 40

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun MoodColorManagementScreenRoot(
    onNavigateBack: () -> Unit,
    viewModel: MoodColorManagementViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Handle one-time UI events (errors only)
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MoodColorEvent.ShowUndoSnackbar -> {
                    // No longer used - we use confirmation dialog instead
                }

                is MoodColorEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = MoodColorErrorFormatter.format(context, event.error),
                    )
                }
            }
        }
    }

    // Seed random mood colors confirmation dialog
    if (state.showSeedRandomDialog) {
        SeedRandomMoodColorsDialog(
            onConfirm = { viewModel.onAction(MoodColorManagementAction.ConfirmSeedRandom) },
            onDismiss = { viewModel.onAction(MoodColorManagementAction.DismissSeedRandomDialog) },
        )
    }

    // Delete confirmation dialog
    state.pendingDelete?.let { moodColor ->
        DeleteMoodColorConfirmDialog(
            moodColor = moodColor,
            onConfirm = { viewModel.onAction(MoodColorManagementAction.ConfirmDelete) },
            onDismiss = { viewModel.onAction(MoodColorManagementAction.DismissDeleteDialog) },
        )
    }

    // Edit dialog (also used for add when editingMoodColor.id is null)
    state.editingMoodColor?.let { moodColor ->
        EditMoodColorDialog(
            moodColor = moodColor,
            showDialog = true,
            onDismiss = { viewModel.onAction(MoodColorManagementAction.DismissDialog) },
            onSave = { newMood, newColorHex ->
                viewModel.onAction(
                    MoodColorManagementAction.SaveMoodColor(
                        moodColor.copy(mood = newMood, color = newColorHex),
                    ),
                )
            },
            externalError = state.dialogError?.let { MoodColorErrorFormatter.format(context, it) },
            onErrorCleared = { viewModel.onAction(MoodColorManagementAction.ClearError) },
        )
    }

    MoodColorManagementScreen(
        state = state,
        onAction = viewModel::onAction,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
    )
}

/**
 * Presenter composable - pure UI, no ViewModel dependency
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MoodColorManagementScreen(
    state: MoodColorManagementUiState,
    onAction: (MoodColorManagementAction) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.manage_mood_colors),
                        style = MaterialTheme.typography.titleLarge,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
                actions = {
                    // Dice button — seed random mood colors from curated pool
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onAction(MoodColorManagementAction.RequestSeedRandom)
                        },
                        enabled = !state.isSeedingInProgress &&
                            state.moodColors.size < SaveMoodColorUseCase.MAX_MOOD_COLORS,
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = stringResource(R.string.seed_random_mood_colors),
                        )
                    }

                    // Only show the counter when approaching the cap — most
                    // users never get close, so it stays out of the way.
                    val activeCount = state.moodColors.size
                    if (activeCount >= MOOD_COLOR_COUNTER_THRESHOLD) {
                        val counterText = stringResource(
                            R.string.mood_color_count_format,
                            activeCount,
                            SaveMoodColorUseCase.MAX_MOOD_COLORS,
                        )
                        val counterDescription = stringResource(
                            R.string.mood_color_count_description,
                            activeCount,
                            SaveMoodColorUseCase.MAX_MOOD_COLORS,
                        )
                        Text(
                            text = counterText,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .padding(end = paddingMedium)
                                .semantics { contentDescription = counterDescription },
                        )
                    }
                },
            )
        },
        snackbarHost = {
            CustomSnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(paddingMedium),
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction(MoodColorManagementAction.AddMoodColor)
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_mood_color),
                )
            }
        },
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // Loading state
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.moodColors.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingMedium),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.no_mood_colors),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(paddingSmall))
                        Text(
                            text = stringResource(R.string.tap_plus_to_add),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            } else {
                // Mood color list with swipe to delete
                LazyColumn(
                    contentPadding = PaddingValues(paddingMedium),
                    verticalArrangement = Arrangement.spacedBy(paddingSmall),
                ) {
                    items(
                        items = state.moodColors,
                        key = { it.moodColor.id ?: 0 },
                    ) { moodColorWithCount ->
                        SwipeToDeleteMoodColorCard(
                            moodColorWithCount = moodColorWithCount,
                            onEdit = {
                                onAction(MoodColorManagementAction.EditMoodColor(moodColorWithCount.moodColor))
                            },
                            onDelete = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAction(
                                    MoodColorManagementAction.RequestDeleteMoodColor(
                                        moodColorWithCount.moodColor,
                                    ),
                                )
                            },
                            onToggleFavorite = {
                                moodColorWithCount.moodColor.id?.let { id ->
                                    onAction(
                                        MoodColorManagementAction.ToggleFavorite(
                                            id = id,
                                            currentValue = moodColorWithCount.moodColor.isFavorite,
                                        ),
                                    )
                                }
                            },
                            isDeleteEnabled = !state.isLoading,
                            pendingDelete = state.pendingDelete,
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToDeleteMoodColorCard(
    moodColorWithCount: MoodColorWithCount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    isDeleteEnabled: Boolean,
    pendingDelete: MoodColor?,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    // Reset swipe position when user cancels the delete dialog
    LaunchedEffect(pendingDelete) {
        if (pendingDelete == null &&
            dismissState.currentValue == SwipeToDismissBoxValue.EndToStart
        ) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Only show background when actively swiping (prevents red corners showing)
            val direction = dismissState.dismissDirection
            if (direction == SwipeToDismissBoxValue.EndToStart) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.error)
                        .padding(horizontal = paddingMedium),
                    contentAlignment = Alignment.CenterEnd,
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = isDeleteEnabled,
        modifier = modifier,
    ) {
        JournalCard(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            elevation = MoodColorConstants.CARD_ELEVATION_DEFAULT,
            pressedElevation = MoodColorConstants.CARD_ELEVATION_PRESSED,
        ) {
            MoodColorRow(
                moodColorWithCount = moodColorWithCount,
                onToggleFavorite = onToggleFavorite,
                onEdit = onEdit,
                showEntryCount = true,
            )
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
            state = MoodColorManagementUiState(
                moodColors = listOf(
                    MoodColorWithCount(
                        moodColor = MoodColor(
                            id = 1,
                            mood = "Happy",
                            color = "FFD700",
                            isFavorite = true,
                            dateStamp = 0,
                        ),
                        entryCount = 15,
                    ),
                    MoodColorWithCount(
                        moodColor = MoodColor(
                            id = 2,
                            mood = "Sad",
                            color = "4169E1",
                            isFavorite = false,
                            dateStamp = 0,
                        ),
                        entryCount = 3,
                    ),
                    MoodColorWithCount(
                        moodColor = MoodColor(
                            id = 3,
                            mood = "Excited",
                            color = "FF6347",
                            isFavorite = false,
                            dateStamp = 0,
                        ),
                        entryCount = 1,
                    ),
                ),
                isLoading = false,
            ),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MoodColorManagementScreenEmptyPreview() {
    TheDayToTheme {
        MoodColorManagementScreen(
            state = MoodColorManagementUiState(isLoading = false),
            onAction = {},
            onNavigateBack = {},
            snackbarHostState = SnackbarHostState(),
        )
    }
}
