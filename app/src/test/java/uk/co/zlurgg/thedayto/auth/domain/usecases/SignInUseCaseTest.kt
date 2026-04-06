package uk.co.zlurgg.thedayto.auth.domain.usecases

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.auth.domain.model.GoogleCredential
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakeAuthStateRepository

/**
 * Unit tests for SignInUseCase.
 *
 * Tests cover:
 * - Successful sign-in returns user data and updates state
 * - Failed sign-in returns error and doesn't update state
 */
class SignInUseCaseTest {

    private lateinit var useCase: SignInUseCase
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeAuthStateRepository: FakeAuthStateRepository

    // Must match the hardcoded return value in FakeAuthRepository.signIn()
    private val testUser = UserData(
        userId = "test_user_123",
        username = "Test User",
        profilePictureUrl = "https://example.com/profile.jpg"
    )

    private val mockCredentialProvider: suspend () -> Result<GoogleCredential, DataError.Auth> = {
        Result.Success(GoogleCredential("mock_id_token"))
    }

    @Before
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        fakeAuthStateRepository = FakeAuthStateRepository()
        useCase = SignInUseCase(fakeAuthRepository, fakeAuthStateRepository)
    }

    @Test
    fun `invoke returns user data on successful sign-in`() = runTest {
        // Given: Auth configured for success
        fakeAuthRepository.shouldReturnError = false
        fakeAuthRepository.setSignedInUser(testUser)

        // When: Signing in
        val result = useCase(mockCredentialProvider)

        // Then: Returns success with user data
        assertTrue(result is Result.Success)
        assertEquals(testUser, (result as Result.Success).data)
    }

    @Test
    fun `invoke updates sign-in state on success`() = runTest {
        // Given: Auth configured for success, state is false
        fakeAuthRepository.shouldReturnError = false
        fakeAuthStateRepository.setSignedInState(false)

        // When: Signing in
        useCase(mockCredentialProvider)

        // Then: Sign-in state is updated to true
        assertTrue(fakeAuthStateRepository.getSignedInState())
    }

    @Test
    fun `invoke returns error on failed sign-in`() = runTest {
        // Given: Auth configured for failure
        fakeAuthRepository.shouldReturnError = true

        // When: Signing in
        val result = useCase(mockCredentialProvider)

        // Then: Returns error
        assertTrue(result is Result.Error)
    }

    @Test
    fun `invoke does not update state on failed sign-in`() = runTest {
        // Given: Auth configured for failure, state is false
        fakeAuthRepository.shouldReturnError = true
        fakeAuthStateRepository.setSignedInState(false)

        // When: Signing in
        useCase(mockCredentialProvider)

        // Then: Sign-in state remains false
        assertFalse(fakeAuthStateRepository.getSignedInState())
    }
}
