package uk.co.zlurgg.thedayto.sync.domain.usecase

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.sync.domain.model.SyncResult
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository

/**
 * Use Case: Perform a full bidirectional sync.
 *
 * Uploads pending local changes and downloads remote changes.
 * Only executes if sync is enabled and user is authenticated.
 */
class PerformSyncUseCase(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository,
    private val syncRepository: SyncRepository,
) {
    suspend operator fun invoke(): Result<SyncResult, DataError.Sync> {
        // Check if sync is enabled
        if (!preferencesRepository.isSyncEnabled()) {
            return Result.Error(DataError.Sync.SYNC_DISABLED)
        }

        // Verify user is authenticated
        val user = authRepository.getSignedInUser()
            ?: return Result.Error(DataError.Sync.NOT_AUTHENTICATED)

        // Perform sync
        val result = syncRepository.performFullSync(user.userId)

        // Update last sync timestamp on success
        if (result is Result.Success) {
            preferencesRepository.setLastSyncTimestamp(System.currentTimeMillis())
        }

        return result
    }
}
