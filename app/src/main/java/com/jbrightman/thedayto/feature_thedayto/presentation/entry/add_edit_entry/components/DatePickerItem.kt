package com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.components

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
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryEvent
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryViewModel
import com.jbrightman.thedayto.feature_thedayto.presentation.util.datestampToFormattedDateText
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun DatePickerItem(
    viewModel: AddEditEntryViewModel
) {
    val dateDialogState = rememberMaterialDialogState()
    var mExpanded by remember { mutableStateOf(false) }
    val dateState = viewModel.entryDate.value
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
            color = Color.DarkGray,
            text = datestampToFormattedDateText(dateState.date),
        )
        Icon(
            imageVector = icon,
            tint = Color.DarkGray,
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
                initialDate = LocalDate.now(),
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

