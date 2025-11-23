package uk.co.zlurgg.thedayto.journal.ui.overview.components

import android.content.res.Configuration
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import uk.co.zlurgg.thedayto.journal.domain.model.EntryWithMoodColor
import uk.co.zlurgg.thedayto.journal.ui.overview.util.CalendarConstants
import uk.co.zlurgg.thedayto.journal.ui.overview.util.CalendarUtils
import uk.co.zlurgg.thedayto.journal.ui.overview.util.SampleEntries
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToMonthValue
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToYearValue
import uk.co.zlurgg.thedayto.journal.ui.util.dayToDatestampForCurrentMonthAndYear
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Self-contained calendar section with month navigation and date selection.
 * Handles its own state for current visible month and filtering entries.
 */
@Composable
fun CalendarSection(
    entries: List<EntryWithMoodColor>,
    currentDate: LocalDate,
    modifier: Modifier = Modifier,
    onDateClick: (entryId: Int?, entryDate: Long?) -> Unit,
    onStatsClick: () -> Unit = {},
    onMonthChanged: (month: Int, year: Int) -> Unit = { _, _ -> }
) {
    BoxWithConstraints(modifier = modifier) {
        // Calculate day size to ensure 7 days always fit in available width
        // Account for FlowRow's padding and spacing between items
        val horizontalPadding = CalendarConstants.CALENDAR_HORIZONTAL_PADDING * 2
        val totalSpacing =
            CalendarConstants.CALENDAR_DAY_SPACING * (CalendarConstants.DAYS_IN_WEEK - 1)
        val buffer = CalendarConstants.BUFFER_SIZE
        val availableForDays = maxWidth - horizontalPadding - totalSpacing - buffer
        val calculatedDaySize = availableForDays / CalendarConstants.DAYS_IN_WEEK
        // Ensure we never exceed maximum, but otherwise use calculated size
        val daySize = if (calculatedDaySize > CalendarConstants.DEFAULT_DAY_SIZE_MAX) {
            CalendarConstants.DEFAULT_DAY_SIZE_MAX
        } else {
            calculatedDaySize
        }

        CalendarContent(
            currentDate = currentDate,
            entries = entries,
            daySize = daySize,
            onStatsClick = onStatsClick,
            onDateClick = onDateClick,
            onMonthChanged = onMonthChanged
        )
    }
}

/**
 * Calendar content - extracted to avoid UiComposable scope issues
 */
@Composable
private fun CalendarContent(
    currentDate: LocalDate,
    entries: List<EntryWithMoodColor>,
    daySize: Dp,
    onStatsClick: () -> Unit,
    onDateClick: (entryId: Int?, entryDate: Long?) -> Unit,
    onMonthChanged: (month: Int, year: Int) -> Unit
) {
    var date by remember { mutableStateOf(currentDate) }
    var showMonthYearPicker by remember { mutableStateOf(false) }

    // Notify parent when month changes
    LaunchedEffect(date.monthValue, date.year) {
        onMonthChanged(date.monthValue, date.year)
    }

    // Calculate derived values from date
    val daysInMonth = date.lengthOfMonth()
    val firstDayOfWeek = date.withDayOfMonth(1).dayOfWeek.value
    val emptyCellsAtStart = firstDayOfWeek - 1
    val totalCells = emptyCellsAtStart + daysInMonth
    Column(modifier = Modifier.fillMaxWidth()) {
        // Filter entries for current month/year
        val filteredEntries = entries.filter { entry ->
            date.monthValue.toString() == datestampToMonthValue(entry.dateStamp) &&
                    date.year.toString() == datestampToYearValue(entry.dateStamp)
        }

        // Month statistics summary
        MonthStatistics(
            entries = filteredEntries,
            daysInMonth = daysInMonth,
            onStatsClick = onStatsClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = paddingMedium)
        )

        // Month/Year header with home button
        MonthYearHeader(
            date = date,
            currentDate = currentDate,
            onHomeClick = { date = currentDate },
            onHeaderClick = { showMonthYearPicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingSmall)
        )

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
            daySize = daySize,
            modifier = Modifier.fillMaxWidth()
        )

        // Calendar grid with infinite pager
        CalendarMonthGrid(
            date = date,
            currentDate = currentDate,
            entries = entries,
            emptyCellsAtStart = emptyCellsAtStart,
            totalCells = totalCells,
            daySize = daySize,
            onDateChange = { date = it },
            onDateClick = onDateClick
        )
    }
}


/**
 * Month/Year header with home button for returning to current month.
 */
