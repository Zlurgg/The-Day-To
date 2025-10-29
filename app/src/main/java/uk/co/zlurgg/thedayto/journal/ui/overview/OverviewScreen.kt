package uk.co.zlurgg.thedayto.journal.ui.overview

import android.Manifest
import android.content.res.Configuration
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.navigation.EditorRoute
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.core.ui.theme.paddingXXSmall
import uk.co.zlurgg.thedayto.core.ui.util.datestampToMonthValue
import uk.co.zlurgg.thedayto.core.ui.util.datestampToYearValue
import uk.co.zlurgg.thedayto.core.ui.util.dayToDatestampForCurrentMonthAndYear
import uk.co.zlurgg.thedayto.journal.ui.overview.components.CalenderDay
import uk.co.zlurgg.thedayto.journal.ui.overview.components.CreateEntryReminderDialog
import uk.co.zlurgg.thedayto.journal.ui.overview.components.DayOfWeekHeader
import uk.co.zlurgg.thedayto.journal.ui.overview.components.EmptyState
import uk.co.zlurgg.thedayto.journal.ui.overview.components.EntryItem
import uk.co.zlurgg.thedayto.journal.ui.overview.components.EntrySortSection
import uk.co.zlurgg.thedayto.journal.ui.overview.components.MonthStatistics
import uk.co.zlurgg.thedayto.journal.ui.overview.components.MonthYearPickerDialog
import uk.co.zlurgg.thedayto.journal.ui.overview.components.SettingsMenu
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewAction
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiEvent
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiState
import uk.co.zlurgg.thedayto.journal.ui.overview.util.SampleEntries
import java.time.LocalDate
import java.time.ZoneOffset

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
    var hasNotificationPermission by remember { mutableStateOf(viewModel.hasNotificationPermission()) }

    // Permission launcher for Android 13+
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.onNotificationPermissionGranted()
            hasNotificationPermission = true // Update state to trigger recomposition
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
            }
        }
    }

    // Delegate to presenter
    OverviewScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToEntry = { entryId ->
            navController.navigate(
                EditorRoute(entryId = entryId, showBackButton = true)
            )
        },
        snackbarHostState = snackbarHostState,
        hasNotificationPermission = hasNotificationPermission
    )
}

/**
 * Presenter composable - pure UI, no ViewModel dependency
 */
