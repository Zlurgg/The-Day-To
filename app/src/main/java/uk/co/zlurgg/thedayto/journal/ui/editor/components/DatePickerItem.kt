package uk.co.zlurgg.thedayto.journal.ui.editor.components

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.datetime.date.DatePickerDefaults
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.util.datestampToFormattedDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Pure presenter component for date selection.
 * No ViewModel dependency - receives state and callbacks as parameters.
 *
 * @param selectedDate The currently selected date (Unix timestamp in seconds)
 * @param onDateSelected Callback when a new date is selected
 * @param modifier Optional modifier
 */
@Composable
fun DatePickerItem(
    selectedDate: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateDialogState = rememberMaterialDialogState()
    var mExpanded by remember { mutableStateOf(false) }

    val date = Instant.ofEpochSecond(selectedDate)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()

    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
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
            text = datestampToFormattedDate(selectedDate),
        )
        Icon(
            imageVector = icon,
            contentDescription = stringResource(R.string.date_picker_dropdown_button),
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
                positiveButton(text = stringResource(R.string.ok), onClick = { mExpanded = false })
                negativeButton(
                    text = stringResource(R.string.cancel),
                    onClick = { mExpanded = false })
            }
        ) {
            datepicker(
                initialDate = date,
                title = stringResource(R.string.pick_a_date),
                colors = DatePickerDefaults.colors(),
                allowedDateValidator = {
                    it.isEqual(LocalDate.now())
                }
            ) {
                onDateSelected(
                    it.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                )
            }
        }
    }
}
