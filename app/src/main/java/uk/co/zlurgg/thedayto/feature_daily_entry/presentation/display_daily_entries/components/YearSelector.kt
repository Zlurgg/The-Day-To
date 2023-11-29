package uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun YearSelector() {
    var mExpanded by remember { mutableStateOf(false) }
    val currentDate = LocalDate.now()
    var currentYear by remember {
        mutableIntStateOf(currentDate.year)
    }
    var years = listOf(2023, 2022, 2021, 2020)

    ExposedDropdownMenuBox(
        expanded = mExpanded,
        onExpandedChange = {
            mExpanded = !mExpanded
        }
    ) {
        TextField(
            value = currentYear.toString(),
            onValueChange = { },
            textStyle = MaterialTheme.typography.headlineSmall,
            colors = OutlinedTextFieldDefaults.colors(
                cursorColor = MaterialTheme.colorScheme.primary,
                focusedBorderColor = MaterialTheme.colorScheme.background,
                unfocusedBorderColor = MaterialTheme.colorScheme.background,
                focusedLabelColor = MaterialTheme.colorScheme.background,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii,
            ),
            readOnly = true,
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth(),
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = mExpanded)
            }
        )
        ExposedDropdownMenu(
            expanded = mExpanded,
            onDismissRequest = {
                mExpanded = false
            }
        ) {
            years.forEach { year ->
                DropdownMenuItem(
                    onClick = {
                        currentYear = year
                        mExpanded = false
                    },
                    text = { Text(text = currentYear.toString()) }
                )
            }
        }
    }
}
