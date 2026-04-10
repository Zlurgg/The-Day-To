package uk.co.zlurgg.thedayto.sync.domain.usecase

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Enables or disables cloud sync.
 *
 * Follows Clean Architecture:
 * - Wraps repository call to prevent direct repository access from ViewModels
 * - Part of sync domain operations
 *
 * @param enabled true to enable sync, false to disable
 */
class SetSyncEnabledUseCase(
    private val preferencesRepository: PreferencesRepository,
) {
    suspend operator fun invoke(enabled: Boolean) {
        preferencesRepository.setSyncEnabled(enabled)
    }
}
