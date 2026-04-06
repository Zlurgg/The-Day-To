package uk.co.zlurgg.thedayto.auth.domain.usecases

import app.cash.turbine.test
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.repository.LocalDataClearer
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakeSyncRepository
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler

/**
 * Unit tests for DeleteAccountUseCase.
 *
 * Tests cover:
 * - Full deletion flow emits correct progress states
 * - Not signed in returns failed state
 * - Remote data deletion failure stops flow
 * - Account deletion failure with REQUIRES_RECENT_LOGIN emits RequiresReAuth
 * - Clears local data after successful deletion
 */
class DeleteAccountUseCaseTest {

    private lateinit var useCase: DeleteAccountUseCase
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeSyncRepository: FakeSyncRepository
    private lateinit var mockLocalDataClearer: LocalDataClearer
    private lateinit var mockSyncScheduler: SyncScheduler

    private val testUser = UserData(
        userId = "test_user_123",
        username = "Test User",
        profilePictureUrl = null
    )

    @Before
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        fakeSyncRepository = FakeSyncRepository()
        mockLocalDataClearer = mockk(relaxed = true)
        mockSyncScheduler = mockk(relaxed = true)

        useCase = DeleteAccountUseCase(
            authRepository = fakeAuthRepository,
            syncRepository = fakeSyncRepository,
            localDataClearer = mockLocalDataClearer,
            syncScheduler = mockSyncScheduler
        )
    }

    @Test
    fun `invoke emits full progress sequence on successful deletion`() = runTest {
        // Given: User is signed in, all operations succeed
        fakeAuthRepository.setSignedInUser(testUser)
        fakeSyncRepository.shouldReturnError = false
        fakeAuthRepository.shouldReturnError = false

        // When/Then: Collecting flow emits correct sequence
        useCase().test {
            assertEquals(DeletionProgress.Starting, awaitItem())
            assertEquals(DeletionProgress.CancellingSync, awaitItem())
            assertEquals(DeletionProgress.DeletingRemote, awaitItem())
            assertEquals(DeletionProgress.DeletingAccount, awaitItem())
            assertEquals(DeletionProgress.ClearingLocal, awaitItem())
            assertEquals(DeletionProgress.Complete, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke cancels sync workers`() = runTest {
        // Given: User is signed in
        fakeAuthRepository.setSignedInUser(testUser)

        // When: Deleting account
        useCase().test {
            cancelAndConsumeRemainingEvents()
        }

        // Then: Sync scheduler was cancelled
        verify { mockSyncScheduler.cancelAllSync() }
    }

    @Test
    fun `invoke clears remote data for user`() = runTest {
        // Given: User is signed in
        fakeAuthRepository.setSignedInUser(testUser)

        // When: Deleting account
        useCase().test {
            cancelAndConsumeRemainingEvents()
        }

        // Then: Remote data was cleared
        assertTrue(fakeSyncRepository.clearRemoteDataCalled)
        assertEquals(testUser.userId, fakeSyncRepository.lastUserId)
    }

    @Test
    fun `invoke clears local data on success`() = runTest {
        // Given: User is signed in, all operations succeed
        fakeAuthRepository.setSignedInUser(testUser)

        // When: Deleting account
        useCase().test {
            cancelAndConsumeRemainingEvents()
        }

        // Then: Local data was cleared
        coVerify { mockLocalDataClearer.clearAllLocalData() }
        coVerify { mockLocalDataClearer.clearPreferences() }
    }

    @Test
    fun `invoke emits Failed when not signed in`() = runTest {
        // Given: No user signed in
        fakeAuthRepository.setSignedInUser(null)

        // When/Then: Emits failed state
        useCase().test {
            assertEquals(DeletionProgress.Starting, awaitItem())
            val failed = awaitItem()
            assertTrue(failed is DeletionProgress.Failed)
            assertEquals("You must be signed in.", (failed as DeletionProgress.Failed).message)
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits Failed when remote data deletion fails`() = runTest {
        // Given: User is signed in, remote deletion fails
        fakeAuthRepository.setSignedInUser(testUser)
        fakeSyncRepository.shouldReturnError = true
        fakeSyncRepository.syncError = DataError.Sync.NETWORK_ERROR

        // When/Then: Emits failed state after remote deletion
        useCase().test {
            assertEquals(DeletionProgress.Starting, awaitItem())
            assertEquals(DeletionProgress.CancellingSync, awaitItem())
            assertEquals(DeletionProgress.DeletingRemote, awaitItem())
            val failed = awaitItem()
            assertTrue(failed is DeletionProgress.Failed)
            awaitComplete()
        }
    }

    @Test
    fun `invoke emits RequiresReAuth when account deletion needs recent login`() = runTest {
        // Given: User is signed in, remote succeeds, account deletion needs re-auth
        fakeAuthRepository.setSignedInUser(testUser)
        fakeSyncRepository.shouldReturnError = false
        fakeAuthRepository.deleteAccountError = DataError.Auth.REQUIRES_RECENT_LOGIN

        // When/Then: Emits RequiresReAuth state
        useCase().test {
            assertEquals(DeletionProgress.Starting, awaitItem())
            assertEquals(DeletionProgress.CancellingSync, awaitItem())
            assertEquals(DeletionProgress.DeletingRemote, awaitItem())
            assertEquals(DeletionProgress.DeletingAccount, awaitItem())
            assertEquals(DeletionProgress.RequiresReAuth, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `invoke does not clear local data when remote deletion fails`() = runTest {
        // Given: User is signed in, remote deletion fails
        fakeAuthRepository.setSignedInUser(testUser)
        fakeSyncRepository.shouldReturnError = true

        // When: Deleting account
        useCase().test {
            cancelAndConsumeRemainingEvents()
        }

        // Then: Local data was NOT cleared
        coVerify(exactly = 0) { mockLocalDataClearer.clearAllLocalData() }
        coVerify(exactly = 0) { mockLocalDataClearer.clearPreferences() }
    }
}
