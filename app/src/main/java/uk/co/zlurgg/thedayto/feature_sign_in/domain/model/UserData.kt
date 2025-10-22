package uk.co.zlurgg.thedayto.feature_sign_in.domain.model

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)