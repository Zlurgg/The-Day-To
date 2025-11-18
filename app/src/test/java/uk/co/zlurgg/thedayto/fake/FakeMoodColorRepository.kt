package uk.co.zlurgg.thedayto.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.domain.repository.MoodColorRepository

/**
 * Fake implementation of MoodColorRepository for testing.
 * Stores mood colors in memory and provides synchronous access for test verification.
 * Uses StateFlow to simulate Room's reactive Flow behavior.
 */
class FakeMoodColorRepository : MoodColorRepository {

    // In-memory storage for testing (use StateFlow to emit on changes like Room)
    private val _moodColors = MutableStateFlow<List<MoodColor>>(emptyList())
    private var nextId = 1

    override suspend fun insertMoodColor(moodColor: MoodColor): Long {
        val moodColorWithId = if (moodColor.id == null) {
            moodColor.copy(id = nextId++)
        } else {
            moodColor
        }
        val currentList = _moodColors.value.toMutableList()
        currentList.removeIf { it.id == moodColorWithId.id }
        currentList.add(moodColorWithId)
        _moodColors.value = currentList
        return moodColorWithId.id!!.toLong()
    }

    override suspend fun deleteMoodColor(id: Int) {
        // Soft delete - set isDeleted flag
        val moodColor = _moodColors.value.find { it.id == id }
        if (moodColor != null) {
            val currentList = _moodColors.value.toMutableList()
            currentList.removeIf { it.id == id }
            currentList.add(moodColor.copy(isDeleted = true))
            _moodColors.value = currentList
        }
    }

    override suspend fun getMoodColorById(id: Int): MoodColor? {
        return _moodColors.value.find { it.id == id }
    }

    override suspend fun getMoodColorByName(mood: String): MoodColor? {
        // Case-insensitive lookup (matches production behavior)
        return _moodColors.value.find { it.mood.trim().lowercase() == mood.trim().lowercase() }
    }

    override fun getMoodColors(): Flow<List<MoodColor>> {
        // Only return non-deleted mood colors (matches production behavior)
        // Use map to filter, maintains reactive behavior like Room
        return _moodColors.map { list -> list.filter { !it.isDeleted } }
    }

    override suspend fun updateMoodColor(moodColor: MoodColor) {
        val currentList = _moodColors.value.toMutableList()
        currentList.removeIf { it.id == moodColor.id }
        currentList.add(moodColor)
        _moodColors.value = currentList
    }

    /**
     * Helper method to reset the repository to its initial state.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        _moodColors.value = emptyList()
        nextId = 1
    }

    /**
     * Helper method to get all mood colors synchronously for test assertions.
     * Includes deleted mood colors for verification.
     */
    fun getMoodColorsSync(): List<MoodColor> {
        return _moodColors.value.toList()
    }

    /**
     * Helper method to get mood color by ID synchronously.
     * Includes deleted mood colors. Used for simulating JOIN operations.
     */
    fun getMoodColorByIdSync(id: Int): MoodColor? {
        return _moodColors.value.find { it.id == id }
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
        _moodColors.value = defaults
        nextId = 6
    }
}
