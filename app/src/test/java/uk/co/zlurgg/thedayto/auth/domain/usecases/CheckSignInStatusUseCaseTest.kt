package uk.co.zlurgg.thedayto.auth.domain.usecases

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakeAuthStateRepository

/**
 * Unit tests for CheckSignInStatusUseCase.
 *
 * Tests cover:
 * - Returns true when both state and user exist
 * - Returns false when state is false
 * - Returns false when user is null
 * - Returns false when both are missing
 */
class CheckSignInStatusUseCaseTest {

    private lateinit var useCase: CheckSignInStatusUseCase
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeAuthStateRepository: FakeAuthStateRepository

    private val testUser = UserData(
        userId = "test_user_123",
        username = "Test User",
        profilePictureUrl = null,
    )

    @Before
    fun setup() {
        fakeAuthRepository = FakeAuthRepository()
        fakeAuthStateRepository = FakeAuthStateRepository()
        useCase = CheckSignInStatusUseCase(fakeAuthRepository, fakeAuthStateRepository)
    }

    @Test
    fun `invoke returns true when state is true and user exists`() {
        // Given: State is true and user exists
        fakeAuthStateRepository.setSignedInState(true)
        fakeAuthRepository.setSignedInUser(testUser)

        // When: Checking status
        val result = useCase()

        // Then: Returns true
        assertTrue(result)
    }

    @Test
    fun `invoke returns false when state is false`() {
        // Given: State is false but user exists
        fakeAuthStateRepository.setSignedInState(false)
        fakeAuthRepository.setSignedInUser(testUser)

        // When: Checking status
        val result = useCase()

        // Then: Returns false
        assertFalse(result)
    }

    @Test
    fun `invoke returns false when user is null`() {
        // Given: State is true but user is null
        fakeAuthStateRepository.setSignedInState(true)
        fakeAuthRepository.setSignedInUser(null)

        // When: Checking status
        val result = useCase()

        // Then: Returns false
        assertFalse(result)
    }

    @Test
    fun `invoke returns false when both state and user are missing`() {
        // Given: State is false and user is null
        fakeAuthStateRepository.setSignedInState(false)
        fakeAuthRepository.setSignedInUser(null)

        // When: Checking status
        val result = useCase()

        // Then: Returns false
        assertFalse(result)
    }
}
