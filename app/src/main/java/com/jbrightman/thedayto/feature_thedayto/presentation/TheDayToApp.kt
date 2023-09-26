package com.jbrightman.thedayto.feature_thedayto.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.add_edit_entry.AddEditEntryScreen
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.EntriesScreen
import com.jbrightman.thedayto.feature_thedayto.presentation.entry.display_entries.EntriesViewModel
import com.jbrightman.thedayto.feature_thedayto.presentation.login.LoginScreen
import com.jbrightman.thedayto.feature_thedayto.presentation.util.Screen
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun TheDayToApp(
    entriesViewModel: EntriesViewModel = hiltViewModel()
) {
    val state = entriesViewModel.state.value
    var startDestination = Screen.AddEditEntryScreen.route

    state.entries.forEach { entry ->
        if (entry.dateStamp == LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)) {
            startDestination = Screen.EntriesScreen.route
        }
    }
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(route = Screen.LoginScreen.route) {
                LoginScreen(navController = navController)
            }
            composable(route = Screen.AddEditEntryScreen.route +
                    "?entryId={entryId}&entryColor={entryColor}&entryDate={entryDate}&showBackButton={showBackButton}",
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
                    },
                    navArgument(
                        name = "entryDate"
                    ) {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument(
                        name = "showBackButton"
                    ) {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) {
                val color = it.arguments?.getInt("entryColor") ?: -1
                val day = it.arguments?.getInt("entryDate") ?: -1
                val backButton = it.arguments?.getBoolean("showBackButton") ?: false
                AddEditEntryScreen(
                    navController = navController,
                    entryColor = color,
                    entryDate = day,
                    showBackButton = backButton
                )
            }
            composable(route = Screen.EntriesScreen.route) {
                EntriesScreen(
                    navController = navController
                )
            }
        }
    }
}