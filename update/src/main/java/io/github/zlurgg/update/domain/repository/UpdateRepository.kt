package io.github.zlurgg.update.domain.repository

import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.Result
import io.github.zlurgg.update.domain.model.UpdateInfo

/**
 * Repository interface for update-related operations.
 * Domain layer - no framework dependencies.
 */
interface UpdateRepository {
    suspend fun getLatestRelease(): Result<UpdateInfo, DataError.Remote>
    suspend fun getReleaseByVersion(version: String): Result<UpdateInfo, DataError.Remote>
    fun downloadApk(url: String, fileName: String): Long
    fun installApk(downloadId: Long)
}
