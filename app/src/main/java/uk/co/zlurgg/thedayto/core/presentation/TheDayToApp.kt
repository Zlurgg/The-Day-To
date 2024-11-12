package uk.co.zlurgg.thedayto.core.presentation

import android.app.Activity.RESULT_OK
import android.widget.Toast
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
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import uk.co.zlurgg.thedayto.core.domain.repository.TheDayToPrefRepository
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryScreen
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.EntriesScreen
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.AddEditMoodColorScreen
import uk.co.zlurgg.thedayto.feature_sign_in.presentation.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.feature_sign_in.presentation.SignInScreen
import uk.co.zlurgg.thedayto.feature_sign_in.presentation.SignInViewModel
import uk.co.zlurgg.thedayto.core.presentation.util.Screen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.defaultExtras
import org.koin.androidx.compose.getViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.rememberCurrentKoinScope
import uk.co.zlurgg.thedayto.R
import java.time.LocalDate
import java.time.ZoneOffset

@Composable
fun TheDayToApp(
    googleAuthUiClient: GoogleAuthUiClient,
    ) {
    /** Check entries for today and see if there is already one, go to entries screen from sign in if so **/
    val startDestination = Screen.SignInScreen.route
    val applicationContext = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    /** shared preferences (entry made today, first time user **/
    val theDayToPrefRepository = TheDayToPrefRepository(applicationContext)

    /** uri for direction of page from notifications **/
    val uri = stringResource(R.string.uri)

    /** context so we can get string resource **/
    val context = LocalContext.current

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
            composable(
                route = Screen.SignInScreen.route,
                deepLinks = listOf(navDeepLink { uriPattern = uri })
            ) {
                checkNotNull(LocalViewModelStoreOwner.current) {
                    "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
                }
                rememberCurrentKoinScope()
                val signInViewModel: SignInViewModel = koinViewModel()
                val state by signInViewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = Unit) {
                    if(googleAuthUiClient.getSignedInUser() != null) {
                        if (theDayToPrefRepository.getDailyEntryDate() ==
                            LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)) {
                            navController.navigate(Screen.EntriesScreen.route)
                        } else {
                            navController.navigate(Screen.AddEditEntryScreen.route)
                        }
                    }
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartIntentSenderForResult(),
                    onResult = { result ->
                        if(result.resultCode == RESULT_OK) {
                            coroutineScope.launch {
                                val signInResult = googleAuthUiClient.signInWithIntent(
                                    intent = result.data ?: return@launch
                                )
                                signInViewModel.onSignInResult(signInResult)
                            }
                        }
                    }
                )

                LaunchedEffect(key1 = state.isSignInSuccessful) {
                    if(state.isSignInSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            context.resources.getString(R.string.sign_in_successful),
                            Toast.LENGTH_LONG
                        ).show()
                        if (theDayToPrefRepository.getDailyEntryDate() ==
                            LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)) {
                            navController.navigate(Screen.EntriesScreen.route)
                        } else {
                            navController.navigate(Screen.AddEditEntryScreen.route)
                        }
                        signInViewModel.resetState()
                    }
                }

                SignInScreen(
                    state = state,
                    onSignInClick = {
                        coroutineScope.launch {
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
            composable(route = "${Screen.AddEditEntryScreen.route}?entryId={entryId}&&entryDate={entryDate}&showBackButton={showBackButton}",
                arguments = listOf(
                    navArgument(
                        name = context.resources.getString(R.string.entryid)
                    ) {
                        type = NavType.IntType
                        defaultValue = -1
                    },
                    navArgument(
                        name = context.resources.getString(R.string.entrydate)
                    ) {
                        type = NavType.LongType
                        defaultValue = -1L
                    },
                    navArgument(
                        name = context.resources.getString(R.string.showbackbutton)
                    ) {
                        type = NavType.BoolType
                        defaultValue = false
                    }
                )
            ) {
                val date = it.arguments?.getLong(stringResource(R.string.entrydate)) ?: -1L
                val backButton = it.arguments?.getBoolean(stringResource(R.string.showbackbutton)) ?: false
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
                        coroutineScope.launch {
                            googleAuthUiClient.signOut()
                            Toast.makeText(
                                applicationContext,
                                context.resources.getString(R.string.signed_out),
                                Toast.LENGTH_LONG
                            ).show()
                            navController.navigate(route = startDestination)
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