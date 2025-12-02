package uk.co.zlurgg.thedayto.update.domain.repository

/**
 * Repository interface for update-related preferences.
 * Minimal interface following Interface Segregation Principle.
 *
 * This allows the update package to be reused without depending
 * on the full PreferencesRepository interface.
 */
interface UpdatePreferencesRepository {
    /**
     * Get the version the user has dismissed (opted out of updating to).
     *
     * @return version string that was dismissed, or null if none
     */
    suspend fun getDismissedVersion(): String?

    /**
     * Set the version the user has dismissed.
     *
     * @param version the version string to mark as dismissed
     */
    suspend fun setDismissedVersion(version: String)
}
