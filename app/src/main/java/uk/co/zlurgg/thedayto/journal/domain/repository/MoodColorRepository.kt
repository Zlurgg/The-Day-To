package uk.co.zlurgg.thedayto.journal.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor

/**
 * Repository for managing mood colors.
 *
 * **Flow vs Suspend Functions:**
 * - `getMoodColors()` returns `Flow<List<MoodColor>>`:
 *   Provides a reactive stream that automatically emits new values when mood colors
 *   are added, updated, or deleted. Room database queries returning Flow are observed
 *   by the UI, ensuring the UI updates automatically when data changes.
 *
 * - Methods using `suspend` (e.g., insertMoodColor, getMoodColorById, deleteMoodColor):
 *   These are one-time operations that execute once and return a result. Use these for
 *   single queries or write operations where you don't need to observe changes.
 *   `insertMoodColor` returns the auto-generated ID of the newly inserted mood color.
 */
interface MoodColorRepository {
    suspend fun insertMoodColor(moodColor: MoodColor): Long
    suspend fun deleteMoodColor(id: Int)
    suspend fun getMoodColorById(id: Int): MoodColor?
    suspend fun getMoodColorByName(mood: String): MoodColor?
    fun getMoodColors(): Flow<List<MoodColor>>
    suspend fun updateMoodColor(moodColor: MoodColor)
}