package com.jbrightman.thedayto.feature_thedayto.presentation.util

sealed class Screen(
    val route: String
) {
    data object MoodScreen: Screen("mood_screen")
}