package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo
import uk.co.zlurgg.thedayto.update.domain.repository.UpdateRepository

/**
 * Fake implementation of UpdateRepository for testing.
 * Allows control over returned values and error states.
 */
class FakeUpdateRepository : UpdateRepository {

    private var latestReleaseResult: Result<UpdateInfo>? = null
    private var downloadedApks = mutableListOf<Pair<String, String>>()
    private var installedDownloadIds = mutableListOf<Long>()
    private var nextDownloadId = 1L

    /**
     * Sets the result to return from getLatestRelease().
     */
    fun setLatestReleaseResult(result: Result<UpdateInfo>) {
        latestReleaseResult = result
    }

    /**
     * Helper to set a successful release response.
     */
    fun setLatestRelease(updateInfo: UpdateInfo) {
        latestReleaseResult = Result.success(updateInfo)
    }

    /**
     * Helper to simulate network error.
     */
    fun setNetworkError(exception: Exception = Exception("Network error")) {
        latestReleaseResult = Result.failure(exception)
    }

    override suspend fun getLatestRelease(): Result<UpdateInfo> {
        return latestReleaseResult ?: Result.failure(Exception("No result set"))
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
        downloadedApks.clear()
        installedDownloadIds.clear()
        nextDownloadId = 1L
    }
}
