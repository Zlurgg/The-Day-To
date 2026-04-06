package uk.co.zlurgg.thedayto.auth.domain.usecases

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository

/**
 * Unit tests for CheckWelcomeDialogSeenUseCase.
 *
 * Tests cover:
 * - Returns true when dialog has been seen
 * - Returns false when dialog has not been seen
 */
class CheckWelcomeDialogSeenUseCaseTest {

    private lateinit var useCase: CheckWelcomeDialogSeenUseCase
    private lateinit var fakePreferencesRepository: FakePreferencesRepository

    @Before
    fun setup() {
        fakePreferencesRepository = FakePreferencesRepository()
        useCase = CheckWelcomeDialogSeenUseCase(fakePreferencesRepository)
    }

    @Test
    fun `invoke returns true when welcome dialog has been seen`() = runTest {
        // Given: Welcome dialog has been seen
        fakePreferencesRepository.markWelcomeDialogSeen()

        // When: Checking if seen
        val result = useCase()

        // Then: Returns true
        assertTrue(result)
    }

    @Test
    fun `invoke returns false when welcome dialog has not been seen`() = runTest {
        // Given: Fresh preferences (dialog not seen)
        // FakePreferencesRepository defaults to not seen

        // When: Checking if seen
        val result = useCase()

        // Then: Returns false
        assertFalse(result)
    }
}
