package uk.co.zlurgg.thedayto.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

/**
 * Fake implementation of MoodColorRepository for testing.
 * Stores mood colors in memory and provides synchronous access for test verification.
 */
class FakeMoodColorRepository : MoodColorRepository {

    // In-memory storage for testing
    private val moodColors = mutableListOf<MoodColor>()
    private var nextId = 1

    override suspend fun insertMoodColor(moodColor: MoodColor) {
        val moodColorWithId = if (moodColor.id == null) {
            moodColor.copy(id = nextId++)
        } else {
            moodColor
        }
        moodColors.removeIf { it.id == moodColorWithId.id }
        moodColors.add(moodColorWithId)
    }

    override suspend fun deleteMoodColor(id: Int) {
        // Soft delete - set isDeleted flag
        val moodColor = moodColors.find { it.id == id }
        if (moodColor != null) {
            moodColors.removeIf { it.id == id }
            moodColors.add(moodColor.copy(isDeleted = true))
        }
    }

    override suspend fun getMoodColorById(id: Int): MoodColor? {
        return moodColors.find { it.id == id }
    }

    override suspend fun getMoodColorByName(mood: String): MoodColor? {
        // Case-insensitive lookup (matches production behavior)
        return moodColors.find { it.mood.trim().lowercase() == mood.trim().lowercase() }
    }

    override fun getMoodColors(): Flow<List<MoodColor>> = flow {
        // Only return non-deleted mood colors (matches production behavior)
        emit(moodColors.filter { !it.isDeleted })
    }

    override suspend fun updateMoodColor(moodColor: MoodColor) {
        moodColors.removeIf { it.id == moodColor.id }
        moodColors.add(moodColor)
    }

    /**
     * Helper method to reset the repository to its initial state.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        moodColors.clear()
        nextId = 1
    }

    /**
     * Helper method to get all mood colors synchronously for test assertions.
     * Includes deleted mood colors for verification.
     */
    fun getMoodColorsSync(): List<MoodColor> {
        return moodColors.toList()
    }

    /**
     * Helper method to add default mood colors for testing.
     */
    fun addDefaultMoods() {
        val defaults = listOf(
            MoodColor("Happy", "4CAF50", false, System.currentTimeMillis(), 1),
            MoodColor("Sad", "2196F3", false, System.currentTimeMillis(), 2),
            MoodColor("Angry", "F44336", false, System.currentTimeMillis(), 3),
            MoodColor("Calm", "9C27B0", false, System.currentTimeMillis(), 4),
            MoodColor("Anxious", "FF9800", false, System.currentTimeMillis(), 5)
        )
        moodColors.addAll(defaults)
        nextId = 6
    }
}
