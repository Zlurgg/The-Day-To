package com.example.thedayto.calender

data class CalendarInput(
    val day:Int,
    val toDos:List<String> = emptyList()
)