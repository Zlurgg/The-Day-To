package uk.co.zlurgg.thedayto.feature_sign_in.domain.model

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)