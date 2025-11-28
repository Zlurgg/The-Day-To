package uk.co.zlurgg.thedayto.update.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO for GitHub Release API response.
 * Uses Gson annotations for JSON deserialization.
 */
data class GitHubReleaseDto(
    @SerializedName("tag_name") val tagName: String,
    @SerializedName("name") val name: String,
    @SerializedName("html_url") val htmlUrl: String,
    @SerializedName("body") val body: String?,
    @SerializedName("assets") val assets: List<AssetDto>
)

/**
 * DTO for release asset (APK file).
 */
data class AssetDto(
    @SerializedName("name") val name: String,
    @SerializedName("browser_download_url") val downloadUrl: String,
    @SerializedName("size") val size: Long
)
