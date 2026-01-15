package io.github.zlurgg.update.data.remote.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.github.zlurgg.update.data.remote.dto.GitHubReleaseDto

/**
 * Ktor-based client for GitHub Releases API.
 * Repository owner and name are passed as parameters to each method.
 */
class GitHubApiService(
    private val httpClient: HttpClient,
    private val baseUrl: String = "https://api.github.com"
) {

    suspend fun getLatestRelease(owner: String, repo: String): GitHubReleaseDto {
        return httpClient.get("$baseUrl/repos/$owner/$repo/releases/latest").body()
    }

    suspend fun getReleaseByTag(owner: String, repo: String, tag: String): GitHubReleaseDto {
        return httpClient.get("$baseUrl/repos/$owner/$repo/releases/tags/$tag").body()
    }
}
