package uk.co.zlurgg.thedayto.journal.ui.overview

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.core.ui.components.AboutDialog
import uk.co.zlurgg.thedayto.core.ui.components.HelpDialog
import uk.co.zlurgg.thedayto.core.ui.navigation.EditorRoute
import uk.co.zlurgg.thedayto.core.ui.notifications.NotificationSettingsDialog
import uk.co.zlurgg.thedayto.core.ui.notifications.PermissionPermanentlyDeniedDialog
import uk.co.zlurgg.thedayto.core.ui.notifications.SystemNotificationDisabledDialog
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.util.AndroidSystemUtils
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingXXSmall
import uk.co.zlurgg.thedayto.journal.ui.overview.components.CalendarSection
import uk.co.zlurgg.thedayto.journal.ui.overview.components.CreateEntryReminderDialog
import uk.co.zlurgg.thedayto.journal.ui.overview.components.EntriesListSection
import uk.co.zlurgg.thedayto.journal.ui.overview.components.OverviewTutorialDialog
import uk.co.zlurgg.thedayto.journal.ui.overview.components.SettingsMenu
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewAction
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiEvent
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiState
import uk.co.zlurgg.thedayto.journal.ui.overview.util.SampleEntries
import java.time.LocalDate

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun OverviewScreenRoot(
    navController: NavController,
    viewModel: OverviewViewModel = koinViewModel(),
    onNavigateToSignIn: () -> Unit,
    onShowSignOutDialog: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var showTutorialDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showSystemNotificationDialog by remember { mutableStateOf(false) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onNotificationPermissionGranted()
        } else {
            viewModel.onNotificationPermissionDenied()
        }
    }

    // Handle one-time UI events
    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is OverviewUiEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is OverviewUiEvent.NavigateToSignIn -> {
                    onNavigateToSignIn()
                }
                is OverviewUiEvent.NavigateToEditor -> {
                    navController.navigate(
                        EditorRoute(entryId = event.entryId, showBackButton = true)
                    )
                }
                is OverviewUiEvent.RequestNotificationPermission -> {
                    // Request permission on Android 13+, otherwise just setup notifications
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        viewModel.onNotificationPermissionGranted()
                    }
                }
                is OverviewUiEvent.ShowSignOutDialog -> {
                    onShowSignOutDialog()
                }
                is OverviewUiEvent.ShowTutorialDialog -> {
                    showTutorialDialog = true
                }
                is OverviewUiEvent.ShowHelpDialog -> {
                    showHelpDialog = true
                }
                is OverviewUiEvent.ShowAboutDialog -> {
                    showAboutDialog = true
                }
                is OverviewUiEvent.ShowSystemNotificationWarning -> {
                    showSystemNotificationDialog = true
                }
                is OverviewUiEvent.ShowPermissionPermanentlyDeniedDialog -> {
                    showPermissionDeniedDialog = true
                }
            }
        }
    }

    // Show tutorial dialog when event is triggered (first-time users)
    if (showTutorialDialog) {
        OverviewTutorialDialog(
            onDismiss = {
                showTutorialDialog = false
                viewModel.onAction(OverviewAction.DismissTutorial)
            }
        )
    }

    // Show help dialog from settings menu
    if (showHelpDialog) {
        HelpDialog(
            onDismiss = { showHelpDialog = false }
        )
    }

    // Show about dialog from settings menu
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }

    // System notification warning dialog
    if (showSystemNotificationDialog) {
        SystemNotificationDisabledDialog(
            onDismiss = { showSystemNotificationDialog = false },
            onOpenSettings = {
                AndroidSystemUtils.openSystemNotificationSettings(context)
                showSystemNotificationDialog = false
            }
        )
    }

    // Permission permanently denied dialog
    if (showPermissionDeniedDialog) {
        PermissionPermanentlyDeniedDialog(
            onDismiss = { showPermissionDeniedDialog = false },
            onOpenSettings = {
                AndroidSystemUtils.openAppSettings(context)
                showPermissionDeniedDialog = false
            }
        )
    }

    // Delegate to presenter
    OverviewScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToEntry = { entryId, entryDate ->
            navController.navigate(
                EditorRoute(
                    entryId = entryId,
                    entryDate = entryDate,
                    showBackButton = true
                )
            )
        },
        onNavigateToStats = {
            navController.navigate(uk.co.zlurgg.thedayto.core.ui.navigation.StatsRoute)
        },
        snackbarHostState = snackbarHostState
    )
}

