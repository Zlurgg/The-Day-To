package com.jbrightman.thedayto.feature_thedayto.presentation.util

sealed class Screen(
    val route: String
) {
    data object EntryScreen: Screen("entry_screen")
    data object CalenderScreen: Screen("calender_screen")
}