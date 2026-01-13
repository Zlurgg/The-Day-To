package uk.co.zlurgg.thedayto.update.data.repository

import io.ktor.util.network.UnresolvedAddressException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.update.data.mapper.toDomain
import uk.co.zlurgg.thedayto.update.data.remote.api.GitHubApiService
import uk.co.zlurgg.thedayto.update.data.service.ApkDownloadService
import uk.co.zlurgg.thedayto.update.domain.model.UpdateConfig
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo
import uk.co.zlurgg.thedayto.update.domain.repository.UpdateRepository
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * Implementation of UpdateRepository.
 * Fetches release info from GitHub and handles APK downloads.
 */
class UpdateRepositoryImpl(
    private val gitHubApiService: GitHubApiService,
    private val apkDownloadService: ApkDownloadService,
    private val config: UpdateConfig
) : UpdateRepository {

    override suspend fun getLatestRelease(): Result<UpdateInfo, DataError.Remote> =
        withContext(Dispatchers.IO) {
            try {
                Timber.d("Fetching latest release from GitHub")
                val response = gitHubApiService.getLatestRelease(
                    owner = config.gitHubOwner,
                    repo = config.gitHubRepo
                )
                val updateInfo = response.toDomain()
                Timber.i("Latest release: ${updateInfo.versionName}")
                Result.Success(updateInfo)
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch latest release")
                Result.Error(mapNetworkException(e))
            }
        }

    override suspend fun getReleaseByVersion(version: String): Result<UpdateInfo, DataError.Remote> =
        withContext(Dispatchers.IO) {
            try {
                // GitHub tags typically use "v" prefix (e.g., "v1.0.5")
                val tag = if (version.startsWith("v")) version else "v$version"
                Timber.d("Fetching release for version $tag from GitHub")
                val response = gitHubApiService.getReleaseByTag(
                    owner = config.gitHubOwner,
                    repo = config.gitHubRepo,
                    tag = tag
                )
                val updateInfo = response.toDomain()
                Timber.i("Release info for $tag: ${updateInfo.versionName}")
                Result.Success(updateInfo)
            } catch (e: Exception) {
                Timber.e(e, "Failed to fetch release for version $version")
                Result.Error(mapNetworkException(e))
            }
        }

    override fun downloadApk(url: String, fileName: String): Long {
        return apkDownloadService.downloadApk(url, fileName)
    }

    override fun installApk(downloadId: Long) {
        apkDownloadService.installApk(downloadId)
    }

    private fun mapNetworkException(e: Exception): DataError.Remote {
        return when (e) {
            is UnresolvedAddressException -> DataError.Remote.NO_INTERNET
            is UnknownHostException -> DataError.Remote.NO_INTERNET
            is SocketTimeoutException -> DataError.Remote.REQUEST_TIMEOUT
            else -> DataError.Remote.UNKNOWN
        }
    }
}
