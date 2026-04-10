package uk.co.zlurgg.thedayto.sync.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository
import uk.co.zlurgg.thedayto.fake.FakeSyncRepository
import uk.co.zlurgg.thedayto.sync.domain.model.SyncResult

/**
 * Unit tests for PerformSyncUseCase.
 *
 * Tests cover:
 * - Sync disabled returns SYNC_DISABLED error
 * - User not authenticated returns NOT_AUTHENTICATED error
 * - Success case triggers sync and updates timestamp
 * - Failure case does not update timestamp
 */
class PerformSyncUseCaseTest {

    private lateinit var useCase: PerformSyncUseCase
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var fakeSyncRepository: FakeSyncRepository

    private val testUser = UserData(
        userId = "test_user_123",
        username = "Test User",
        profilePictureUrl = null,
    )

    @Before
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        fakePreferencesRepository = FakePreferencesRepository()
        fakeSyncRepository = FakeSyncRepository()

        useCase = PerformSyncUseCase(
            authRepository = fakeAuthRepository,
            preferencesRepository = fakePreferencesRepository,
            syncRepository = fakeSyncRepository,
        )
    }

    // ============================================================
    // Precondition Tests
    // ============================================================

    @Test
    fun `invoke returns SYNC_DISABLED error when sync is disabled`() = runTest {
        // Given: Sync is disabled
        fakePreferencesRepository.setSyncEnabled(false)
        fakeAuthRepository.setSignedInUser(testUser)

        // When: Invoking use case
        val result = useCase()

        // Then: Returns SYNC_DISABLED error
        assertTrue(result is Result.Error)
        assertEquals(DataError.Sync.SYNC_DISABLED, (result as Result.Error).error)

        // And: Repository sync not called
        assertTrue(!fakeSyncRepository.performFullSyncCalled)
    }

    @Test
    fun `invoke returns NOT_AUTHENTICATED error when user is not signed in`() = runTest {
        // Given: Sync enabled but user not signed in
        fakePreferencesRepository.setSyncEnabled(true)
        fakeAuthRepository.setSignedInUser(null)

        // When: Invoking use case
        val result = useCase()

        // Then: Returns NOT_AUTHENTICATED error
        assertTrue(result is Result.Error)
        assertEquals(DataError.Sync.NOT_AUTHENTICATED, (result as Result.Error).error)

        // And: Repository sync not called
        assertTrue(!fakeSyncRepository.performFullSyncCalled)
    }

    // ============================================================
    // Success Case Tests
    // ============================================================

    @Test
    fun `invoke performs sync when enabled and authenticated`() = runTest {
        // Given: Sync enabled and user signed in
        fakePreferencesRepository.setSyncEnabled(true)
        fakeAuthRepository.setSignedInUser(testUser)
        fakeSyncRepository.syncResult = SyncResult(
            entriesUploaded = 2,
            entriesDownloaded = 3,
        )

        // When: Invoking use case
        val result = useCase()

        // Then: Returns success with sync result
        assertTrue(result is Result.Success)
        assertEquals(2, (result as Result.Success).data.entriesUploaded)
        assertEquals(3, result.data.entriesDownloaded)

        // And: Repository sync was called with correct userId
        assertTrue(fakeSyncRepository.performFullSyncCalled)
        assertEquals(testUser.userId, fakeSyncRepository.lastUserId)
    }

    @Test
    fun `invoke updates last sync timestamp on success`() = runTest {
        // Given: Sync enabled and user signed in, no previous timestamp
        fakePreferencesRepository.setSyncEnabled(true)
        fakeAuthRepository.setSignedInUser(testUser)
        assertNull(fakePreferencesRepository.getLastSyncTimestamp())

        // When: Invoking use case (successful)
        val beforeSync = System.currentTimeMillis()
        useCase()
        val afterSync = System.currentTimeMillis()

        // Then: Last sync timestamp is set
        val lastSync = fakePreferencesRepository.getLastSyncTimestamp()
        assertTrue("Timestamp should be set", lastSync != null)
        assertTrue("Timestamp should be >= beforeSync", lastSync!! >= beforeSync)
        assertTrue("Timestamp should be <= afterSync", lastSync <= afterSync)
    }

    // ============================================================
    // Failure Case Tests
    // ============================================================

    @Test
    fun `invoke does not update timestamp on failure`() = runTest {
        // Given: Sync enabled, user signed in, but repository returns error
        fakePreferencesRepository.setSyncEnabled(true)
        fakeAuthRepository.setSignedInUser(testUser)
        fakeSyncRepository.shouldReturnError = true
        fakeSyncRepository.syncError = DataError.Sync.NETWORK_ERROR

        // When: Invoking use case (fails)
        val result = useCase()

        // Then: Returns error
        assertTrue(result is Result.Error)
        assertEquals(DataError.Sync.NETWORK_ERROR, (result as Result.Error).error)

        // And: Timestamp not updated
        assertNull(fakePreferencesRepository.getLastSyncTimestamp())
    }

    @Test
    fun `invoke returns repository error on sync failure`() = runTest {
        // Given: Sync enabled, user signed in, repository configured to fail
        fakePreferencesRepository.setSyncEnabled(true)
        fakeAuthRepository.setSignedInUser(testUser)
        fakeSyncRepository.shouldReturnError = true
        fakeSyncRepository.syncError = DataError.Sync.QUOTA_EXCEEDED

        // When: Invoking use case
        val result = useCase()

        // Then: Returns the specific error from repository
        assertTrue(result is Result.Error)
        assertEquals(DataError.Sync.QUOTA_EXCEEDED, (result as Result.Error).error)
    }
}
