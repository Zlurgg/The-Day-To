package uk.co.zlurgg.thedayto.journal.ui.editor.components

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Minimum year for date selection in the editor.
 * Set to 2020 as a reasonable starting point for journal entries.
 */
private const val MIN_SELECTABLE_YEAR = 2020

/**
 * Material3 DatePicker dialog for selecting entry dates.
 * Prevents selection of future dates.
 *
 * @param currentDate The currently selected date (will be pre-selected)
 * @param onDismiss Callback when dialog is dismissed without selection
 * @param onDateSelected Callback when user confirms date selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorDatePickerDialog(
    currentDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    // Convert LocalDate to milliseconds for DatePicker (using UTC for consistency with app storage)
    val initialDateMillis = currentDate
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

    // Limit to today's date (no future dates)
    val todayMillis = LocalDate.now()
        .atStartOfDay(ZoneOffset.UTC)
        .toInstant()
        .toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDateMillis,
        yearRange = MIN_SELECTABLE_YEAR..LocalDate.now().year,
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                return utcTimeMillis <= todayMillis
            }
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val selectedDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onDateSelected(selectedDate)
                    }
                    onDismiss()
                },
                enabled = datePickerState.selectedDateMillis != null
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        },
        colors = DatePickerDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            headlineContentColor = MaterialTheme.colorScheme.onSurface,
            selectedDayContainerColor = MaterialTheme.colorScheme.primary,
            selectedDayContentColor = MaterialTheme.colorScheme.onPrimary,
            todayContentColor = MaterialTheme.colorScheme.primary,
            todayDateBorderColor = MaterialTheme.colorScheme.primary
        )
    ) {
        DatePicker(state = datePickerState)
    }
}

@Preview(showBackground = true, locale = "en")
@Composable
private fun EditorDatePickerDialogPreview() {
    TheDayToTheme {
        EditorDatePickerDialog(
            currentDate = LocalDate.now(),
            onDismiss = {},
            onDateSelected = {}
        )
    }
}
