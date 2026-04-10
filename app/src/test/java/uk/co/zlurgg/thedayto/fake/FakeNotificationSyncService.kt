package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.notification.domain.sync.NotificationSyncService

/**
 * Fake implementation of NotificationSyncService for testing.
 */
class FakeNotificationSyncService : NotificationSyncService {

    private val remoteSettings = mutableMapOf<String, NotificationSettings>()
    private var shouldFail = false
    private var uploadCount = 0
    private var downloadCount = 0

    fun setRemoteSettings(userId: String, settings: NotificationSettings) {
        remoteSettings[userId] = settings
    }

    fun setShouldFail(shouldFail: Boolean) {
        this.shouldFail = shouldFail
    }

    fun getUploadCount(): Int = uploadCount

    fun getDownloadCount(): Int = downloadCount

    fun reset() {
        remoteSettings.clear()
        shouldFail = false
        uploadCount = 0
        downloadCount = 0
    }

    override suspend fun uploadPending(userId: String): Result<Int, DataError.Sync> {
        if (shouldFail) {
            return Result.Error(DataError.Sync.NETWORK_ERROR)
        }
        uploadCount++
        return Result.Success(1)
    }

    override suspend fun download(userId: String): Result<NotificationSettings?, DataError.Sync> {
        if (shouldFail) {
            return Result.Error(DataError.Sync.NETWORK_ERROR)
        }
        downloadCount++
        return Result.Success(remoteSettings[userId])
    }

    override suspend fun deleteRemote(
        syncId: String,
        userId: String,
    ): EmptyResult<DataError.Sync> {
        if (shouldFail) {
            return Result.Error(DataError.Sync.NETWORK_ERROR)
        }
        remoteSettings.remove(userId)
        return Result.Success(Unit)
    }
}
