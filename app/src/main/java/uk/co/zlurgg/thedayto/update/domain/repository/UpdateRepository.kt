package uk.co.zlurgg.thedayto.update.domain.repository

import uk.co.zlurgg.thedayto.update.domain.model.UpdateInfo

/**
 * Repository interface for update-related operations.
 * Domain layer - no framework dependencies.
 */
interface UpdateRepository {
    suspend fun getLatestRelease(): Result<UpdateInfo>
    fun downloadApk(url: String, fileName: String): Long
    fun installApk(downloadId: Long)
}
