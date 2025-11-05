package uk.co.zlurgg.thedayto.journal.ui.overview.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.ui.overview.util.CalendarUtils
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToMonthValue
import uk.co.zlurgg.thedayto.journal.ui.util.datestampToYearValue
import uk.co.zlurgg.thedayto.journal.ui.util.dayToDatestampForCurrentMonthAndYear
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium
import uk.co.zlurgg.thedayto.core.ui.theme.paddingSmall
import androidx.compose.foundation.gestures.Orientation

/**
 * Self-contained calendar section with month navigation and date selection.
 * Handles its own state for current visible month and filtering entries.
 */
@Composable
fun CalendarSection(
    entries: List<Entry>,
    currentDate: LocalDate,
    onDateClick: (entryId: Int?, entryDate: Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var date by remember { mutableStateOf(currentDate) }
    var showMonthYearPicker by remember { mutableStateOf(false) }
    val daysInMonth = date.lengthOfMonth()

    // Calculate the day of week for the first day of the month (Monday = 1, Sunday = 7)
    val firstDayOfWeek = date.withDayOfMonth(1).dayOfWeek.value
    // Create empty cells for days before the 1st (Monday=1 needs 0 empty, Tuesday=2 needs 1, etc.)
    val emptyCellsAtStart = firstDayOfWeek - 1
    val totalCells = emptyCellsAtStart + daysInMonth

    Column(modifier = modifier) {
        // Filter entries for current month/year
        val filteredEntries = entries.filter { entry ->
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
            modifier = Modifier.fillMaxWidth()
        )

        // Calendar grid with infinite pager
        CalendarMonthGrid(
            date = date,
            currentDate = currentDate,
            entries = entries,
            daysInMonth = daysInMonth,
            emptyCellsAtStart = emptyCellsAtStart,
            totalCells = totalCells,
            onDateChange = { newDate -> date = newDate },
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
            val isCurrentMonth = date.year == currentDate.year && date.monthValue == currentDate.monthValue

            IconButton(
                onClick = onHomeClick,
                enabled = !isCurrentMonth
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Return to current month",
                    tint = if (isCurrentMonth) {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }

            // Month/Year text (clickable for picker)
            Row(
                modifier = Modifier.clickable { onHeaderClick() }
            ) {
                Text(
                    text = date.month.getDisplayName(TextStyle.FULL, Locale.getDefault()),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.padding(horizontal = paddingSmall))
                Text(
                    text = date.year.toString(),
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        Text(
            text = "▼",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Infinite horizontal pager for calendar months with swipe navigation.
 */
@Composable
private fun CalendarMonthGrid(
    date: LocalDate,
    currentDate: LocalDate,
    entries: List<Entry>,
    daysInMonth: Int,
    emptyCellsAtStart: Int,
    totalCells: Int,
    onDateChange: (LocalDate) -> Unit,
    onDateClick: (entryId: Int?, entryDate: Long?) -> Unit
) {
    Box {
        // Constants for infinite pager
        val initialPage = 1_000_000_000

        // Helper to calculate month offset between dates
        fun calculateDateOffset(current: LocalDate, initial: LocalDate): Long {
            return CalendarUtils.calculateMonthsBetween(initial, current)
        }

        // Wrap pager in key(date) so it recreates when MonthYearPicker changes date
        key(date) {
            // Capture the date at the time of pager creation
            val pagerCreationDate = date

            val pagerState = rememberPagerState(
                initialPage = initialPage,
                initialPageOffsetFraction = 0f,
                pageCount = { Int.MAX_VALUE }
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
                                val entry = entries.find { it.dateStamp == entryDate }

                                if (entry != null) {
                                    CalenderDay(
                                        entry = entry,
                                        modifier = Modifier.clickable {
                                            onDateClick(entry.id, null)
                                        }
                                    )
                                } else {
                                    // No entry for this date - determine if clickable
                                    val isToday = CalendarUtils.isToday(entryDate, currentDate)
                                    val isPast = CalendarUtils.isPast(entryDate, currentDate)

                                    Box(
                                        modifier = Modifier
                                            .then(
                                                when {
                                                    isToday -> Modifier
                                                        .border(
                                                            2.dp,
                                                            MaterialTheme.colorScheme.primary,
                                                            androidx.compose.foundation.shape.CircleShape
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
                                                    isToday -> 1f
                                                    isPast -> 0.7f    // Darker for clickable past dates
                                                    else -> 0.3f    // Very faded for future dates
                                                }
                                            ),
                                            text = "$dayNumber",
                                            style = MaterialTheme.typography.headlineSmall,
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

