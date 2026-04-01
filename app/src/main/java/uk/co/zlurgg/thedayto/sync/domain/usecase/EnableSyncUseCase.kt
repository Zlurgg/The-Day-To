package uk.co.zlurgg.thedayto.sync.domain.usecase

import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.sync.domain.model.SyncResult
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository

/**
 * Use Case: Enable cloud sync for the current user.
 *
 * Verifies the user is authenticated, enables sync in preferences,
 * and triggers an initial upload of local data to Firestore.
 */
class EnableSyncUseCase(
    private val authRepository: AuthRepository,
    private val preferencesRepository: PreferencesRepository,
    private val syncRepository: SyncRepository
) {
    suspend operator fun invoke(): Result<SyncResult, DataError.Sync> {
        // Verify user is authenticated
        val user = authRepository.getSignedInUser()
            ?: return Result.Error(DataError.Sync.NOT_AUTHENTICATED)

        // Enable sync in preferences
        preferencesRepository.setSyncEnabled(true)

        // Perform initial sync (upload local data)
        return syncRepository.performFullSync(user.userId)
    }
}
