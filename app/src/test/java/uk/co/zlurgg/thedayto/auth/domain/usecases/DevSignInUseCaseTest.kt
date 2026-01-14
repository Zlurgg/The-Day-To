package uk.co.zlurgg.thedayto.auth.domain.usecases

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.fake.FakeAuthStateRepository
import uk.co.zlurgg.thedayto.fake.FakeDevAuthService

/**
 * Unit tests for DevSignInUseCase.
 *
 * Tests cover:
 * - Successful dev sign-in updates auth state
 * - Failed dev sign-in returns error without updating state
 * - isAvailable() delegates to service
 */
class DevSignInUseCaseTest {

    private lateinit var fakeDevAuthService: FakeDevAuthService
    private lateinit var fakeAuthStateRepository: FakeAuthStateRepository
    private lateinit var devSignInUseCase: DevSignInUseCase

    @Before
    fun setup() {
        fakeDevAuthService = FakeDevAuthService()
        fakeAuthStateRepository = FakeAuthStateRepository()
        devSignInUseCase = DevSignInUseCase(
            devAuthService = fakeDevAuthService,
            authStateRepository = fakeAuthStateRepository
        )
    }

    @Test
    fun `invoke - successful sign-in returns user data and updates auth state`() = runTest {
        // Given: Dev auth service configured for success
        fakeDevAuthService.shouldReturnError = false

        // When: Signing in with test credentials
        val result = devSignInUseCase("test@example.com", "password123")

        // Then: Result should be success with user data
        assertTrue("Result should be success", result is Result.Success)
        val userData = (result as Result.Success).data
        assertEquals("dev_test_user", userData.userId)
        assertEquals("Dev Test User", userData.username)

        // And: Auth state should be updated
        assertTrue("Auth state should be signed in", fakeAuthStateRepository.getSignedInState())
    }

    @Test
    fun `invoke - failed sign-in returns error without updating auth state`() = runTest {
        // Given: Dev auth service configured for failure
        fakeDevAuthService.shouldReturnError = true
        fakeDevAuthService.authError = DataError.Auth.FAILED

        // When: Signing in with test credentials
        val result = devSignInUseCase("test@example.com", "wrong_password")

        // Then: Result should be error
        assertTrue("Result should be error", result is Result.Error)
        assertEquals(DataError.Auth.FAILED, (result as Result.Error).error)

        // And: Auth state should NOT be updated
        assertFalse("Auth state should not be signed in", fakeAuthStateRepository.getSignedInState())
    }

    @Test
    fun `invoke - network error returns appropriate error type`() = runTest {
        // Given: Dev auth service configured for network error
        fakeDevAuthService.shouldReturnError = true
        fakeDevAuthService.authError = DataError.Auth.NETWORK_ERROR

        // When: Signing in
        val result = devSignInUseCase("test@example.com", "password123")

        // Then: Result should be network error
        assertTrue("Result should be error", result is Result.Error)
        assertEquals(DataError.Auth.NETWORK_ERROR, (result as Result.Error).error)
    }

    @Test
    fun `isAvailable - returns true when service is available`() {
        // Given: Dev auth service is available
        fakeDevAuthService.isDevAvailable = true

        // When/Then: UseCase reports available
        assertTrue("Should be available", devSignInUseCase.isAvailable())
    }

    @Test
    fun `isAvailable - returns false when service is not available`() {
        // Given: Dev auth service is not available
        fakeDevAuthService.isDevAvailable = false

        // When/Then: UseCase reports not available
        assertFalse("Should not be available", devSignInUseCase.isAvailable())
    }
}
