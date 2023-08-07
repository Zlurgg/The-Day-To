package com.example.thedayto.data.calender

data class CalendarInput(
    val day:Int,
    val toDos:List<String> = emptyList()
)