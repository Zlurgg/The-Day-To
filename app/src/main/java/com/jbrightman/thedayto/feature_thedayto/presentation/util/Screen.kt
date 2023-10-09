package com.jbrightman.thedayto.feature_thedayto.presentation.util

sealed class Screen(
    val route: String
) {
    data object EntriesScreen: Screen("entries_screen")
    data object AddEditEntryScreen: Screen("add_edit_entry_screen")
    data object LoginScreen: Screen("login_screen")
    data object AddEditMoodColorScreen: Screen("add_edit_mood_color_screen")
}