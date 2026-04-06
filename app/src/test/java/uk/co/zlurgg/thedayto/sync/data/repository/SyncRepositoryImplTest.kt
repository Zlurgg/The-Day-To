package uk.co.zlurgg.thedayto.sync.data.repository

import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.journal.data.dao.EntryDao
import uk.co.zlurgg.thedayto.journal.data.dao.MoodColorDao
import uk.co.zlurgg.thedayto.journal.data.model.MoodColorEntity
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.notification.domain.sync.NotificationSyncService
import uk.co.zlurgg.thedayto.sync.data.dao.PendingSyncDeletionDao
import uk.co.zlurgg.thedayto.sync.domain.model.SyncStatus

/**
 * Unit tests for SyncRepositoryImpl.
 *
 * Tests cover:
 * - Batch query usage in uploadPendingEntries (N+1 fix)
 * - Upload counts entries correctly
 * - Error handling propagates correctly
 */
class SyncRepositoryImplTest {

    private lateinit var repository: SyncRepositoryImpl
    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockEntryDao: EntryDao
    private lateinit var mockMoodColorDao: MoodColorDao
    private lateinit var mockPendingSyncDeletionDao: PendingSyncDeletionDao
    private lateinit var mockNotificationSyncService: NotificationSyncService

    private val testUserId = "test_user_123"

    @Before
    fun setup() {
        mockFirestore = mockk(relaxed = true)
        mockEntryDao = mockk(relaxed = true)
        mockMoodColorDao = mockk(relaxed = true)
        mockPendingSyncDeletionDao = mockk(relaxed = true)
        mockNotificationSyncService = mockk(relaxed = true)

        repository = SyncRepositoryImpl(
            firestore = mockFirestore,
            entryDao = mockEntryDao,
            moodColorDao = mockMoodColorDao,
            pendingSyncDeletionDao = mockPendingSyncDeletionDao,
            notificationSyncService = mockNotificationSyncService
        )
    }

    // ============================================================
    // Batch Query Tests (N+1 Fix)
    // ============================================================

    @Test
    fun `uploadPendingEntries uses batch query for mood colors`() = runTest {
        // Given: Multiple entries with different mood color IDs
        val entries = listOf(
            createTestEntry(id = 1, moodColorId = 10),
            createTestEntry(id = 2, moodColorId = 20),
            createTestEntry(id = 3, moodColorId = 10) // Duplicate moodColorId
        )

        val moodColorEntities = listOf(
            createMoodColorEntity(id = 10, syncId = "sync_10"),
            createMoodColorEntity(id = 20, syncId = "sync_20")
        )

        // Mock batch query to return mood colors
        coEvery { mockMoodColorDao.getMoodColorsByIds(any()) } returns moodColorEntities

        // Mock Firestore document operations
        setupFirestoreMocks()

        // When: Uploading entries
        val result = repository.uploadPendingEntries(entries, testUserId)

        // Then: Batch query was used (not individual getMoodColorById calls)
        coVerify(exactly = 1) { mockMoodColorDao.getMoodColorsByIds(listOf(10, 20)) }
        coVerify(exactly = 0) { mockMoodColorDao.getMoodColorById(any()) }

        // And: Result is successful with correct count
        assertTrue(result is Result.Success)
        assertEquals(3, (result as Result.Success).data)
    }

    @Test
    fun `uploadPendingEntries handles empty entry list`() = runTest {
        // Given: Empty entry list
        val entries = emptyList<Entry>()

        // When: Uploading entries
        val result = repository.uploadPendingEntries(entries, testUserId)

        // Then: Returns success with zero count
        assertTrue(result is Result.Success)
        assertEquals(0, (result as Result.Success).data)

        // And: No database queries were made
        coVerify(exactly = 0) { mockMoodColorDao.getMoodColorsByIds(any()) }
    }

    @Test
    fun `uploadPendingEntries deduplicates mood color IDs before batch query`() = runTest {
        // Given: Entries with duplicate mood color IDs
        val entries = listOf(
            createTestEntry(id = 1, moodColorId = 10),
            createTestEntry(id = 2, moodColorId = 10),
            createTestEntry(id = 3, moodColorId = 10)
        )

        val moodColorEntities = listOf(
            createMoodColorEntity(id = 10, syncId = "sync_10")
        )

        coEvery { mockMoodColorDao.getMoodColorsByIds(any()) } returns moodColorEntities
        setupFirestoreMocks()

        // When: Uploading entries
        repository.uploadPendingEntries(entries, testUserId)

        // Then: Batch query called with deduplicated IDs
        val capturedIds = slot<List<Int>>()
        coVerify { mockMoodColorDao.getMoodColorsByIds(capture(capturedIds)) }
        assertEquals(listOf(10), capturedIds.captured)
    }

