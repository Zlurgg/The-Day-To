package com.example.thedayto.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteScreen(
    onSubmitNoteButtonClicked: (String) -> Unit,
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
            label = { Text("Extra Note:")}
        )

        Spacer(modifier = Modifier.height(15.dp))

        Button(
            onClick = { onSubmitNoteButtonClicked(text) },
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "Submit",
                textAlign = TextAlign.Center)
        }
    }
}