@Composable
private fun MonthYearHeader(
    date: LocalDate,
    currentDate: LocalDate,
    onHomeClick: () -> Unit,
    onHeaderClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Home button - always visible, disabled when on current month
            val isCurrentMonth =
                date.year == currentDate.year && date.monthValue == currentDate.monthValue

            IconButton(
                onClick = onHomeClick,
                enabled = !isCurrentMonth
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = stringResource(R.string.return_to_current_month),
                    tint = if (isCurrentMonth) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            // Month/Year text (clickable for picker)
            Row(
                modifier = Modifier.clickable { onHeaderClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.padding(horizontal = paddingSmall))
                Text(
                    text = date.year.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = stringResource(R.string.select_month_and_year),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Infinite horizontal pager for calendar months with swipe navigation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalendarMonthGrid(
    date: LocalDate,
    currentDate: LocalDate,
    entries: List<EntryWithMoodColor>,
    emptyCellsAtStart: Int,
    totalCells: Int,
    daySize: Dp,
    onDateChange: (LocalDate) -> Unit,
    onDateClick: (entryId: Int?, entryDate: Long?) -> Unit
) {
    Box {
        // Constants for infinite pager
        val initialPage = CalendarConstants.INITIAL_PAGER_PAGE

        // Helper to calculate month offset between dates
        fun calculateDateOffset(current: LocalDate, initial: LocalDate): Long {
            return CalendarUtils.calculateMonthsBetween(initial, current)
        }

        // Wrap pager in key(date) so it recreates when MonthYearPicker changes date
        key(date) {
            // Capture the date at the time of pager creation
            val pagerCreationDate = date

            // Calculate max page: can go back infinitely, but forward only to current month
            val monthsToCurrentFromCreation =
                CalendarUtils.calculateMonthsBetween(pagerCreationDate, currentDate)
            val maxPageIndex = initialPage + monthsToCurrentFromCreation.toInt()

            val pagerState = rememberPagerState(
                initialPage = initialPage,
                initialPageOffsetFraction = 0f,
                pageCount = { maxPageIndex + 1 }
            )

            LaunchedEffect(pagerState) {
                snapshotFlow { pagerState.currentPage }.collect { page ->
                    // Calculate how many months we've moved from initial page
                    val pageOffsetFromInitial = page - initialPage

                    // Calculate how many months date state is from pager creation date
                    val dateOffsetFromInitial = calculateDateOffset(date, pagerCreationDate)

                    // Only update if they differ (user swiped)
                    if (pageOffsetFromInitial < dateOffsetFromInitial) {
                        onDateChange(date.minusMonths(1))
                    } else if (pageOffsetFromInitial > dateOffsetFromInitial) {
                        onDateChange(date.plusMonths(1))
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
                    // Use FlowRow instead of LazyVerticalGrid to avoid nested scroll issues
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                start = CalendarConstants.CALENDAR_HORIZONTAL_PADDING,
                                end = CalendarConstants.CALENDAR_HORIZONTAL_PADDING,
                                top = 0.dp,
                                bottom = CalendarConstants.CALENDAR_BOTTOM_PADDING
                            ),
                        horizontalArrangement = Arrangement.spacedBy(CalendarConstants.CALENDAR_DAY_SPACING),
                        verticalArrangement = Arrangement.spacedBy(CalendarConstants.CALENDAR_ROW_SPACING),
                        maxItemsInEachRow = CalendarConstants.DAYS_IN_WEEK
                    ) {
                        repeat(totalCells) { index ->
                            // Check if this is an empty cell before the first day
                            if (index < emptyCellsAtStart) {
                                // Empty cell - just a spacer
                                Box(modifier = Modifier.size(daySize))
                            } else {
                                // Calculate the actual day number (1-based)
                                val dayNumber = index - emptyCellsAtStart + 1
                                val entryDate = dayToDatestampForCurrentMonthAndYear(
                                    dayNumber,
                                    date.monthValue,
                                    date.year
                                )
                                val entry = entries.find { it.dateStamp == entryDate }

                                if (entry != null) {
                                    CalendarDay(
                                        entry = entry,
                                        modifier = Modifier
                                            .size(daySize)
                                            .clickable {
                                                onDateClick(entry.id, null)
                                            }
                                    )
                                } else {
                                    // No entry for this date - determine if clickable
                                    val isToday = CalendarUtils.isToday(entryDate, currentDate)
                                    val isPast = CalendarUtils.isPast(entryDate, currentDate)

                                    Box(
                                        modifier = Modifier
                                            .size(daySize)
                                            .then(
                                                when {
                                                    isToday -> Modifier
                                                        .border(
                                                            CalendarConstants.TODAY_BORDER_WIDTH,
                                                            MaterialTheme.colorScheme.primary,
                                                            CircleShape
                                                        )
                                                        .clickable {
                                                            onDateClick(null, entryDate)
                                                        }

                                                    isPast -> Modifier
                                                        .clickable {
                                                            onDateClick(null, entryDate)
                                                        }

                                                    else -> Modifier  // Future dates not clickable
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            modifier = Modifier.alpha(
                                                when {
                                                    isToday -> CalendarConstants.DayAlpha.TODAY
                                                    isPast -> CalendarConstants.DayAlpha.PAST
                                                    else -> CalendarConstants.DayAlpha.FUTURE
                                                }
                                            ),
                                            text = "$dayNumber",
                                            style = MaterialTheme.typography.headlineMedium,
                                            color = if (isToday) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurface,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
        } // Close key(date) block
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalendarSectionPreview() {
    TheDayToTheme {
        CalendarSection(
            entries = SampleEntries.allSamples,
            currentDate = LocalDate.now(),
            onDateClick = { _, _ -> },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(name = "Empty Calendar - Light", showBackground = true)
@Preview(name = "Empty Calendar - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun CalendarSectionEmptyPreview() {
    TheDayToTheme {
        CalendarSection(
            entries = emptyList(),
            currentDate = LocalDate.now(),
            onDateClick = { _, _ -> },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

