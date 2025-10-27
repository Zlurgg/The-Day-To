package uk.co.zlurgg.thedayto.fake

import uk.co.zlurgg.thedayto.core.domain.repository.PreferencesRepository

/**
 * Fake implementation of PreferencesRepository for testing.
 * Stores data in memory instead of SharedPreferences.
 */
class FakePreferencesRepository : PreferencesRepository {

    // In-memory storage for testing
    private var entryCreated: Boolean = false
    private var entryDate: Long = 0L
    private var signedInState: Boolean = false

    override fun entryCreated(entryCreated: Boolean) {
        this.entryCreated = entryCreated
    }

    override fun getEntryCreated(): Boolean {
        return entryCreated
    }

    override fun setEntryDate(datestamp: Long) {
        this.entryDate = datestamp
    }

    override fun getEntryDate(): Long {
        return entryDate
    }

    override fun setSignedInState(isSignedIn: Boolean) {
        this.signedInState = isSignedIn
    }

    override fun getSignedInState(): Boolean {
        return signedInState
    }

    /**
     * Helper method to reset all values to defaults.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        entryCreated = false
        entryDate = 0L
        signedInState = false
    }
}
