package com.example.thedayto.presentation.entry

import android.content.ContentValues.TAG
import android.content.Context
import android.util.Log
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
import com.example.thedayto.TheDayToApplication
import com.example.thedayto.data.local.TheDayToEntity
import com.example.thedayto.common.util.DateUtil
import kotlinx.coroutines.launch
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import com.example.thedayto.presentation.entry.destinations.DisplayEntryScreenDestination
import com.example.thedayto.presentation.entry.destinations.EntryScreenDestination
import com.example.thedayto.presentation.entry.destinations.MoodScreenDestination
import com.example.thedayto.presentation.entry.destinations.NoteScreenDestination
import com.example.thedayto.presentation.entry.destinations.SaveScreenDestination
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator

@Destination(start = true)
@Composable
fun EntryScreen(
    navigator: DestinationsNavigator
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
            Row {
                Button(onClick = {
                    navigator.navigate(
                        MoodScreenDestination()
                    )
                }) {
                    Text("Create an entry for today?")
                }
            }
        }
    }
}

@Composable
@Destination
fun MoodScreen(
    navigator: DestinationsNavigator
) {
    val context: Context = LocalContext.current
    val entryViewModel: EntryViewModel = viewModel(
        factory = EntryViewModelFactory((context.applicationContext as TheDayToApplication).repository)
    )
    val entryDetails = entryViewModel.entriesUiState.entryDetails
    val date = DateUtil().getCurrentDate()
    entryDetails.date = date

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            AddMoodCard(
                entryUiState = entryViewModel.entriesUiState,
                onEntryValueChange = entryViewModel::updateUiState,
                entryDetails = entryDetails
            )
        }
        Row {
            Button(onClick = {
                Log.i(TAG, "Mood entryDetails: $entryDetails");
                navigator.navigate(
                    NoteScreenDestination(date)
                )
            }) {
                Text("Go to Note Screen")
            }
        }
    }
}
@Composable
@Destination
fun NoteScreen(
    navigator: DestinationsNavigator,
    date: String
) {
    val context: Context = LocalContext.current
    val entryViewModel: EntryViewModel = viewModel(
        factory = EntryViewModelFactory((context.applicationContext as TheDayToApplication).repository)
    )

    Log.i(TAG, "Note: entriesUiState date: ${entryViewModel.entriesUiState.entryDetails.date}")
    Log.i(TAG, "Note: entryViewModel: ${entryViewModel.entryFromDate(date)}")

    val entryDetails = entryViewModel.entriesUiState.entryDetails
    Log.i(TAG, "Note: entryDetails: ${entryDetails}")

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            AddNoteCard(
                entryUiState = entryViewModel.entriesUiState,
                onEntryValueChange = entryViewModel::updateUiState,
                entryDetails = entryDetails
            )
        }
        Row {
            Button(onClick = {
                Log.i(TAG, "Note: entryDetails: After card: $entryDetails");
                navigator.navigate(
                    SaveScreenDestination(date)
                )
            }) {
                Text("Go to Save Screen")
            }
        }
    }
}

@Composable
@Destination
fun SaveScreen(
    navigator: DestinationsNavigator,
    date: String
) {
    val context: Context = LocalContext.current
    val entryViewModel: EntryViewModel = viewModel(
        factory = EntryViewModelFactory((context.applicationContext as TheDayToApplication).repository)
    )
    entryViewModel.entryFromDate(date)
    Log.i(TAG, "Save: entryViewModel: $entryViewModel")
    val coroutineScope = rememberCoroutineScope()
    Log.i(TAG, "Save: entryDetails: ${entryViewModel.entriesUiState.entryDetails}");

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            SaveCard(
                onSaveClick = {
                    Log.i(TAG, "Save: entryDetails: After card:${entryViewModel.entriesUiState.entryDetails}");
                    coroutineScope.launch {
                        entryViewModel.saveEntry()
                    }
                    navigator.navigate(
                        DisplayEntryScreenDestination()
                    )
                }
            )
        }
    }
}

@Composable
@Destination
fun DisplayEntryScreen(
    navigator: DestinationsNavigator,
) {
    val context: Context = LocalContext.current
    val entryViewModel: EntryViewModel = viewModel(
        factory = EntryViewModelFactory((context.applicationContext as TheDayToApplication).repository)
    )
    val entries: List<TheDayToEntity> by entryViewModel.allEntries.observeAsState(listOf())

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
                    DisplayEntriesCard(theDayToEntity = it)
                }
            }
        }
        Button(
            onClick = {
                Log.i(TAG, "Display entry: ${entryViewModel.entriesUiState.entryDetails}");

                // Navigates back to 1st screen
                navigator.navigate(EntryScreenDestination())
            }
        ) {
            Text(text = "Navigate back")
        }
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
fun DisplayEntriesCard(theDayToEntity: TheDayToEntity) {
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
                text = theDayToEntity.id.toString() + " | ",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                ),
            )
            Text(
                text = theDayToEntity.date + " | ",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                )
            )
            Text(
                text = theDayToEntity.mood + " | ",
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                )
            )
            Text(
                text = theDayToEntity.note,
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 15.sp
                )
            )
        }
    }
}


