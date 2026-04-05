package uk.co.zlurgg.thedayto.core.domain.repository

/**
 * Interface for clearing all local data.
 *
 * Used during account deletion to ensure all user data is removed
 * from the device after successful remote deletion.
 */
interface LocalDataClearer {
    /**
     * Delete ALL local data (entries, mood colors, notifications, pending deletions).
     */
    suspend fun clearAllLocalData()

    /**
     * Clear all preferences (sync settings, timestamps, flags).
     */
    suspend fun clearPreferences()
}
