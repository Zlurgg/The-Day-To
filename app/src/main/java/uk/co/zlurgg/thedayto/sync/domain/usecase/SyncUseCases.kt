package uk.co.zlurgg.thedayto.sync.domain.usecase

/**
 * Aggregator for sync-related UseCases.
 *
 * Provides a convenient way to inject all sync UseCases together.
 */
data class SyncUseCases(
    val performSync: PerformSyncUseCase,
    val observeSyncState: ObserveSyncStateUseCase,
    val getLastSyncTimestamp: GetLastSyncTimestampUseCase,
    val prepareForSync: PrepareForSyncUseCase,
    val setSyncEnabled: SetSyncEnabledUseCase,
)
