package uk.co.zlurgg.thedayto.auth.domain.model

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)