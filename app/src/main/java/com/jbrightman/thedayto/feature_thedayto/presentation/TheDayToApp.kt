package com.jbrightman.thedayto.feature_thedayto.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jbrightman.thedayto.feature_thedayto.presentation.mood.MoodScreen
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Screen

@Composable
fun TheDayToApp() {
    Surface(
        color = MaterialTheme.colorScheme.background
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Screen.MoodScreen.route
        ) {
            composable(route = Screen.MoodScreen.route) {
                MoodScreen(navController = navController)
            }
        }
    }
}