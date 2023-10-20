package com.jbrightman.thedayto.feature_login.presentation

data class LoginState(
    val isSignInSuccessful: Boolean = false,
    val signInError: String? = null
)