package com.jbrightman.thedayto.presentation

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.navArgument
import com.jbrightman.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryScreen
import com.jbrightman.thedayto.feature_daily_entry.presentation.display_daily_entries.EntriesScreen
import com.jbrightman.thedayto.feature_daily_entry.presentation.display_daily_entries.EntriesViewModel
import com.jbrightman.thedayto.feature_login.presentation.GoogleAuthUiClient
import com.jbrightman.thedayto.feature_login.presentation.LoginScreen
import com.jbrightman.thedayto.feature_mood_color.presentation.AddEditMoodColorScreen
import com.jbrightman.thedayto.feature_login.presentation.LoginViewModel
import com.jbrightman.thedayto.presentation.util.Screen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun TheDayToApp(
    googleAuthUiClient: GoogleAuthUiClient,
    entriesViewModel: EntriesViewModel = hiltViewModel()
) {

    var startDestination = Screen.LoginScreen.route
    val applicationContext = LocalContext.current
    val scope = rememberCoroutineScope()

    /** Check entries for today and see if there is already one, go to entries screen from login if so **/
    var alreadyMadeAnEntryToday = false

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
                val viewModel = viewModel<LoginViewModel>()
                val state by viewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = Unit) {
                    entriesViewModel.state.value.entries.forEach { entry ->
                        if (entry.dateStamp == LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)) {
                            alreadyMadeAnEntryToday = true
                        }
                    }
                    if(googleAuthUiClient.getSignedInUser() != null) {
                        if (alreadyMadeAnEntryToday) {
                            navController.navigate(Screen.EntriesScreen.route)
                        } else {
                            navController.navigate(Screen.AddEditEntryScreen.route)
                        }
                    }
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = { result ->
                        if(result.resultCode == ComponentActivity.RESULT_OK) {
                            scope.launch {
                                val signInResult = googleAuthUiClient.signInWithIntent(
                                    intent = result.data ?: return@launch
                                )
                                viewModel.onSignInResult(signInResult)
                            }
                        }
                    }
                )

                LaunchedEffect(key1 = state.isSignInSuccessful) {
                    if(state.isSignInSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            "Sign in successful",
                            Toast.LENGTH_LONG
                        ).show()

                        if (alreadyMadeAnEntryToday) {
                            navController.navigate(Screen.EntriesScreen.route)
                        } else {
                            navController.navigate(Screen.AddEditEntryScreen.route)
                        }
                        viewModel.resetState()
                    }
                }

                LoginScreen(
                    state = state,
                    onSignInClick = {
                        scope.launch {
                            val signInIntentSender = googleAuthUiClient.signIn()
                            launcher.launch(
                                IntentSenderRequest.Builder(
                                    signInIntentSender ?: return@launch
                                ).build()
                            )
                        }
                    }
                )
            }
            composable(route = Screen.AddEditEntryScreen.route +
                    "?entryId={entryId}&&entryDate={entryDate}&showBackButton={showBackButton}",
                arguments = listOf(
                    navArgument(
                        name = "entryId"
                    ) {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument(
                        name = "entryDate"
                    ) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(
                        name = "showBackButton"
                    ) {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) {
                val date = it.arguments?.getLong("entryDate") ?: -1L
                val backButton = it.arguments?.getBoolean("showBackButton") ?: false
                AddEditEntryScreen(
                    navController = navController,
                    entryDate = date,
                    showBackButton = backButton
                )
            }
            composable(route = Screen.EntriesScreen.route) {
                EntriesScreen(
                    navController = navController,
                    onSignOut = {
                        scope.launch {
                            googleAuthUiClient.signOut()
                            Toast.makeText(
                                applicationContext,
                                "Signed out",
                                Toast.LENGTH_LONG
                            ).show()
                            navController.popBackStack()
                        }
                    }
                )
            }
            composable(route = Screen.AddEditMoodColorScreen.route) {
                AddEditMoodColorScreen()
            }
        }
    }
}