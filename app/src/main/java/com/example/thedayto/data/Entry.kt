package com.example.thedayto.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_entries")
data class Entry(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var date: String = "date string",
    var mood: String = "default mood",
    var note: String = "placeholder note"
)