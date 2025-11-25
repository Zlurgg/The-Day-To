package uk.co.zlurgg.thedayto.journal.ui.overview.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingExtraSmall
import uk.co.zlurgg.thedayto.journal.ui.overview.util.UiConstants
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthYearPickerDialog(
    currentDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var selectedYear by remember { mutableIntStateOf(currentDate.year) }
    var selectedMonth by remember { mutableIntStateOf(currentDate.monthValue) }

    val now = LocalDate.now()
    val months = Month.entries
    val years = (2020..now.year).toList()

    // Reset selected month if it becomes invalid when switching to current year
    LaunchedEffect(selectedYear) {
        if (selectedYear == now.year && selectedMonth > now.monthValue) {
            selectedMonth = now.monthValue
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.select_month_and_year),
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column {
                // Year selector
                Text(
                    text = stringResource(R.string.month_picker_year_label),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = paddingExtraSmall)
                )
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(UiConstants.MONTH_PICKER_ITEM_SPACING),
                    verticalArrangement = Arrangement.spacedBy(UiConstants.MONTH_PICKER_ITEM_SPACING),
                    modifier = Modifier.height(UiConstants.MONTH_PICKER_MONTH_HEIGHT)
                ) {
                    items(years) { year ->
                        FilterChip(
                            selected = year == selectedYear,
                            onClick = { selectedYear = year },
                            label = {
                                Text(
                                    text = year.toString(),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(UiConstants.MONTH_PICKER_DIVIDER_SPACING))

                // Month selector
                Text(
                    text = stringResource(R.string.month_picker_month_label),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = paddingExtraSmall)
                )

                // Filter out future months when current year is selected
                val availableMonths = if (selectedYear == now.year) {
                    months.filter { it.value <= now.monthValue }
                } else {
                    months
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(UiConstants.MONTH_PICKER_ITEM_SPACING),
                    verticalArrangement = Arrangement.spacedBy(UiConstants.MONTH_PICKER_ITEM_SPACING),
                    modifier = Modifier.height(UiConstants.MONTH_PICKER_YEAR_HEIGHT)
                ) {
                    items(availableMonths) { month ->
                        FilterChip(
                            selected = month.value == selectedMonth,
                            onClick = { selectedMonth = month.value },
                            label = {
                                Text(
                                    text = month.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val newDate = LocalDate.of(selectedYear, selectedMonth, 1)
                    onDateSelected(newDate)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
