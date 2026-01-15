package io.github.zlurgg.update.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO for GitHub Release API response.
 * Uses kotlinx.serialization for JSON deserialization.
 */
@Serializable
data class GitHubReleaseDto(
    @SerialName("tag_name") val tagName: String,
    @SerialName("name") val name: String,
    @SerialName("html_url") val htmlUrl: String,
    @SerialName("body") val body: String? = null,
    @SerialName("assets") val assets: List<AssetDto>
)

/**
 * DTO for release asset (APK file).
 */
@Serializable
data class AssetDto(
    @SerialName("name") val name: String,
    @SerialName("browser_download_url") val downloadUrl: String,
    @SerialName("size") val size: Long
)
