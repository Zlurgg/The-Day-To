package uk.co.zlurgg.thedayto.core.data.repository

import uk.co.zlurgg.thedayto.core.domain.repository.LocalDataClearer
import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao
import uk.co.zlurgg.thedayto.notification.data.local.NotificationSettingsDao
import uk.co.zlurgg.thedayto.sync.data.dao.PendingSyncDeletionDao

/**
 * Implementation of LocalDataClearer that clears all local data.
 *
 * Used during account deletion to ensure all user data is removed
 * from the device after successful remote deletion.
 */
class LocalDataClearerImpl(
    private val entryDao: EntryDao,
    private val moodColorDao: MoodColorDao,
    private val notificationSettingsDao: NotificationSettingsDao,
    private val pendingSyncDeletionDao: PendingSyncDeletionDao,
    private val preferencesRepository: PreferencesRepository,
) : LocalDataClearer {

    override suspend fun clearAllLocalData() {
        entryDao.deleteAll()
        moodColorDao.deleteAll()
        notificationSettingsDao.deleteAll()
        pendingSyncDeletionDao.deleteAll()
    }

    override suspend fun clearPreferences() {
        preferencesRepository.clear()
    }
}
