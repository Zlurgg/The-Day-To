package com.example.thedayto.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.util.Currency
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    onSubmitNoteButtonClicked: (String) -> Unit,
    entryUiState: EntryUiState,
    onEntryValueChange: (EntryDetails) -> Unit,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {

        /** text field to input daily note **/
        var text by remember { mutableStateOf("") }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { text = it },
            label = { Text("Extra Note:") }
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(
            onClick = { onSubmitNoteButtonClicked(text) },
//            enabled = entryUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Submit",
                textAlign = TextAlign.Center
            )
        }
        Spacer(modifier = Modifier.height(15.dp))

        ItemInputForm(
            entryDetails = entryUiState.entryDetails,
            onValueChange = onEntryValueChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(15.dp))
        Button(
//            onClick = { onSubmitNoteButtonClicked(text) },
            onClick = { onSaveClick() },
//            enabled = entryUiState.isEntryValid,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Submit",
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemInputForm(
    entryDetails: EntryDetails,
    modifier: Modifier = Modifier,
    onValueChange: (EntryDetails) -> Unit = {},
    enabled: Boolean = true
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy( 16.dp)
    ) {
        entryDetails.note?.let {
            OutlinedTextField(
                value = it,
                onValueChange = { onValueChange(entryDetails.copy(note = it)) },
                label = { Text("Extra Note:") },
                modifier = Modifier.fillMaxWidth(),
    //            enabled = enabled,
                singleLine = true
            )
        }
    }
}