/**
 * Presenter composable - pure UI, no ViewModel dependency
 */
@Composable
private fun OverviewScreen(
    uiState: OverviewUiState,
    onAction: (OverviewAction) -> Unit,
    onNavigateToEntry: (entryId: Int?, entryDate: Long?) -> Unit,
    onNavigateToStats: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val currentDate = LocalDate.now()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Only show FAB when today's entry is missing with smooth animation
            AnimatedVisibility(
                visible = !uiState.hasTodayEntry,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = { onAction(OverviewAction.CreateNewEntry) },
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(uk.co.zlurgg.thedayto.R.string.create_today_entry)
                    )
                }
            }
        },
        modifier = modifier,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .systemBarsPadding()
                    .padding(uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SettingsMenu(
                    onOpenNotificationSettings = { onAction(OverviewAction.OpenNotificationSettings) },
                    onShowHelp = { onAction(OverviewAction.RequestShowHelp) },
                    onShowAbout = { onAction(OverviewAction.RequestShowAbout) },
                    onNavigateToStats = onNavigateToStats,
                    onSignOut = { onAction(OverviewAction.RequestSignOut) }
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(paddingXXSmall)
            ) {
                // Calendar section with month navigation
                CalendarSection(
                    entries = uiState.entries,
                    currentDate = currentDate,
                    onDateClick = onNavigateToEntry,
                    onStatsClick = onNavigateToStats,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(paddingMedium))

                // Entries list section with sorting
                EntriesListSection(
                    entries = uiState.entries,
                    entryOrder = uiState.entryOrder,
                    onOrderChange = { onAction(OverviewAction.Order(it)) },
                    onEntryClick = { entryId -> onNavigateToEntry(entryId, null) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    )

    // Entry reminder dialog
    if (uiState.showEntryReminderDialog) {
        CreateEntryReminderDialog(
            onDismiss = { onAction(OverviewAction.DismissEntryReminder) },
            onCreateEntry = { onAction(OverviewAction.CreateTodayEntry) }
        )
    }

    // Notification settings dialog
    if (uiState.showNotificationSettingsDialog) {
        NotificationSettingsDialog(
            enabled = uiState.notificationsEnabled,
            hour = uiState.notificationHour,
            minute = uiState.notificationMinute,
            hasPermission = uiState.hasNotificationPermission,
            onDismiss = { onAction(OverviewAction.DismissNotificationSettings) },
            onRequestPermission = { onAction(OverviewAction.RequestNotificationPermission) },
            onSave = { enabled, hour, minute ->
                onAction(OverviewAction.SaveNotificationSettings(enabled, hour, minute))
            }
        )
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OverviewScreenPreview() {
    TheDayToTheme {
        OverviewScreen(
            uiState = OverviewUiState(
                entries = SampleEntries.allSamples,
                greeting = "Good morning"
            ),
            onAction = {},
            onNavigateToEntry = { _, _ -> },
            onNavigateToStats = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Single Entry - Light", showBackground = true)
@Preview(name = "Single Entry - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OverviewScreenSingleEntryPreview() {
    TheDayToTheme {
        OverviewScreen(
            uiState = OverviewUiState(
                entries = listOf(SampleEntries.sampleEntry1),
                greeting = "Good afternoon"
            ),
            onAction = {},
            onNavigateToEntry = { _, _ -> },
            onNavigateToStats = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}

@Preview(name = "Empty State - Light", showBackground = true)
@Preview(name = "Empty State - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun OverviewScreenEmptyPreview() {
    TheDayToTheme {
        OverviewScreen(
            uiState = OverviewUiState(
                entries = emptyList(),
                greeting = "Good evening"
            ),
            onAction = {},
            onNavigateToEntry = { _, _ -> },
            onNavigateToStats = {},
            snackbarHostState = remember { SnackbarHostState() }
        )
    }
}
