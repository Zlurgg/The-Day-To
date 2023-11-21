package com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryEvent
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryViewModel
import com.jbrightman.thedayto.presentation.util.datestampToFormattedDate
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset


@Composable
fun DatePickerItem(
    viewModel: AddEditEntryViewModel = hiltViewModel(),
    entryDate: Long
) {
    val dateDialogState = rememberMaterialDialogState()
    var mExpanded by remember { mutableStateOf(false) }

    /** will change entry date to contain month and year going foward and pass around the long value **/
    val dateState = viewModel.entryDate.value
    dateState.date =  if (entryDate != -1L ) {
        entryDate
    } else {
        viewModel.entryDate.value.date
    }

    val date = Instant.ofEpochSecond(dateState.date)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()


    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClick = {
                    dateDialogState.show()
                    mExpanded = !mExpanded
                },
                role = Role.Button,
            )
    ) {
        Text(
            style = MaterialTheme.typography.headlineSmall,
            text = datestampToFormattedDate(dateState.date),
        )
        Icon(
            imageVector = icon,
            contentDescription = "Date picker dropdown button",
        )
        MaterialDialog(
            dialogState = dateDialogState,
            properties = DialogProperties(
                dismissOnBackPress = true,
            ),
            backgroundColor = MaterialTheme.colorScheme.background,
            elevation = 10.dp,
            onCloseRequest = { mExpanded = false },
            buttons = {
                positiveButton(text = "Ok", onClick = { mExpanded = false })
                negativeButton(text = "Cancel", onClick = { mExpanded = false })
            }
        ) {
            datepicker(
                initialDate = date,
                title = "Pick a date",
                colors = DatePickerDefaults.colors(),
                allowedDateValidator = {
                    !it.isAfter(LocalDate.now())
                }
            ) {
                viewModel.onEvent(
                    AddEditEntryEvent.EnteredDate(
                        it.atStartOfDay().toEpochSecond(
                            ZoneOffset.UTC
                        )
                    )
                )
            }
        }
        if (dateState.date == 0L) {
            viewModel.onEvent(
                AddEditEntryEvent.EnteredDate(
                    LocalDate.now().atStartOfDay().toEpochSecond(
                        ZoneOffset.UTC
                    )
                )
            )
        }
    }
}

