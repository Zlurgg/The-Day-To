package uk.co.zlurgg.thedayto.auth.domain.model

data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)