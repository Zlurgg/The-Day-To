package uk.co.zlurgg.thedayto.sync.domain.model

/**
 * Result of a sync operation containing counts of synced items.
 */
data class SyncResult(
    val entriesUploaded: Int = 0,
    val entriesDownloaded: Int = 0,
    val moodColorsUploaded: Int = 0,
    val moodColorsDownloaded: Int = 0,
    val conflictsResolved: Int = 0
) {
    val totalUploaded: Int get() = entriesUploaded + moodColorsUploaded
    val totalDownloaded: Int get() = entriesDownloaded + moodColorsDownloaded
    val totalSynced: Int get() = totalUploaded + totalDownloaded

    fun isEmpty(): Boolean = totalSynced == 0
}
