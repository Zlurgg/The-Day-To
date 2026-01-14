package uk.co.zlurgg.thedayto.auth.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * No-op DevSignInButton for release builds.
 * Renders nothing - dev sign-in is not available in production.
 */
@Composable
fun DevSignInButton(
    onClick: () -> Unit,
    showButton: Boolean,
    modifier: Modifier = Modifier
) {
    // Empty - no dev sign-in in release builds
}
