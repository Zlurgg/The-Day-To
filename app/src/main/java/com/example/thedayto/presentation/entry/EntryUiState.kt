package com.example.thedayto.presentation.entry

import com.example.thedayto.data.local.TheDayToEntity

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

fun EntryDetails.toEntry(): TheDayToEntity = TheDayToEntity(
    id = id,
    date = date,
    mood = mood,
    note = note
)
