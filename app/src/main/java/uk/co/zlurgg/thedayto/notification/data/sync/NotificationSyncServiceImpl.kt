package uk.co.zlurgg.thedayto.notification.data.sync

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsDao
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings
import uk.co.zlurgg.thedayto.notification.domain.sync.NotificationSyncService
import uk.co.zlurgg.thedayto.sync.data.mapper.FirestoreMapper.resolveNotificationSettingsConflict
import uk.co.zlurgg.thedayto.sync.data.mapper.FirestoreMapper.toFirestoreMap
import uk.co.zlurgg.thedayto.sync.data.mapper.FirestoreMapper.toNotificationSettingsEntity
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus
import java.util.UUID

/**
 * Firestore implementation of NotificationSyncService.
 *
 * Handles bidirectional sync of notification settings between Room and Firestore.
 */
class NotificationSyncServiceImpl(
    private val firestore: FirebaseFirestore,
    private val settingsDao: NotificationSettingsDao,
) : NotificationSyncService {

    override suspend fun uploadPending(userId: String): Result<Int, DataError.Sync> = runCatching {
        val pending = settingsDao.getPendingSync(userId) ?: return@runCatching 0

        val syncId = pending.syncId.takeIf { it.isNotBlank() } ?: UUID.randomUUID().toString()

        val docRef = firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(NOTIFICATION_SETTINGS_COLLECTION)
            .document(syncId)

        docRef.set(pending.toFirestoreMap()).await()

        // Update local with sync info
        settingsDao.upsert(
            pending.copy(
                syncId = syncId,
                syncStatus = SyncStatus.SYNCED.name,
            ),
        )

        Timber.d("Uploaded notification settings for user %s", userId)
        1
    }.fold(
        onSuccess = { Result.Success(it) },
        onFailure = { mapFirestoreException(it) },
    )

    override suspend fun download(userId: String): Result<NotificationSettings?, DataError.Sync> =
        runCatching {
            val snapshot = firestore
                .collection(USERS_COLLECTION)
                .document(userId)
                .collection(NOTIFICATION_SETTINGS_COLLECTION)
                .get()
                .await()

            if (snapshot.isEmpty) {
                Timber.d("No notification settings found remotely for user %s", userId)
                return@runCatching null
            }

            // We expect at most one document per user
            val remoteDoc = snapshot.documents.firstOrNull() ?: return@runCatching null
            val remoteEntity = remoteDoc.toNotificationSettingsEntity(userId) ?: return@runCatching null

            // Check for local settings
            val localEntity = settingsDao.getByUserId(userId)

            val finalEntity = if (localEntity != null) {
                // Resolve conflict
                val resolved = resolveNotificationSettingsConflict(localEntity, remoteEntity)
                if (resolved == localEntity) {
                    Timber.d("Local notification settings win - no update needed")
                    return@runCatching localEntity.toDomain()
                }
                resolved
            } else {
                remoteEntity
            }

            // Save the resolved/new entity
            settingsDao.upsert(finalEntity)
            Timber.d("Downloaded notification settings for user %s", userId)

            finalEntity.toDomain()
        }.fold(
            onSuccess = { Result.Success(it) },
            onFailure = { mapFirestoreException(it) },
        )

    override suspend fun deleteRemote(
        syncId: String,
        userId: String,
    ): EmptyResult<DataError.Sync> = runCatching {
        firestore
            .collection(USERS_COLLECTION)
            .document(userId)
            .collection(NOTIFICATION_SETTINGS_COLLECTION)
            .document(syncId)
            .delete()
            .await()

        Timber.d("Deleted notification settings from Firestore: syncId=%s", syncId)
    }.fold(
        onSuccess = { Result.Success(Unit) },
        onFailure = { mapFirestoreException(it) },
    )

    private fun <T> mapFirestoreException(throwable: Throwable): Result<T, DataError.Sync> {
        Timber.e(throwable, "Notification sync error")

        val error = when (throwable) {
            is FirebaseFirestoreException -> mapFirebaseException(throwable)
            is java.net.UnknownHostException,
            is java.net.SocketTimeoutException,
            is java.net.ConnectException,
            -> DataError.Sync.NETWORK_ERROR

            else -> DataError.Sync.UNKNOWN
        }

        return Result.Error(error)
    }

    private fun mapFirebaseException(e: FirebaseFirestoreException): DataError.Sync = when (e.code) {
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> DataError.Sync.PERMISSION_DENIED
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> DataError.Sync.NOT_AUTHENTICATED
        FirebaseFirestoreException.Code.RESOURCE_EXHAUSTED -> DataError.Sync.QUOTA_EXCEEDED
        FirebaseFirestoreException.Code.UNAVAILABLE -> DataError.Sync.NETWORK_ERROR
        else -> DataError.Sync.UNKNOWN
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val NOTIFICATION_SETTINGS_COLLECTION = "notification_settings"
    }
}
