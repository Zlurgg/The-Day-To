package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.presentation.Screen
import uk.co.zlurgg.thedayto.core.presentation.util.datestampToMonthValue
import uk.co.zlurgg.thedayto.core.presentation.util.datestampToYearValue
import uk.co.zlurgg.thedayto.core.presentation.util.dayToDatestampForCurrentMonthAndYear
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.components.CalenderDay
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.components.EntryItem
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.components.OrderSection
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.state.EntriesAction
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.state.EntriesUiState
import uk.co.zlurgg.thedayto.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.ui.theme.paddingVeryLarge
import uk.co.zlurgg.thedayto.ui.theme.paddingXXSmall
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Root composable - handles ViewModel, state collection, and side effects
 */
@Composable
fun EntriesScreenRoot(
    navController: NavController,
    viewModel: EntriesViewModel = koinViewModel(),
    onSignOut: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Delegate to presenter
    EntriesScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onNavigateToEntry = { entryId ->
            navController.navigate(
                "${Screen.AddEditEntryScreen.route}?entryId=${entryId}&showBackButton=${true}"
            )
        },
        onSignOut = onSignOut
    )
}

/**
 * Presenter composable - pure UI, no ViewModel dependency
 */
@Composable
private fun EntriesScreen(
    uiState: EntriesUiState,
    onAction: (EntriesAction) -> Unit,
    onNavigateToEntry: (Int?) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val currentDate = LocalDate.now()
    var date by remember { mutableStateOf(currentDate) }
    val daysInMonth = date.lengthOfMonth()
    val dates = MutableList(daysInMonth) { it }

    Scaffold(
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
                    text = stringResource(R.string.your_month_in_colour),
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(
                    onClick = { onAction(EntriesAction.ToggleOrderSection) }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(R.string.sort)
                    )
                }
            }
            AnimatedVisibility(
                visible = uiState.isOrderSectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                OrderSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = paddingVeryLarge),
                    dailyEntryOrder = uiState.dailyEntryOrder,
                    onOrderChange = { onAction(EntriesAction.Order(it)) },
                    onSignOut = onSignOut
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(paddingMedium)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(paddingXXSmall)
                ) {
                    Row {
                        Text(text = date.month.toString())
                        Spacer(modifier = Modifier.padding(horizontal = paddingXXSmall))
                        Text(text = date.year.toString())
                    }
                }
                Box(
                    modifier = Modifier.padding(paddingXXSmall)
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
                                    horizontal = 16.dp,
                                    vertical = 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(dates) { dayIndex ->
                                    val entryDate = dayToDatestampForCurrentMonthAndYear(
                                        dayIndex + 1,
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
                                    } else if (entryDate != currentDate.atStartOfDay()
                                            .toEpochSecond(ZoneOffset.UTC)
                                    ) {
                                        Box(
                                            modifier = Modifier,
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                modifier = Modifier.alpha(0.5f),
                                                text = "${dayIndex + 1}",
                                                style = MaterialTheme.typography.headlineSmall,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(paddingMedium))

                // Filter entries for current month/year
                val filteredEntries = uiState.entries.filter { entry ->
                    date.monthValue.toString() == datestampToMonthValue(entry.dateStamp) &&
                            date.year.toString() == datestampToYearValue(entry.dateStamp)
                }

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredEntries) { entry ->
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
    )
}
