package uk.co.zlurgg.thedayto.sync.domain.usecase

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Use Case: Get the timestamp of the last successful sync.
 */
class GetLastSyncTimestampUseCase(
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(): Long? = preferencesRepository.getLastSyncTimestamp()
}
