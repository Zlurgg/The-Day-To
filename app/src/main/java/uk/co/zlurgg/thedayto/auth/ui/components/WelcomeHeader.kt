package uk.co.zlurgg.thedayto.auth.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import uk.co.zlurgg.thedayto.R
import uk.co.zlurgg.thedayto.core.ui.theme.paddingLarge
import uk.co.zlurgg.thedayto.core.ui.theme.paddingMedium

@Composable
fun WelcomeHeader(
    showWelcome: Boolean,
    showAppName: Boolean,
    showSubtitle: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(paddingMedium),
        modifier = modifier.fillMaxWidth()
    ) {
        // Welcome text with fade-in animation
        AnimatedVisibility(
            visible = showWelcome,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = tween(600)
                    )
        ) {
            Text(
                text = stringResource(R.string.welcome),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }

        // App name with slide-in animation
        AnimatedVisibility(
            visible = showAppName,
            enter = fadeIn(animationSpec = tween(600)) +
                    slideInVertically(
                        initialOffsetY = { 40 },
                        animationSpec = tween(600)
                    )
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }

        // Subtitle/tagline
        AnimatedVisibility(
            visible = showSubtitle,
            enter = fadeIn(animationSpec = tween(800))
        ) {
            Text(
                text = "Track your daily mood journey",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = paddingLarge)
            )
        }

        Spacer(modifier = Modifier.height(paddingLarge))
    }
}
