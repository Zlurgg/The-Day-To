package uk.co.zlurgg.thedayto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Launch Compose UI
        setContent {
            TheDayToTheme {
                TheDayToApp()
            }
        }
    }
}

