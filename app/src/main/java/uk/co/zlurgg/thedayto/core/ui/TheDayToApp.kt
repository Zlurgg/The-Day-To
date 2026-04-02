package uk.co.zlurgg.thedayto.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import uk.co.zlurgg.thedayto.core.ui.navigation.AccountRoute
import uk.co.zlurgg.thedayto.core.ui.navigation.EditorRoute
import uk.co.zlurgg.thedayto.core.ui.navigation.OverviewRoute
import uk.co.zlurgg.thedayto.core.ui.navigation.StatsRoute
import uk.co.zlurgg.thedayto.core.ui.navigation.MoodColorManagementRoute
import uk.co.zlurgg.thedayto.journal.ui.editor.EditorScreenRoot
import uk.co.zlurgg.thedayto.journal.ui.moodcolormanagement.MoodColorManagementScreenRoot
import uk.co.zlurgg.thedayto.journal.ui.overview.OverviewScreenRoot
import uk.co.zlurgg.thedayto.journal.ui.stats.StatsScreenRoot
import uk.co.zlurgg.thedayto.sync.ui.AccountScreenRoot

/**
 * Main navigation graph for The Day To app
 *
 * Following Google's 2025 recommended architecture:
 * - Type-safe navigation with Kotlin Serialization
 * - No business logic (handled in ViewModels)
 * - Pure navigation definition
 *
 * First-time user experience is handled by OverviewScreen's tutorial dialog,
 * which combines welcome message with getting started tips.
 */
@Composable
fun TheDayToApp() {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = OverviewRoute
        ) {
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
                        animationSpec = tween(durationMillis = 600)
                    ) + fadeOut(animationSpec = tween(durationMillis = 600))
                },
                // Slide down when BACK button pressed (popping back stack)
                popExitTransition = {
                    slideOutVertically(
                        targetOffsetY = { fullHeight -> fullHeight },
                        animationSpec = tween(durationMillis = 600)
                    ) + fadeOut(animationSpec = tween(durationMillis = 600))
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
                OverviewScreenRoot(
                    navController = navController
                )
            }

            // Stats Screen
            composable<StatsRoute> {
                StatsScreenRoot(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            // Mood Color Management Screen
            composable<MoodColorManagementRoute> {
                MoodColorManagementScreenRoot(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }

            // Account Screen
            composable<AccountRoute> {
                AccountScreenRoot(
                    onNavigateBack = {
                        navController.navigateUp()
                    }
                )
            }
        }
    }
}
