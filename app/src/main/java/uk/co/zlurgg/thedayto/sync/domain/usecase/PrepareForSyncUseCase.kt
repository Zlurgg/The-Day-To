package uk.co.zlurgg.thedayto.sync.domain.usecase

import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository

/**
 * Use Case: Prepare local data for sync after sign-in.
 *
 * Orchestrates the data preparation steps needed before sync:
 * 1. Adopt orphaned data (userId = null) for the current user
 * 2. Mark local-only data as pending sync for upload
 *
 * Note: We intentionally keep data from other users visible locally.
 * This treats the phone as a personal device where sync is for backup/transfer,
 * not data isolation. Other users' data won't sync (different userId).
 *
 * @param syncRepository Repository for sync operations
 */
class PrepareForSyncUseCase(
    private val syncRepository: SyncRepository,
) {
    /**
     * Prepare local data for sync.
     *
     * @param userId The ID of the signed-in user
     * @return Total count of items affected (adopted + marked)
     */
    suspend operator fun invoke(userId: String): Int {
        // Step 1: Adopt orphaned data (userId = null) - data created before first sign-in
        val adoptedCount = syncRepository.adoptOrphanedData(userId)

        // Step 2: Mark any LOCAL_ONLY data as PENDING_SYNC so it gets uploaded
        val markedCount = syncRepository.markLocalDataForSync()

        return adoptedCount + markedCount
    }
}
