package com.jbrightman.thedayto.feature_thedayto.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jbrightman.thedayto.feature_thedayto.presentation.calendar.CalenderScreen
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryScreen
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.EntriesScreen
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
            startDestination = Screen.EntriesScreen.route
        ) {
            composable(route = Screen.EntriesScreen.route) {
                EntriesScreen(
                    navController = navController
                )
            }
            composable(route = Screen.AddEditEntryScreen.route +
                    "?entryId={entryId}&entryColor={entryColor}",
                arguments = listOf(
                    navArgument(
                        name = "entryId"
                    ) {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument(
                        name = "entryColor"
                    ) {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) {
                val color = it.arguments?.getInt("entryColor") ?: -1
                AddEditEntryScreen(
                    navController = navController,
                    entryColor = color
                )
            }
            composable(route = Screen.CalenderScreen.route) {
                CalenderScreen(
                    modifier = Modifier,
                    navController = navController
                )
            }

        }
    }
}