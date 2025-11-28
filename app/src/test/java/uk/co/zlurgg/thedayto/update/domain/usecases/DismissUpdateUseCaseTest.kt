package uk.co.zlurgg.thedayto.update.domain.usecases

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository

/**
 * Unit tests for DismissUpdateUseCase.
 *
 * Tests that version dismissal is persisted correctly.
 */
class DismissUpdateUseCaseTest {

    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var dismissUpdateUseCase: DismissUpdateUseCase

    @Before
    fun setup() {
        fakePreferencesRepository = FakePreferencesRepository()
        dismissUpdateUseCase = DismissUpdateUseCase(fakePreferencesRepository)
    }

    @Test
    fun `invoke - stores dismissed version in preferences`() = runTest {
        // Given: No dismissed version initially
        assertEquals(null, fakePreferencesRepository.getDismissedVersion())

        // When: Dismissing version 1.0.4
        dismissUpdateUseCase("1.0.4")

        // Then: Version should be stored
        assertEquals("1.0.4", fakePreferencesRepository.getDismissedVersion())
    }

    @Test
    fun `invoke - overwrites previously dismissed version`() = runTest {
        // Given: Version 1.0.3 already dismissed
        fakePreferencesRepository.setDismissedVersionForTest("1.0.3")

        // When: Dismissing version 1.0.4
        dismissUpdateUseCase("1.0.4")

        // Then: New version should be stored
        assertEquals("1.0.4", fakePreferencesRepository.getDismissedVersion())
    }

    @Test
    fun `invoke - handles version with v prefix`() = runTest {
        // When: Dismissing version with v prefix
        dismissUpdateUseCase("v1.0.4")

        // Then: Version should be stored as-is
        assertEquals("v1.0.4", fakePreferencesRepository.getDismissedVersion())
    }
}
