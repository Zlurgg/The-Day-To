package uk.co.zlurgg.thedayto.update.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import uk.co.zlurgg.thedayto.update.data.remote.dto.GitHubReleaseDto

/**
 * Retrofit interface for GitHub Releases API.
 */
interface GitHubApiService {

    @GET("repos/{owner}/{repo}/releases/latest")
    suspend fun getLatestRelease(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): GitHubReleaseDto

    @GET("repos/{owner}/{repo}/releases/tags/{tag}")
    suspend fun getReleaseByTag(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("tag") tag: String
    ): GitHubReleaseDto

    companion object {
        const val GITHUB_OWNER = "Zlurgg"
        const val GITHUB_REPO = "The-Day-To"
    }
}
