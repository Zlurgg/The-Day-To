package uk.co.zlurgg.thedayto.update.domain.model

/**
 * Domain model representing available update information.
 * Pure Kotlin - no framework dependencies.
 */
data class UpdateInfo(
    val versionName: String,
    val releaseUrl: String,
    val apkDownloadUrl: String?,
    val apkSize: Long?,
    val changelog: String?
)
