package io.github.zlurgg.update.domain.usecases

import timber.log.Timber
import io.github.zlurgg.update.domain.repository.UpdatePreferencesRepository

/**
 * Use case to dismiss a specific update version.
 * Stores the version so the user won't be prompted again.
 */
class DismissUpdateUseCase(
    private val updatePreferencesRepository: UpdatePreferencesRepository
) {
    suspend operator fun invoke(version: String) {
        Timber.i("User dismissed update version: $version")
        updatePreferencesRepository.setDismissedVersion(version)
    }
}
