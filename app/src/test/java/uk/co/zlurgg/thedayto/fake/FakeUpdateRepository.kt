package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo
import uk.co.zlurgg.thedayto.update.domain.repository.UpdateRepository

/**
 * Fake implementation of UpdateRepository for testing.
 * Allows control over returned values and error states.
 */
class FakeUpdateRepository : UpdateRepository {

    private var latestReleaseResult: Result<UpdateInfo, DataError.Remote>? = null
    private var releaseByVersionResults = mutableMapOf<String, Result<UpdateInfo, DataError.Remote>>()
    private var downloadedApks = mutableListOf<Pair<String, String>>()
    private var installedDownloadIds = mutableListOf<Long>()
    private var nextDownloadId = 1L

    /**
     * Sets the result to return from getLatestRelease().
     */
    fun setLatestReleaseResult(result: Result<UpdateInfo, DataError.Remote>) {
        latestReleaseResult = result
    }

    /**
     * Helper to set a successful release response.
     */
    fun setLatestRelease(updateInfo: UpdateInfo) {
        latestReleaseResult = Result.Success(updateInfo)
    }

    /**
     * Sets the result to return from getReleaseByVersion() for a specific version.
     */
    fun setReleaseByVersion(version: String, result: Result<UpdateInfo, DataError.Remote>) {
        releaseByVersionResults[version] = result
    }

    /**
     * Helper to set a successful release response for a specific version.
     */
    fun setReleaseInfo(version: String, updateInfo: UpdateInfo) {
        releaseByVersionResults[version] = Result.Success(updateInfo)
    }

    /**
     * Helper to simulate network error.
     */
    fun setNetworkError(error: DataError.Remote = DataError.Remote.NO_INTERNET) {
        latestReleaseResult = Result.Error(error)
    }

    override suspend fun getLatestRelease(): Result<UpdateInfo, DataError.Remote> {
        return latestReleaseResult ?: Result.Error(DataError.Remote.UNKNOWN)
    }

    override suspend fun getReleaseByVersion(version: String): Result<UpdateInfo, DataError.Remote> {
        // Check both with and without "v" prefix
        val normalizedVersion = version.removePrefix("v")
        return releaseByVersionResults[version]
            ?: releaseByVersionResults["v$normalizedVersion"]
            ?: releaseByVersionResults[normalizedVersion]
            ?: Result.Error(DataError.Remote.NOT_FOUND)
    }

    override fun downloadApk(url: String, fileName: String): Long {
        downloadedApks.add(url to fileName)
        return nextDownloadId++
    }

    override fun installApk(downloadId: Long) {
        installedDownloadIds.add(downloadId)
    }

    /**
     * Returns list of downloaded APKs (url, fileName) pairs.
     */
    fun getDownloadedApks(): List<Pair<String, String>> = downloadedApks.toList()

    /**
     * Returns list of download IDs that were requested to install.
     */
    fun getInstalledDownloadIds(): List<Long> = installedDownloadIds.toList()

    /**
     * Resets the repository to its initial state.
     */
    fun reset() {
        latestReleaseResult = null
        releaseByVersionResults.clear()
        downloadedApks.clear()
        installedDownloadIds.clear()
        nextDownloadId = 1L
    }
}
