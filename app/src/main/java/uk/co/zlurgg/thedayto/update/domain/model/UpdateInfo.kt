package uk.co.zlurgg.thedayto.update.domain.model

import androidx.compose.runtime.Immutable

/**
 * Domain model representing available update information.
 */
@Immutable
data class UpdateInfo(
    val versionName: String,
    val releaseUrl: String,
    val apkDownloadUrl: String?,
    val apkSize: Long?,
    val changelog: String?
)
