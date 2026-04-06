package uk.co.zlurgg.thedayto.auth.domain.usecases

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository

/**
 * Unit tests for MarkWelcomeDialogSeenUseCase.
 *
 * Tests cover:
 * - Marks the welcome dialog as seen
 */
class MarkWelcomeDialogSeenUseCaseTest {

    private lateinit var useCase: MarkWelcomeDialogSeenUseCase
    private lateinit var fakePreferencesRepository: FakePreferencesRepository

    @Before
    fun setup() {
        fakePreferencesRepository = FakePreferencesRepository()
        useCase = MarkWelcomeDialogSeenUseCase(fakePreferencesRepository)
    }

    @Test
    fun `invoke marks welcome dialog as seen`() = runTest {
        // Given: Dialog not yet seen
        assertTrue(!fakePreferencesRepository.hasSeenWelcomeDialog())

        // When: Marking as seen
        useCase()

        // Then: Dialog is marked as seen
        assertTrue(fakePreferencesRepository.hasSeenWelcomeDialog())
    }

    @Test
    fun `invoke is idempotent`() = runTest {
        // Given: Dialog already seen
        fakePreferencesRepository.markWelcomeDialogSeen()

        // When: Marking as seen again
        useCase()

        // Then: Still marked as seen (no error)
        assertTrue(fakePreferencesRepository.hasSeenWelcomeDialog())
    }
}
