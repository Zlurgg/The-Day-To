package uk.co.zlurgg.thedayto.core.domain.usecases.notifications

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakeNotificationSettingsRepository
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID
import uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings

class GetNotificationSettingsUseCaseTest {

    private lateinit var useCase: GetNotificationSettingsUseCase
    private lateinit var settingsRepository: FakeNotificationSettingsRepository
    private lateinit var authRepository: FakeAuthRepository

    private val testUser = UserData(
        userId = "test_user_123",
        username = "Test User",
        profilePictureUrl = "https://example.com/profile.jpg",
    )

    @Before
    fun setup() {
        settingsRepository = FakeNotificationSettingsRepository()
        authRepository = FakeAuthRepository()
        useCase = GetNotificationSettingsUseCase(settingsRepository, authRepository)
    }

    @Test
    fun `invoke returns settings for signed-in user`() = runTest {
        // Given: User is signed in with saved settings
        authRepository.setSignedInUser(testUser)
        val savedSettings = NotificationSettings(enabled = true, hour = 14, minute = 30)
        settingsRepository.setSettings(testUser.userId, savedSettings)

        // When: Getting settings
        val result = useCase()

        // Then: User's settings returned
        assertTrue(result is Result.Success)
        val settings = (result as Result.Success).data
        assertEquals(true, settings.enabled)
        assertEquals(14, settings.hour)
        assertEquals(30, settings.minute)
    }

    @Test
    fun `invoke returns settings for anonymous user when not signed in`() = runTest {
        // Given: No user signed in, but anonymous settings exist
        authRepository.setSignedInUser(null)
        val savedSettings = NotificationSettings(enabled = true, hour = 8, minute = 0)
        settingsRepository.setSettings(ANONYMOUS_USER_ID, savedSettings)

        // When: Getting settings
        val result = useCase()

        // Then: Anonymous user's settings returned
        assertTrue(result is Result.Success)
        val settings = (result as Result.Success).data
        assertEquals(true, settings.enabled)
        assertEquals(8, settings.hour)
        assertEquals(0, settings.minute)
    }

    @Test
    fun `invoke returns default settings when repository returns null`() = runTest {
        // Given: User signed in but no settings saved
        authRepository.setSignedInUser(testUser)
        // settingsRepository has no settings for this user

        // When: Getting settings
        val result = useCase()

        // Then: Default settings returned (disabled, 9:00 AM)
        assertTrue(result is Result.Success)
        val settings = (result as Result.Success).data
        assertEquals(false, settings.enabled)
        assertEquals(9, settings.hour)
        assertEquals(0, settings.minute)
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given: Repository configured to fail
        settingsRepository.shouldReturnError = true
        settingsRepository.errorToReturn = DataError.Local.DATABASE_ERROR
        authRepository.setSignedInUser(testUser)

        // When: Getting settings
        val result = useCase()

        // Then: Error returned
        assertTrue(result is Result.Error)
        assertEquals(DataError.Local.DATABASE_ERROR, (result as Result.Error).error)
    }
}
