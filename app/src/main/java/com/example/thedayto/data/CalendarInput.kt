package com.example.thedayto.data

data class CalendarInput(
    val day:Int,
    val toDos:List<String> = emptyList()
)