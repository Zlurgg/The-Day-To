package uk.co.zlurgg.thedayto.sync.domain.usecase

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Use Case: Disable cloud sync.
 *
 * Disables sync in preferences. Local data status (SYNCED, PENDING_SYNC, etc.)
 * remains unchanged - data is preserved locally but no longer syncs to cloud.
 * Does NOT delete remote data (user may want to re-enable sync later).
 */
class DisableSyncUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke() {
        preferencesRepository.setSyncEnabled(false)
    }
}
