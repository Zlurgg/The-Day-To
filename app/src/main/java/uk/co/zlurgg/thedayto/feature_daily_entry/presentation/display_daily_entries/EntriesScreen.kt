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
import androidx.navigation.NavController
import org.koin.androidx.compose.koinViewModel
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.presentation.util.Screen
import uk.co.zlurgg.thedayto.core.presentation.util.datestampToMonthValue
import uk.co.zlurgg.thedayto.core.presentation.util.datestampToYearValue
import uk.co.zlurgg.thedayto.core.presentation.util.dayToDatestampForCurrentMonthAndYear
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.components.CalenderDay
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.components.EntryItem
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.components.OrderSection
import uk.co.zlurgg.thedayto.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.ui.theme.paddingVeryLarge
import uk.co.zlurgg.thedayto.ui.theme.paddingXXSmall
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun EntriesScreen(
    navController: NavController,
    viewModel: EntriesViewModel = koinViewModel(),
    onSignOut: () -> Unit
) {
    val state = viewModel.state.value
    val snackbarHostState = remember { SnackbarHostState() }
//    val scope = rememberCoroutineScope()

    val currentDate = LocalDate.now()
    var date by remember {
        mutableStateOf(currentDate)
    }
    val daysInMonth = date.lengthOfMonth()
    val dates = MutableList(daysInMonth) { it }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(paddingSmall),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.your_month_in_colour),
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(
                    onClick = {
                        viewModel.onEvent(EntriesEvent.ToggleOrderSection)
                    },
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = stringResource(R.string.sort)
                    )
                }
            }
            AnimatedVisibility(
                visible = state.isOrderSectionVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                OrderSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = paddingVeryLarge),
                    dailyEntryOrder = state.dailyEntryOrder,
                    onOrderChange = {
                        viewModel.onEvent(EntriesEvent.Order(it))
                    },
                    onSignOut = onSignOut
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                    /** TODO add drop down list to select year **/
                    Row {
                        Text(text = date.month.toString())
                        Spacer(modifier = Modifier.padding(horizontal = paddingXXSmall))
                        Text(text = date.year.toString())
                    }
                }
                Box(
                    modifier = Modifier.padding(paddingXXSmall)
                ) {
                    /** pages are indexed so 12 months as 0-12 but month
                     * value isn't so -1 from month value for initial **/
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
                                modifier = Modifier
                                    .systemBarsPadding(),
                                columns = GridCells.Fixed(7),
                                contentPadding = PaddingValues(
                                    horizontal = 16.dp,
                                    vertical = 16.dp
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                var addNumberToCalenderIfNoEntryForDateExists = true
                                items(dates) {
                                    val entryDate =
                                        dayToDatestampForCurrentMonthAndYear(
                                            it + 1,
                                            date.monthValue,
                                            date.year
                                        )
                                    state.entries.forEach { entry ->
                                        if (entryDate == entry.dateStamp) {
                                            CalenderDay(
                                                entry = entry,
                                                modifier = Modifier
                                                    .clickable {
                                                        navController.navigate(
                                                            "${Screen.AddEditEntryScreen.route}?entryId=${entry.id}&showBackButton=${true}"
                                                        )
                                                    }
                                            )
                                        } else if (addNumberToCalenderIfNoEntryForDateExists && entryDate != currentDate.atStartOfDay()
                                                .toEpochSecond(ZoneOffset.UTC)
                                        ) {
                                            addNumberToCalenderIfNoEntryForDateExists = false
                                            Box(
                                                modifier = Modifier,
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    modifier = Modifier.alpha(0.5f),
                                                    text = "${it + 1}",
                                                    style = MaterialTheme.typography.headlineSmall,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    addNumberToCalenderIfNoEntryForDateExists = true
                                }
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(paddingMedium))
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(state.entries) { entry ->
                        if (date.monthValue.toString() == datestampToMonthValue(entry.dateStamp)
                            && date.year.toString() == datestampToYearValue(entry.dateStamp)
                        ) {
                            EntryItem(
                                entry = entry,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        navController.navigate(
                                            "${Screen.AddEditEntryScreen.route}?entryId=${entry.id}&showBackButton=${true}"
                                        )
                                    },
                                /*                                onDeleteClick = {
                                                                    viewModel.onEvent(EntriesEvent.DeleteEntry(entry))
                                                                    scope.launch {
                                                                        val result = snackbarHostState.showSnackbar(
                                                                            message = "Entry deleted",
                                                                            actionLabel = "Undo"
                                                                        )
                                                                        if (result == SnackbarResult.ActionPerformed) {
                                                                            viewModel.onEvent(EntriesEvent.RestoreEntry)
                                                                        }
                                                                    }
                                                                }*/
                            )
                        }
                        Spacer(modifier = Modifier.height(paddingMedium))
                    }
                }

            }
        }
    )
}