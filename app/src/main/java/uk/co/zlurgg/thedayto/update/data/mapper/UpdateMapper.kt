package uk.co.zlurgg.thedayto.update.data.mapper

import uk.co.zlurgg.thedayto.update.data.remote.dto.GitHubReleaseDto
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo

private const val APK_EXTENSION = ".apk"

/**
 * Extension function to map GitHub Release DTO to domain model.
 */
fun GitHubReleaseDto.toDomain(): UpdateInfo {
    val apkAsset = assets.find { it.name.endsWith(APK_EXTENSION) }

    return UpdateInfo(
        versionName = tagName.removePrefix("v"),
        releaseUrl = htmlUrl,
        apkDownloadUrl = apkAsset?.downloadUrl,
        apkSize = apkAsset?.size,
        changelog = body
    )
}