@Composable
private fun OverviewScreen(
    uiState: OverviewUiState,
    onAction: (OverviewAction) -> Unit,
    onNavigateToEntry: (Int?) -> Unit,
    snackbarHostState: SnackbarHostState,
    hasNotificationPermission: Boolean,
    modifier: Modifier = Modifier
) {
    val currentDate = LocalDate.now()
    var date by remember { mutableStateOf(currentDate) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    val daysInMonth = date.lengthOfMonth()

    // Calculate the day of week for the first day of the month (Monday = 1, Sunday = 7)
    val firstDayOfWeek = date.withDayOfMonth(1).dayOfWeek.value
    // Create empty cells for days before the 1st (Monday=1 needs 0 empty, Tuesday=2 needs 1, etc.)
    val emptyCellsAtStart = firstDayOfWeek - 1
    val totalCells = emptyCellsAtStart + daysInMonth

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Only show FAB when today's entry is missing
            if (!uiState.hasTodayEntry) {
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
                    .padding(paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = uiState.greeting,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                SettingsMenu(
                    hasNotificationPermission = hasNotificationPermission,
                    onRequestNotificationPermission = { onAction(OverviewAction.RequestNotificationPermission) },
                    onSignOut = { onAction(OverviewAction.RequestSignOut) }
                )
            }
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(paddingXXSmall)
            ) {
                // Filter entries for current month/year
                val filteredEntries = uiState.entries.filter { entry ->
                    date.monthValue.toString() == datestampToMonthValue(entry.dateStamp) &&
                            date.year.toString() == datestampToYearValue(entry.dateStamp)
                }

                // Month statistics summary
                MonthStatistics(
                    entries = filteredEntries,
                    daysInMonth = daysInMonth,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = paddingMedium)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingSmall)
                        .clickable { showMonthYearPicker = true },
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row {
                        Text(
                            text = date.month.toString(),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.padding(horizontal = paddingSmall))
                        Text(
                            text = date.year.toString(),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    Text(
                        text = "▼",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Month/Year picker dialog
                if (showMonthYearPicker) {
                    MonthYearPickerDialog(
                        currentDate = date,
                        onDismiss = { showMonthYearPicker = false },
                        onDateSelected = { newDate ->
                            date = newDate
                        }
                    )
                }

                // Day-of-week labels
                DayOfWeekHeader(
                    modifier = Modifier.fillMaxWidth()
                )

                Box(
                    modifier = Modifier
                ) {
                    val pagerState = rememberPagerState(
                        initialPage = date.monthValue - 1,
                        initialPageOffsetFraction = 0f,
                        pageCount = { 12 }
                    )
                    LaunchedEffect(pagerState) {
                        snapshotFlow { pagerState.currentPage }.collect { calenderPage ->
                            if (calenderPage < (date.monthValue - 1)) {
                                date = date.minusMonths(1)
                            } else if (calenderPage > (date.monthValue - 1)) {
                                date = date.plusMonths(1)
                            }
                        }
                    }
                    HorizontalPager(
                        modifier = Modifier,
                        state = pagerState,
                        pageSpacing = 0.dp,
                        userScrollEnabled = true,
                        reverseLayout = false,
                        contentPadding = PaddingValues(0.dp),
                        beyondViewportPageCount = 0,
                        key = { it },
                        pageSize = PageSize.Fill,
                        flingBehavior = PagerDefaults.flingBehavior(state = pagerState),
                        pageNestedScrollConnection = PagerDefaults.pageNestedScrollConnection(
                            pagerState,
                            Orientation.Horizontal
                        ),
                        pageContent = { _ ->
                            LazyVerticalGrid(
                                modifier = Modifier.systemBarsPadding(),
                                columns = GridCells.Fixed(7),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 0.dp,
                                    bottom = 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(totalCells) { index ->
                                    // Check if this is an empty cell before the first day
                                    if (index < emptyCellsAtStart) {
                                        // Empty cell - just a spacer
                                        Box(modifier = Modifier)
                                    } else {
                                        // Calculate the actual day number (1-based)
                                        val dayNumber = index - emptyCellsAtStart + 1
                                        val entryDate = dayToDatestampForCurrentMonthAndYear(
                                            dayNumber,
                                            date.monthValue,
                                            date.year
                                        )
                                        val entry = uiState.entries.find { it.dateStamp == entryDate }

                                        if (entry != null) {
                                            CalenderDay(
                                                entry = entry,
                                                modifier = Modifier.clickable {
                                                    onNavigateToEntry(entry.id)
                                                }
                                            )
                                        } else {
                                            val isToday = entryDate == currentDate.atStartOfDay()
                                                .toEpochSecond(ZoneOffset.UTC)

                                            Box(
                                                modifier = Modifier
                                                    .then(
                                                        if (isToday) {
                                                            Modifier
                                                                .border(
                                                                    2.dp,
                                                                    MaterialTheme.colorScheme.primary,
                                                                    androidx.compose.foundation.shape.CircleShape
                                                                )
                                                                .clickable { onNavigateToEntry(null) }
                                                        } else {
                                                            Modifier
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    modifier = Modifier.alpha(if (isToday) 1f else 0.5f),
                                                    text = "$dayNumber",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(paddingMedium))

                // Entry sorting controls
                EntrySortSection(
                    modifier = Modifier.padding(vertical = paddingSmall),
                    entryOrder = uiState.entryOrder,
                    onOrderChange = { onAction(OverviewAction.Order(it)) }
                )

                Spacer(modifier = Modifier.height(paddingSmall))

                // Show empty state if no entries, otherwise show list
                if (filteredEntries.isEmpty()) {
                    EmptyState()
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(
                            items = filteredEntries,
                            key = { it.id ?: 0 }
                        ) { entry ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically(
                                    initialOffsetY = { it / 4 }
                                )
                            ) {
                                Column {
                                    EntryItem(
                                        entry = entry,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                onNavigateToEntry(entry.id)
                                            }
                                    )
                                    Spacer(modifier = Modifier.height(paddingMedium))
                                }
                            }
                        }
                    }
                }
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
            onNavigateToEntry = {},
            snackbarHostState = remember { SnackbarHostState() },
            hasNotificationPermission = true
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
            onNavigateToEntry = {},
            snackbarHostState = remember { SnackbarHostState() },
            hasNotificationPermission = true
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
            onNavigateToEntry = {},
            snackbarHostState = remember { SnackbarHostState() },
            hasNotificationPermission = false
        )
    }
}
