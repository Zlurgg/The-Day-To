package uk.co.zlurgg.thedayto.feature_sign_in.presentation

data class SignInState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)