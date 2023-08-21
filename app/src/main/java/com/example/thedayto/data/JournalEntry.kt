package com.example.thedayto.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "entries_table")
@Parcelize
class JournalEntry(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "mood") val mood: String,
    @ColumnInfo(name = "note") val note: String,
    @ColumnInfo(name = "date") val date: String
) : Parcelable
