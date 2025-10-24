package uk.co.zlurgg.thedayto.auth.ui.state

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)