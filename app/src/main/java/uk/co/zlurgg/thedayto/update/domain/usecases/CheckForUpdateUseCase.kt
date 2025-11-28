package uk.co.zlurgg.thedayto.update.domain.usecases

import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo
import uk.co.zlurgg.thedayto.update.domain.repository.UpdateRepository

/**
 * Use case to check for available app updates.
 * Compares current version with latest GitHub release.
 * Respects user's dismissal of specific versions.
 */
class CheckForUpdateUseCase(
    private val updateRepository: UpdateRepository,
    private val preferencesRepository: PreferencesRepository,
    private val currentVersion: String
) {
    suspend operator fun invoke(forceCheck: Boolean = false): UpdateInfo? {
        return try {
            Timber.d("Checking for updates (current: $currentVersion, force: $forceCheck)")

            val result = updateRepository.getLatestRelease()
            val updateInfo = result.getOrNull() ?: run {
                Timber.d("Failed to get release info")
                return null
            }

            // Check if newer version
            if (!isNewerVersion(updateInfo.versionName, currentVersion)) {
                Timber.d("Current version $currentVersion is up to date")
                return null
            }

            // Check if user dismissed this version (skip if force check)
            if (!forceCheck) {
                val dismissedVersion = preferencesRepository.getDismissedVersion()
                if (dismissedVersion == updateInfo.versionName) {
                    Timber.d("User dismissed version ${updateInfo.versionName}")
                    return null
                }
            }

            // Check if APK is available
            if (updateInfo.apkDownloadUrl == null) {
                Timber.w("Update available but no APK asset found")
                return null
            }

            Timber.i("Update available: ${updateInfo.versionName}")
            updateInfo
        } catch (e: Exception) {
            Timber.e(e, "Failed to check for updates")
            null
        }
    }

    /**
     * Compares semantic versions (e.g., 1.0.4 vs 1.0.3).
     * Returns true if remote version is newer than current.
     */
    internal fun isNewerVersion(remote: String, current: String): Boolean {
        val remoteParts = remote.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(remoteParts.size, currentParts.size)) {
            val r = remoteParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (r > c) return true
            if (r < c) return false
        }
        return false
    }
}
