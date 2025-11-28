package uk.co.zlurgg.thedayto.update.domain.usecases

import timber.log.Timber
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Use case to dismiss a specific update version.
 * Stores the version so the user won't be prompted again.
 */
class DismissUpdateUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(version: String) {
        Timber.i("User dismissed update version: $version")
        preferencesRepository.setDismissedVersion(version)
    }
}
