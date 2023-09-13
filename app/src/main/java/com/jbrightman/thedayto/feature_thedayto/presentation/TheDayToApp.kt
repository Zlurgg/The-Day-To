package com.jbrightman.thedayto.feature_thedayto.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jbrightman.thedayto.feature_thedayto.presentation.calendar.CalenderScreen
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.EntryScreen
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Screen

@Composable
fun TheDayToApp(
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Screen.CalenderScreen.route
        ) {
            composable(route = Screen.CalenderScreen.route) {
                CalenderScreen(
                    modifier = Modifier,
                    navController = navController
                )
            }
            composable(route = Screen.EntryScreen.route) {
                EntryScreen(
                    navController = navController,
                    modifier = Modifier
                )
            }
        }
    }
}