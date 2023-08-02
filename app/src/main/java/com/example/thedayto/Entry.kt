package com.example.thedayto

data class Entry(
    var id: Long = -1,
    var date: String = "",
    var mood: String = "",
    var note: String = "Hi I'm a placeholder note"
)