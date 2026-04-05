package uk.co.zlurgg.thedayto.auth.domain.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.repository.LocalDataClearer
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository

/**
 * Use case for deleting the user's account and all associated data.
 *
 * Deletion order (critical for security rules):
 * 1. Cancel sync workers (prevent interference)
 * 2. Delete Firestore data (while still authenticated)
 * 3. Delete Firebase Auth account (point of no return)
 * 4. Clear local data
 *
 * Why this order:
 * - Firestore security rules require authentication
 * - Once Auth is deleted, we can't access Firestore
 * - Auth deletion is "point of no return"
 */
class DeleteAccountUseCase(
    private val authRepository: AuthRepository,
    private val syncRepository: SyncRepository,
    private val localDataClearer: LocalDataClearer,
    private val syncScheduler: SyncScheduler
) {
    operator fun invoke(): Flow<DeletionProgress> = flow {
        emit(DeletionProgress.Starting)

        // 1. Verify signed in
        val user = authRepository.getSignedInUser()
            ?: return@flow emit(DeletionProgress.Failed("You must be signed in."))

        // 2. Cancel sync workers
        emit(DeletionProgress.CancellingSync)
        syncScheduler.cancelAllSync()

        // 3. Delete Firestore data (while still authenticated)
        emit(DeletionProgress.DeletingRemote)
        when (val result = syncRepository.clearRemoteData(user.userId)) {
            is Result.Error -> {
                emit(DeletionProgress.Failed("Failed to delete cloud data. Please try again."))
                return@flow
            }
            is Result.Success -> { /* continue */ }
        }

        // 4. Delete Firebase Auth account
        emit(DeletionProgress.DeletingAccount)
        when (val result = authRepository.deleteAccount()) {
            is Result.Error -> {
                if (result.error == DataError.Auth.REQUIRES_RECENT_LOGIN) {
                    emit(DeletionProgress.RequiresReAuth)
                } else {
                    // Firestore already deleted - inform user
                    emit(
                        DeletionProgress.Failed(
                            "Cloud data deleted but account deletion failed. Please try again."
                        )
                    )
                }
                return@flow
            }
            is Result.Success -> { /* continue */ }
        }

        // 5. Clear local data
        emit(DeletionProgress.ClearingLocal)
        localDataClearer.clearAllLocalData()
        localDataClearer.clearPreferences()

        emit(DeletionProgress.Complete)
    }
}

/**
 * Progress states for account deletion.
 */
sealed interface DeletionProgress {
    data object Starting : DeletionProgress
    data object CancellingSync : DeletionProgress
    data object DeletingRemote : DeletionProgress
    data object DeletingAccount : DeletionProgress
    data object ClearingLocal : DeletionProgress
    data object Complete : DeletionProgress
    data object RequiresReAuth : DeletionProgress
    data class Failed(val message: String) : DeletionProgress
}
