package uk.co.zlurgg.thedayto.sync.data.mapper

import org.junit.Assert.assertEquals
import org.junit.Test
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus

/**
 * Unit tests for FirestoreMapper conflict resolution logic.
 *
 * Tests cover:
 * - Entry conflict resolution with last-write-wins strategy
 * - MoodColor conflict resolution with pending state protection
 * - Seed behavior (updatedAt <= 0 always loses to remote)
 */
class FirestoreMapperTest {

    // ============================================================
    // Entry Conflict Resolution Tests
    // ============================================================

    @Test
    fun `resolveEntryConflict - local PENDING_SYNC wins over remote`() {
        // Given: Local entry with PENDING_SYNC status and older timestamp
        val local = Entry(
            moodColorId = 1,
            content = "Local content",
            dateStamp = 1000L,
            id = 10,
            syncId = "sync_123",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        val remote = Entry(
            moodColorId = 1,
            content = "Remote content",
            dateStamp = 1000L,
            id = null,
            syncId = "sync_123",
            userId = "user_1",
            updatedAt = 6000L, // Remote is newer
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveEntryConflict(local, remote)

        // Then: Local wins because it has pending changes
        assertEquals(local, result)
    }

    @Test
    fun `resolveEntryConflict - remote newer wins and preserves local ID`() {
        // Given: Local entry with SYNCED status and older timestamp
        val local = Entry(
            moodColorId = 1,
            content = "Local content",
            dateStamp = 1000L,
            id = 10,
            syncId = "sync_123",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.SYNCED
        )
        val remote = Entry(
            moodColorId = 1,
            content = "Remote content",
            dateStamp = 1000L,
            id = null,
            syncId = "sync_123",
            userId = "user_1",
            updatedAt = 6000L, // Remote is newer
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveEntryConflict(local, remote)

        // Then: Remote wins but preserves local Room ID
        assertEquals(10, result.id)
        assertEquals("Remote content", result.content)
        assertEquals(6000L, result.updatedAt)
    }

    @Test
    fun `resolveEntryConflict - local newer marks for PENDING_SYNC`() {
        // Given: Local entry with newer timestamp
        val local = Entry(
            moodColorId = 1,
            content = "Local content",
            dateStamp = 1000L,
            id = 10,
            syncId = "sync_123",
            userId = "user_1",
            updatedAt = 7000L, // Local is newer
            syncStatus = SyncStatus.SYNCED
        )
        val remote = Entry(
            moodColorId = 1,
            content = "Remote content",
            dateStamp = 1000L,
            id = null,
            syncId = "sync_123",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveEntryConflict(local, remote)

        // Then: Local wins and is marked for re-upload
        assertEquals("Local content", result.content)
        assertEquals(10, result.id)
        assertEquals(SyncStatus.PENDING_SYNC, result.syncStatus)
    }

    @Test
    fun `resolveEntryConflict - seed (updatedAt = 0) is always overwritten by remote`() {
        // Given: Local seed entry (updatedAt = 0) with PENDING_SYNC
        val localSeed = Entry(
            moodColorId = 1,
            content = "Seed content",
            dateStamp = 1000L,
            id = 10,
            syncId = "seed_entry",
            userId = "user_1",
            updatedAt = 0L, // Seed indicator
            syncStatus = SyncStatus.PENDING_SYNC
        )
        val remote = Entry(
            moodColorId = 1,
            content = "Remote user content",
            dateStamp = 1000L,
            id = null,
            syncId = "seed_entry",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveEntryConflict(localSeed, remote)

        // Then: Remote wins even though local has PENDING_SYNC (because it's a seed)
        assertEquals("Remote user content", result.content)
        assertEquals(10, result.id) // Preserves local ID
    }

    // ============================================================
    // MoodColor Conflict Resolution Tests
    // ============================================================

    @Test
    fun `resolveMoodColorConflict - local PENDING_SYNC wins over remote`() {
        // Given: Local mood color with PENDING_SYNC status
        val local = MoodColor(
            mood = "Happy",
            color = "FFA726",
            isDeleted = false,
            dateStamp = 1000L,
            id = 5,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.PENDING_SYNC
        )
        val remote = MoodColor(
            mood = "Happy",
            color = "FF5722", // Different color
            isDeleted = false,
            dateStamp = 1000L,
            id = null,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 6000L, // Remote is newer
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveMoodColorConflict(local, remote)

        // Then: Local wins because it has pending changes
        assertEquals(local, result)
    }

    @Test
    fun `resolveMoodColorConflict - local PENDING_DELETE wins over remote`() {
        // Given: Local mood color marked for deletion
        val local = MoodColor(
            mood = "Happy",
            color = "FFA726",
            isDeleted = true,
            dateStamp = 1000L,
            id = 5,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.PENDING_DELETE
        )
        val remote = MoodColor(
            mood = "Happy",
            color = "FFA726",
            isDeleted = false,
            dateStamp = 1000L,
            id = null,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 6000L, // Remote is newer
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveMoodColorConflict(local, remote)

        // Then: Local wins because it has pending delete
        assertEquals(local, result)
    }

    @Test
    fun `resolveMoodColorConflict - remote newer wins and preserves local ID`() {
        // Given: Local mood color with SYNCED status and older timestamp
        val local = MoodColor(
            mood = "Happy",
            color = "FFA726",
            isDeleted = false,
            dateStamp = 1000L,
            id = 5,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.SYNCED
        )
        val remote = MoodColor(
            mood = "Happy",
            color = "FF5722", // Updated color
            isDeleted = false,
            dateStamp = 1000L,
            id = null,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 6000L, // Remote is newer
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveMoodColorConflict(local, remote)

        // Then: Remote wins but preserves local Room ID
        assertEquals(5, result.id)
        assertEquals("FF5722", result.color)
        assertEquals(6000L, result.updatedAt)
    }

    @Test
    fun `resolveMoodColorConflict - local newer marks for PENDING_SYNC`() {
        // Given: Local mood color with newer timestamp
        val local = MoodColor(
            mood = "Happy",
            color = "FFA726",
            isDeleted = false,
            dateStamp = 1000L,
            id = 5,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 7000L, // Local is newer
            syncStatus = SyncStatus.SYNCED
        )
        val remote = MoodColor(
            mood = "Happy",
            color = "FF5722",
            isDeleted = false,
            dateStamp = 1000L,
            id = null,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveMoodColorConflict(local, remote)

        // Then: Local wins and is marked for re-upload
        assertEquals("FFA726", result.color)
        assertEquals(5, result.id)
        assertEquals(SyncStatus.PENDING_SYNC, result.syncStatus)
    }

    @Test
    fun `resolveMoodColorConflict - seed (updatedAt = 0) is always overwritten by remote`() {
        // Given: Local seed mood color (updatedAt = 0)
        val localSeed = MoodColor(
            mood = "Happy",
            color = "FFA726",
            isDeleted = false,
            dateStamp = 1000L,
            id = 5,
            syncId = "seed_happy",
            userId = "user_1",
            updatedAt = 0L, // Seed indicator
            syncStatus = SyncStatus.PENDING_SYNC
        )
        val remote = MoodColor(
            mood = "Happy",
            color = "FF9800", // User customized color
            isDeleted = false,
            dateStamp = 1000L,
            id = null,
            syncId = "seed_happy",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveMoodColorConflict(localSeed, remote)

        // Then: Remote wins even though local has PENDING_SYNC (because it's a seed)
        assertEquals("FF9800", result.color)
        assertEquals(5, result.id) // Preserves local ID
    }

    @Test
    fun `resolveMoodColorConflict - seed with negative updatedAt is overwritten`() {
        // Given: Local seed with negative updatedAt (edge case)
        val localSeed = MoodColor(
            mood = "Sad",
            color = "1565C0",
            isDeleted = false,
            dateStamp = 1000L,
            id = 6,
            syncId = "seed_sad",
            userId = "user_1",
            updatedAt = -1L, // Negative value also indicates seed
            syncStatus = SyncStatus.LOCAL_ONLY
        )
        val remote = MoodColor(
            mood = "Sad",
            color = "2196F3", // User customized color
            isDeleted = false,
            dateStamp = 1000L,
            id = null,
            syncId = "seed_sad",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveMoodColorConflict(localSeed, remote)

        // Then: Remote wins (seed loses)
        assertEquals("2196F3", result.color)
        assertEquals(6, result.id)
    }

    @Test
    fun `resolveMoodColorConflict - equal timestamps prefer remote`() {
        // Given: Local and remote with equal timestamps
        val local = MoodColor(
            mood = "Happy",
            color = "FFA726",
            isDeleted = false,
            dateStamp = 1000L,
            id = 5,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 5000L,
            syncStatus = SyncStatus.SYNCED
        )
        val remote = MoodColor(
            mood = "Happy",
            color = "FF5722",
            isDeleted = false,
            dateStamp = 1000L,
            id = null,
            syncId = "mood_123",
            userId = "user_1",
            updatedAt = 5000L, // Equal timestamp
            syncStatus = SyncStatus.SYNCED
        )

        // When: Resolving conflict
        val result = FirestoreMapper.resolveMoodColorConflict(local, remote)

        // Then: Remote wins on tie (>= comparison)
        assertEquals("FF5722", result.color)
        assertEquals(5, result.id)
    }
}
