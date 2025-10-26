package uk.co.zlurgg.thedayto.core.domain.repository

interface PreferencesRepository {
    fun entryCreated(entryCreated: Boolean)
    fun getEntryCreated(): Boolean
    fun setEntryDate(datestamp: Long)
    fun getEntryDate(): Long
    fun setSignedInState(isSignedIn: Boolean)
    fun getSignedInState(): Boolean
}