    // ============================================================
    // Local Data Operations Tests
    // ============================================================

    @Test
    fun `markLocalDataForSync delegates to DAOs`() = runTest {
        // Given: DAOs return counts
        coEvery { mockMoodColorDao.markLocalOnlyAsPendingSync() } returns 3
        coEvery { mockEntryDao.markLocalOnlyAsPendingSync() } returns 5

        // When: Marking local data for sync
        val result = repository.markLocalDataForSync()

        // Then: Returns combined count
        assertEquals(8, result)
        coVerify { mockMoodColorDao.markLocalOnlyAsPendingSync() }
        coVerify { mockEntryDao.markLocalOnlyAsPendingSync() }
    }

    @Test
    fun `adoptOrphanedData delegates to DAOs with userId`() = runTest {
        // Given: DAOs return counts
        coEvery { mockMoodColorDao.adoptOrphans(testUserId) } returns 2
        coEvery { mockEntryDao.adoptOrphans(testUserId) } returns 4

        // When: Adopting orphaned data
        val result = repository.adoptOrphanedData(testUserId)

        // Then: Returns combined count
        assertEquals(6, result)
        coVerify { mockMoodColorDao.adoptOrphans(testUserId) }
        coVerify { mockEntryDao.adoptOrphans(testUserId) }
    }

    @Test
    fun `markSyncedAsLocalOnly delegates to DAOs`() = runTest {
        // Given: DAOs return counts
        coEvery { mockMoodColorDao.markSyncedAsLocalOnly() } returns 1
        coEvery { mockEntryDao.markSyncedAsLocalOnly() } returns 2

        // When: Marking synced as local only
        val result = repository.markSyncedAsLocalOnly()

        // Then: Returns combined count
        assertEquals(3, result)
        coVerify { mockMoodColorDao.markSyncedAsLocalOnly() }
        coVerify { mockEntryDao.markSyncedAsLocalOnly() }
    }

    @Test
    fun `clearUserData delegates to DAOs`() = runTest {
        // Given: DAOs return counts
        coEvery { mockEntryDao.deleteByUserId(testUserId) } returns 5
        coEvery { mockMoodColorDao.deleteByUserId(testUserId) } returns 3

        // When: Clearing user data
        val result = repository.clearUserData(testUserId)

        // Then: Returns combined count
        assertEquals(8, result)
        coVerify { mockEntryDao.deleteByUserId(testUserId) }
        coVerify { mockMoodColorDao.deleteByUserId(testUserId) }
    }

    // ============================================================
    // Helper Functions
    // ============================================================

    private fun setupFirestoreMocks() {
        val mockCollection = mockk<CollectionReference>(relaxed = true)
        val mockDocument = mockk<DocumentReference>(relaxed = true)
        val mockQuerySnapshot = mockk<QuerySnapshot>(relaxed = true)

        every { mockFirestore.collection(any()) } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument
        every { mockDocument.collection(any()) } returns mockCollection
        every { mockDocument.set(any()) } returns Tasks.forResult(null)
        every { mockCollection.get() } returns Tasks.forResult(mockQuerySnapshot)
        every { mockQuerySnapshot.documents } returns emptyList()
    }

    private fun createTestEntry(
        id: Int,
        moodColorId: Int,
        syncId: String? = "sync_$id",
        syncStatus: SyncStatus = SyncStatus.PENDING_SYNC
    ) = Entry(
        id = id,
        dateStamp = 1000L + id,
        content = "Test entry $id",
        moodColorId = moodColorId,
        syncId = syncId,
        userId = testUserId,
        syncStatus = syncStatus,
        updatedAt = 2000L
    )

    private fun createMoodColorEntity(
        id: Int,
        syncId: String,
        mood: String = "Test Mood $id"
    ) = MoodColorEntity(
        id = id,
        mood = mood,
        moodNormalized = mood.lowercase(),
        color = "#FF0000",
        syncId = syncId,
        userId = testUserId,
        syncStatus = SyncStatus.SYNCED.name,
        dateStamp = 1000L,
        updatedAt = 2000L,
        isDeleted = false
    )
}
