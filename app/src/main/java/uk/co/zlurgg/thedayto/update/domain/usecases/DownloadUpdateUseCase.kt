package uk.co.zlurgg.thedayto.update.domain.usecases

import timber.log.Timber
import uk.co.zlurgg.thedayto.update.domain.model.UpdateConfig
import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo
import uk.co.zlurgg.thedayto.update.domain.repository.UpdateRepository

/**
 * Use case to download an APK update.
 * Returns the download ID for tracking progress.
 */
class DownloadUpdateUseCase(
    private val updateRepository: UpdateRepository,
    private val config: UpdateConfig
) {
    operator fun invoke(updateInfo: UpdateInfo): Long? {
        val downloadUrl = updateInfo.apkDownloadUrl ?: run {
            Timber.e("No APK download URL available")
            return null
        }

        val fileName = "${config.appName}-${updateInfo.versionName}.apk"
        Timber.i("Starting download: $fileName")

        return updateRepository.downloadApk(downloadUrl, fileName)
    }
}
