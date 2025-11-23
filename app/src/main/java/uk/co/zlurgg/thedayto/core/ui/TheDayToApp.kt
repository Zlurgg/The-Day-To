package uk.co.zlurgg.thedayto.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.coroutines.launch
import org.koin.compose.koinInject
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignOutUseCase
import uk.co.zlurgg.thedayto.auth.ui.SignInScreenRoot
import uk.co.zlurgg.thedayto.auth.ui.components.SignOutDialog
import uk.co.zlurgg.thedayto.core.ui.navigation.EditorRoute
import uk.co.zlurgg.thedayto.core.ui.navigation.OverviewRoute
import uk.co.zlurgg.thedayto.core.ui.navigation.SignInRoute
import uk.co.zlurgg.thedayto.core.ui.navigation.StatsRoute
import uk.co.zlurgg.thedayto.journal.ui.editor.EditorScreenRoot
import uk.co.zlurgg.thedayto.journal.ui.overview.OverviewScreenRoot
import uk.co.zlurgg.thedayto.journal.ui.stats.StatsScreenRoot

/**
 * Main navigation graph for The Day To app
 *
 * Following Google's 2025 recommended architecture:
 * - Type-safe navigation with Kotlin Serialization
 * - No business logic (handled in ViewModels)
 * - Pure navigation definition
 */
@Composable
fun TheDayToApp() {
    /** uri for direction of page from notifications **/
    val uri = stringResource(R.string.uri)

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = SignInRoute
        ) {
            // Sign In Screen
            composable<SignInRoute>(
                deepLinks = listOf(navDeepLink<SignInRoute>(basePath = uri))
            ) {
                SignInScreenRoot(
                    onNavigateToOverview = {
                        navController.navigate(OverviewRoute) {
                            // Clear back stack so back button doesn't return to sign-in
                            popUpTo<SignInRoute> { inclusive = true }
                        }
                    }
                )
            }

            // Editor Screen (Add/Edit Entry)
            composable<EditorRoute>(
                deepLinks = listOf(
                    navDeepLink<EditorRoute>(
                        basePath = "https://thedayto.co.uk/editor"
                    )
                ),
                // Slide up when navigating TO editor
                enterTransition = {
                    slideInVertically(
                        initialOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeIn(animationSpec = tween(durationMillis = 300))
                },
                // Slide down when navigating AWAY from editor (forward navigation)
                exitTransition = {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut(animationSpec = tween(durationMillis = 300))
                },
                // Slide down when BACK button pressed (popping back stack)
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(durationMillis = 300)
                    ) + fadeOut(animationSpec = tween(durationMillis = 300))
                }
            ) { backStackEntry ->
                val editorRoute = backStackEntry.toRoute<EditorRoute>()
                EditorScreenRoot(
                    navController = navController,
                    showBackButton = editorRoute.showBackButton
                )
            }

            // Overview Screen (Calendar + Entry List)
            composable<OverviewRoute>() {
                val signOutUseCase: SignOutUseCase = koinInject()
                val scope = rememberCoroutineScope()
                var showSignOutDialog by remember { mutableStateOf(false) }

                OverviewScreenRoot(
                    navController = navController,
                    onNavigateToSignIn = {
                        navController.navigate(SignInRoute) {
                            // Clear entire back stack on sign-out
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onShowSignOutDialog = {
                        showSignOutDialog = true
                    }
                )

                // Sign Out Dialog (stateless)
                if (showSignOutDialog) {
                    SignOutDialog(
                        onDismiss = { },
                        onConfirm = {
                            scope.launch {
                                signOutUseCase()
                                navController.navigate(SignInRoute) {
                                    // Clear entire back stack on sign-out
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                    )
                }
            }

            // Stats Screen
            composable<StatsRoute> {
                StatsScreenRoot(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}