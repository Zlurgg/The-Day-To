package uk.co.zlurgg.thedayto.update.domain.usecases

import timber.log.Timber
import io.github.zlurgg.core.domain.result.getOrNull
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo
import uk.co.zlurgg.thedayto.update.domain.repository.UpdateRepository

/**
 * Use case to get release info for the current installed version.
 * Fetches changelog and version details from GitHub for display in "Up to Date" dialog.
 */
class GetCurrentVersionInfoUseCase(
    private val updateRepository: UpdateRepository,
    private val currentVersion: String
) {
    suspend operator fun invoke(): UpdateInfo? {
        return try {
            Timber.d("Fetching release info for current version: $currentVersion")
            val result = updateRepository.getReleaseByVersion(currentVersion)
            result.getOrNull()
        } catch (e: Exception) {
            Timber.e(e, "Failed to fetch current version info")
            null
        }
    }
}
