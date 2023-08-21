package com.example.thedayto.ui

import com.example.thedayto.data.JournalEntry

data class EntryUiState(
    val entryDetails: EntryDetails = EntryDetails(),
    val isEntryValid: Boolean = false
)

data class EntryDetails(
    var id: Int = 0,
    var date: String = "",
    var mood: String = "",
    var note: String = ""
)

fun EntryDetails.toEntry(): JournalEntry = JournalEntry(
    id = id,
    date = date,
    mood = mood,
    note = note
)
