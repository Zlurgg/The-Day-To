package com.jbrightman.thedayto.feature_login.presentation

data class LoginResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)