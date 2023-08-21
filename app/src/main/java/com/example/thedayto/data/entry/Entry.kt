package com.example.thedayto.data.entry

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity data class represents a single row in the database.
 */
@Entity(tableName = "entries")
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "date") var date: String,
    @ColumnInfo(name = "mood") var mood: String,
    @ColumnInfo(name = "note") var note: String
)