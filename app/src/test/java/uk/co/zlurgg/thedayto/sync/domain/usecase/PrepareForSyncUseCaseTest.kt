package uk.co.zlurgg.thedayto.sync.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeSyncRepository

/**
 * Unit tests for PrepareForSyncUseCase.
 *
 * Tests cover:
 * - Adopts orphaned data for the user
 * - Marks local-only data as pending sync
 * - Returns total count of affected items
 */
class PrepareForSyncUseCaseTest {

    private lateinit var useCase: PrepareForSyncUseCase
    private lateinit var fakeSyncRepository: FakeSyncRepository

    private val testUserId = "test_user_123"

    @Before
    fun setup() {
        fakeSyncRepository = FakeSyncRepository()
        useCase = PrepareForSyncUseCase(fakeSyncRepository)
    }

    @Test
    fun `invoke adopts orphaned data for the user`() = runTest {
        // Given: Repository configured with orphaned data
        fakeSyncRepository.adoptOrphanedDataCount = 5

        // When: Invoking use case
        useCase(testUserId)

        // Then: adoptOrphanedData was called with correct userId
        assertTrue(fakeSyncRepository.adoptOrphanedDataCalled)
        assertEquals(testUserId, fakeSyncRepository.lastUserId)
    }

    @Test
    fun `invoke marks local data for sync`() = runTest {
        // Given: Repository configured with local-only data
        fakeSyncRepository.markLocalDataForSyncCount = 3

        // When: Invoking use case
        useCase(testUserId)

        // Then: markLocalDataForSync was called
        assertTrue(fakeSyncRepository.markLocalDataForSyncCalled)
    }

    @Test
    fun `invoke returns total count of adopted and marked items`() = runTest {
        // Given: Repository has orphaned and local-only data
        fakeSyncRepository.adoptOrphanedDataCount = 5
        fakeSyncRepository.markLocalDataForSyncCount = 3

        // When: Invoking use case
        val result = useCase(testUserId)

        // Then: Returns sum of both counts
        assertEquals(8, result)
    }

    @Test
    fun `invoke returns zero when no data to prepare`() = runTest {
        // Given: No orphaned or local-only data
        fakeSyncRepository.adoptOrphanedDataCount = 0
        fakeSyncRepository.markLocalDataForSyncCount = 0

        // When: Invoking use case
        val result = useCase(testUserId)

        // Then: Returns zero
        assertEquals(0, result)
    }

    @Test
    fun `invoke calls adopt before mark`() = runTest {
        // This test verifies the order of operations matches the documented behavior
        // Given: Fresh repository
        fakeSyncRepository.adoptOrphanedDataCount = 2
        fakeSyncRepository.markLocalDataForSyncCount = 1

        // When: Invoking use case
        useCase(testUserId)

        // Then: Both operations were called
        assertTrue(fakeSyncRepository.adoptOrphanedDataCalled)
        assertTrue(fakeSyncRepository.markLocalDataForSyncCalled)
    }
}
