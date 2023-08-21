package com.example.thedayto.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.thedayto.data.EntryApplication
import com.example.thedayto.data.JournalEntry
import com.example.thedayto.ui.EntryDetails
import com.example.thedayto.ui.EntryUiState
import com.example.thedayto.util.DateUtil
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import com.example.thedayto.ui.screens.destinations.NoteScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination(start = true)
@Composable
fun EntryScreen(
    navigator: DestinationsNavigator,

) {
    val context: Context = LocalContext.current
    val entryViewModel: EntryViewModel = viewModel(
    factory = EntryViewModelFactory((context.applicationContext as EntryApplication).repository)
    )
    /** fetch all entries via the view-model **/
    val entries: List<JournalEntry> by entryViewModel.allEntries.observeAsState(listOf())
    val coroutineScope = rememberCoroutineScope()

    /** create an instance of entry details **/
    val entryDetails = entryViewModel.entriesUiState.entryDetails

    /** set current date for entry **/
    val date = DateUtil().getCurrentDate()
    entryDetails.date = date

    Column(verticalArrangement = Arrangement.SpaceBetween) {
        /** update entry with a mood **/
        Row {
            AddMoodCard(
                entryUiState = entryViewModel.entriesUiState,
                onEntryValueChange = entryViewModel::updateUiState,
                entryDetails = entryDetails
            )
        }
        Row {
            Button(onClick = {
                navigator.navigate(
                    NoteScreenDestination(
                        JournalEntry(
                            id = 99,
                            mood = "example_mood",
                            note = "example_note",
                            date = DateUtil().getCurrentDate()
                        )
                    )
                )
            }) {
                Text("Go to Note Screen")
            }
        }
        /** update entry with a note **/
       /* Row {
            AddNoteCard(
                entryUiState = entryViewModel.entriesUiState,
                onEntryValueChange = entryViewModel::updateUiState,
                entryDetails = entryDetails
            )
        }

        *//** save the entry **//*
        Row {
            SaveCard(
                onSaveClick = {
                    coroutineScope.launch {
                        entryViewModel.saveEntry()
                    }
                }
            )
        }

        *//** display all database entries **//*
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            text = "Current database content:",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        Row(modifier = Modifier.padding(24.dp)) {
            LazyColumn(contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
                items(
                    entries
                ) {
                    DisplayEntriesCard(journalEntry = it)
                }
            }
        }*/
    }
}

@Destination
@Composable
fun NoteScreen(
    navigator: DestinationsNavigator,
    journalEntry: JournalEntry
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Profile Screen: ${journalEntry.mood}", textAlign = TextAlign.Center)
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMoodCard(
    entryUiState: EntryUiState,
    onEntryValueChange: (EntryDetails) -> Unit,
    entryDetails: EntryDetails
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "Today's Mood",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = entryUiState.entryDetails.mood,
                    onValueChange = { onEntryValueChange(entryDetails.copy(mood = it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteCard(
    entryUiState: EntryUiState,
    onEntryValueChange: (EntryDetails) -> Unit,
    enabled: Boolean = true,
    entryDetails: EntryDetails
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "Extra note about how you are feeling today?",
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                TextField(
                    value = entryUiState.entryDetails.note,
                    onValueChange = { onEntryValueChange(entryDetails.copy(note = it)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = enabled,
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun SaveCard(
    onSaveClick: () -> Unit,
    enabled: Boolean = true,
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Button(
                onClick = onSaveClick,
                enabled = enabled,
                shape = MaterialTheme.shapes.small,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Save")
            }
        }
    }
}


@Composable
fun DisplayEntriesCard(journalEntry: JournalEntry) {
    Card(
        modifier = Modifier
            .padding(4.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 10.dp
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
        ) {
            Text(
                text = journalEntry.id.toString() + " | ",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                ),
            )
            Text(
                text = journalEntry.date + " | ",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                )
            )
            Text(
                text = journalEntry.mood + " | ",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                )
            )
            Text(
                text = journalEntry.note,
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                )
            )
        }
    }
}


