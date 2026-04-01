package uk.co.zlurgg.thedayto.sync.domain.usecase

/**
 * Aggregator for sync-related UseCases.
 *
 * Provides a convenient way to inject all sync UseCases together.
 */
data class SyncUseCases(
    val enableSync: EnableSyncUseCase,
    val disableSync: DisableSyncUseCase,
    val performSync: PerformSyncUseCase,
    val observeSyncState: ObserveSyncStateUseCase,
    val checkSyncEnabled: CheckSyncEnabledUseCase,
    val getLastSyncTimestamp: GetLastSyncTimestampUseCase
)
