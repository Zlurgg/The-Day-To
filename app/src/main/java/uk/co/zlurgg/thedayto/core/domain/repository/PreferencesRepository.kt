package uk.co.zlurgg.thedayto.core.domain.repository

interface PreferencesRepository {
    fun setDailyEntryCreated(dailyEntryCreated: Boolean)
    fun getDailyEntryCreated(): Boolean
    fun setDailyEntryDate(datestamp: Long)
    fun getDailyEntryDate(): Long
}