package uk.co.zlurgg.thedayto.core.ui

import android.widget.Toast
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.currentKoinScope
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.data.repository.TheDayToPrefRepository
import uk.co.zlurgg.thedayto.journal.ui.editor.EditorScreenRoot
import uk.co.zlurgg.thedayto.journal.ui.overview.OverviewScreenRoot
import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.auth.ui.SignInScreen
import uk.co.zlurgg.thedayto.auth.ui.SignInViewModel
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
                currentKoinScope()
                val signInViewModel: SignInViewModel = koinViewModel()
                val state by signInViewModel.state.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = Unit) {
                    if (googleAuthUiClient.getSignedInUser() != null) {
                        if (theDayToPrefRepository.getEntryDate() ==
                            LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                        ) {
                            navController.navigate(Screen.OverviewScreen.route)
                        } else {
                            navController.navigate(Screen.EditorScreen.route)
                        }
                    }
                }

                LaunchedEffect(key1 = state.isSignInSuccessful) {
                    if (state.isSignInSuccessful) {
                        Toast.makeText(
                            applicationContext,
                            context.resources.getString(R.string.sign_in_successful),
                            Toast.LENGTH_LONG
                        ).show()
                        if (theDayToPrefRepository.getEntryDate() ==
                            LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                        ) {
                            navController.navigate(Screen.OverviewScreen.route)
                        } else {
                            navController.navigate(Screen.EditorScreen.route)
                        }
                        signInViewModel.resetState()
                    }
                }

                SignInScreen(
                    state = state,
                    onSignInClick = {
                        coroutineScope.launch {
                            val signInResult = googleAuthUiClient.signIn(context)
                            signInViewModel.onSignInResult(signInResult)
                        }
                    }
                )
            }
            composable(route = "${Screen.EditorScreen.route}?entryId={entryId}&&entryDate={entryDate}&showBackButton={showBackButton}",
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
                val backButton =
                    it.arguments?.getBoolean(stringResource(R.string.showbackbutton)) ?: false
                EditorScreenRoot(
                    navController = navController,
                    entryDate = date,
                    showBackButton = backButton
                )
            }
            composable(route = Screen.OverviewScreen.route) {
                OverviewScreenRoot(
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
        }
    }
}