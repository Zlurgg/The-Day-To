package uk.co.zlurgg.thedayto.journal.domain.repository

import kotlinx.coroutines.flow.Flow
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.EmptyResult
import uk.co.zlurgg.thedayto.core.domain.result.Result
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
    suspend fun insertMoodColor(moodColor: MoodColor): Result<Long, DataError.Local>
    suspend fun deleteMoodColor(id: Int): EmptyResult<DataError.Local>
    suspend fun getMoodColorById(id: Int): Result<MoodColor?, DataError.Local>
    suspend fun getMoodColorByName(mood: String): Result<MoodColor?, DataError.Local>
    suspend fun getActiveCount(): Result<Int, DataError.Local>

    /**
     * Returns the set of normalized (lowercase) mood names for all active
     * (non-deleted) mood colors. Used for bulk duplicate checking by the
     * random seeder. Unlike other repository methods, this does NOT return
     * a [Result] — a DAO exception propagates directly. Callers should
     * handle this at the coroutine level (e.g. try/finally).
     */
    suspend fun getActiveMoodNames(): Set<String>

    fun getMoodColors(): Flow<List<MoodColor>>
    suspend fun updateMoodColor(moodColor: MoodColor): EmptyResult<DataError.Local>
    suspend fun setFavorite(id: Int, isFavorite: Boolean): EmptyResult<DataError.Local>
    suspend fun restore(id: Int): EmptyResult<DataError.Local>
}
