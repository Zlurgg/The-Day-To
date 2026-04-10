package uk.co.zlurgg.thedayto.auth.domain.usecases

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakeAuthStateRepository

/**
 * Unit tests for SignOutUseCase.
 *
 * Tests cover:
 * - Sign-out calls repository
 * - Sign-out clears sign-in state
 */
class SignOutUseCaseTest {

    private lateinit var useCase: SignOutUseCase
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
        useCase = SignOutUseCase(fakeAuthRepository, fakeAuthStateRepository)
    }

    @Test
    fun `invoke calls repository signOut`() = runTest {
        // Given: User is signed in
        fakeAuthRepository.setSignedInUser(testUser)

        // When: Signing out
        useCase()

        // Then: Repository signOut was called
        assertTrue(fakeAuthRepository.signOutCalled)
    }

    @Test
    fun `invoke clears sign-in state`() = runTest {
        // Given: User is signed in, state is true
        fakeAuthRepository.setSignedInUser(testUser)
        fakeAuthStateRepository.setSignedInState(true)

        // When: Signing out
        useCase()

        // Then: Sign-in state is false
        assertFalse(fakeAuthStateRepository.getSignedInState())
    }

    @Test
    fun `invoke works when already signed out`() = runTest {
        // Given: No user signed in, state is false
        fakeAuthRepository.setSignedInUser(null)
        fakeAuthStateRepository.setSignedInState(false)

        // When: Signing out (should not throw)
        useCase()

        // Then: State remains false
        assertFalse(fakeAuthStateRepository.getSignedInState())
    }
}
