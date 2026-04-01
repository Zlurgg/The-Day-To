package uk.co.zlurgg.thedayto.sync.domain.usecase

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Use Case: Check if cloud sync is enabled.
 */
class CheckSyncEnabledUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(): Boolean = preferencesRepository.isSyncEnabled()
}
