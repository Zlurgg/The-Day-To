package uk.co.zlurgg.thedayto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import uk.co.zlurgg.thedayto.core.ui.TheDayToApp
import uk.co.zlurgg.thedayto.core.ui.theme.TheDayToTheme

/**
 * Main Activity for The Day To app.
 *
 * Follows Google's "Now in Android" architecture pattern:
 * - Minimal Activity - only handles theme and navigation
 * - No business logic
 * - No permission requests (handled contextually in feature screens)
 * - No WorkManager setup (done in Application.onCreate())
 *
 * Responsibilities:
 * - Apply app theme
 * - Launch Compose navigation graph
 * - Enable edge-to-edge with theme-aware system bars
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            TheDayToTheme {
                // Sync system bar appearance with the current theme so the
                // status bar and navigation bar icons match dark/light mode.
                SystemBarEffect()
                TheDayToApp()
            }
        }
    }

    /**
     * Keeps the system bar icon style in sync with the Compose theme.
     *
     * Called inside [TheDayToTheme] so [isSystemInDarkTheme] reflects the
     * current mode. Re-calls [enableEdgeToEdge] with the appropriate
     * [SystemBarStyle] when the theme changes.
     */
    @Composable
    private fun SystemBarEffect() {
        val isDark = isSystemInDarkTheme()
        LaunchedEffect(isDark) {
            enableEdgeToEdge(
                statusBarStyle = if (isDark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    )
                },
                navigationBarStyle = if (isDark) {
                    SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                } else {
                    SystemBarStyle.light(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    )
                },
            )
        }
    }
}
