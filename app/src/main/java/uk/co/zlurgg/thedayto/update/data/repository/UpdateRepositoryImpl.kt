package uk.co.zlurgg.thedayto.update.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import uk.co.zlurgg.thedayto.update.data.mapper.toDomain
import uk.co.zlurgg.thedayto.update.data.remote.api.GitHubApiService
import uk.co.zlurgg.thedayto.update.data.service.ApkDownloadService
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo
import uk.co.zlurgg.thedayto.update.domain.repository.UpdateRepository

/**
 * Implementation of UpdateRepository.
 * Fetches release info from GitHub and handles APK downloads.
 */
class UpdateRepositoryImpl(
    private val gitHubApiService: GitHubApiService,
    private val apkDownloadService: ApkDownloadService
) : UpdateRepository {

    override suspend fun getLatestRelease(): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            Timber.d("Fetching latest release from GitHub")
            val response = gitHubApiService.getLatestRelease(
                owner = GitHubApiService.GITHUB_OWNER,
                repo = GitHubApiService.GITHUB_REPO
            )
            val updateInfo = response.toDomain()
            Timber.i("Latest release: ${updateInfo.versionName}")
            Result.success(updateInfo)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch latest release")
            Result.failure(e)
        }
    }

    override suspend fun getReleaseByVersion(version: String): Result<UpdateInfo> = withContext(Dispatchers.IO) {
        try {
            // GitHub tags typically use "v" prefix (e.g., "v1.0.5")
            val tag = if (version.startsWith("v")) version else "v$version"
            Timber.d("Fetching release for version $tag from GitHub")
            val response = gitHubApiService.getReleaseByTag(
                owner = GitHubApiService.GITHUB_OWNER,
                repo = GitHubApiService.GITHUB_REPO,
                tag = tag
            )
            val updateInfo = response.toDomain()
            Timber.i("Release info for $tag: ${updateInfo.versionName}")
            Result.success(updateInfo)
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch release for version $version")
            Result.failure(e)
        }
    }

    override fun downloadApk(url: String, fileName: String): Long {
        return apkDownloadService.downloadApk(url, fileName)
    }

    override fun installApk(downloadId: Long) {
        apkDownloadService.installApk(downloadId)
    }
}
