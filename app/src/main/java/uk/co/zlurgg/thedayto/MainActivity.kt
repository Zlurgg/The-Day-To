package uk.co.zlurgg.thedayto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import org.koin.android.ext.android.inject
import uk.co.zlurgg.thedayto.core.domain.model.ThemeMode
import uk.co.zlurgg.thedayto.core.domain.usecases.theme.GetThemeModeUseCase
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

    private val getThemeModeUseCase: GetThemeModeUseCase by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            val themeMode = getThemeModeUseCase()
                .collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)

            val useDarkTheme = when (themeMode.value) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            TheDayToTheme(useDarkTheme = useDarkTheme) {
                SystemBarEffect(isDark = useDarkTheme)
                TheDayToApp()
            }
        }
    }

    /**
     * Keeps the system bar icon style in sync with the Compose theme.
     *
     * Re-calls [enableEdgeToEdge] with the appropriate
     * [SystemBarStyle] when the theme changes.
     */
    @Composable
    private fun SystemBarEffect(isDark: Boolean) {
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
