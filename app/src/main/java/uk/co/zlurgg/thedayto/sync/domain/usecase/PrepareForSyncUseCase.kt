package uk.co.zlurgg.thedayto.sync.domain.usecase

import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository

/**
 * Use Case: Prepare local data for sync after sign-in.
 *
 * Orchestrates the data preparation steps needed before sync:
 * 1. Clear data from other users (user isolation)
 * 2. Adopt orphaned data (userId = null) for the current user
 * 3. Mark local-only data as pending sync for upload
 *
 * @param syncRepository Repository for sync operations
 */
class PrepareForSyncUseCase(
    private val syncRepository: SyncRepository
) {
    /**
     * Prepare local data for sync.
     *
     * @param userId The ID of the signed-in user
     * @return Total count of items affected (cleared + adopted + marked)
     */
    suspend operator fun invoke(userId: String): Int {
        // Step 1: Clear data from any other user (user isolation)
        val clearedCount = syncRepository.clearOtherUserData(userId)

        // Step 2: Adopt orphaned data (userId = null) - data created before first sign-in
        val adoptedCount = syncRepository.adoptOrphanedData(userId)

        // Step 3: Mark any LOCAL_ONLY data as PENDING_SYNC so it gets uploaded
        val markedCount = syncRepository.markLocalDataForSync()

        return clearedCount + adoptedCount + markedCount
    }
}
