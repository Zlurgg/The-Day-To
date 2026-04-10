package uk.co.zlurgg.thedayto.sync.domain.usecase

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState
import uk.co.zlurgg.thedayto.sync.domain.repository.SyncRepository

/**
 * Use Case: Observe the current sync state.
 *
 * Returns a Flow that emits sync state changes (Idle, Syncing, Success, Error).
 */
class ObserveSyncStateUseCase(
    private val syncRepository: SyncRepository,
) {
    operator fun invoke(): Flow<SyncState> = syncRepository.observeSyncState()
}
