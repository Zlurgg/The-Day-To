package uk.co.zlurgg.thedayto.auth.ui.state

import androidx.compose.runtime.Stable

@Stable
data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null,
    val showWelcomeDialog: Boolean = false,
    val navigationTarget: SignInNavigationTarget? = null,
    val isDevSignInAvailable: Boolean = false
)

sealed interface SignInNavigationTarget {
    data object ToOverview : SignInNavigationTarget
}