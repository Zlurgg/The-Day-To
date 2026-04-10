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
import uk.co.zlurgg.thedayto.fake.FakeNotificationScheduler
import uk.co.zlurgg.thedayto.fake.FakeNotificationSettingsRepository
import uk.co.zlurgg.thedayto.notification.data.migration.NotificationMigrationService.Companion.ANONYMOUS_USER_ID

class SaveNotificationSettingsUseCaseTest {

    private lateinit var useCase: SaveNotificationSettingsUseCase
    private lateinit var settingsRepository: FakeNotificationSettingsRepository
    private lateinit var scheduler: FakeNotificationScheduler
    private lateinit var authRepository: FakeAuthRepository

    private val testUser = UserData(
        userId = "test_user_123",
        username = "Test User",
        profilePictureUrl = "https://example.com/profile.jpg",
    )

    @Before
    fun setup() {
        settingsRepository = FakeNotificationSettingsRepository()
        scheduler = FakeNotificationScheduler()
        authRepository = FakeAuthRepository()
        useCase = SaveNotificationSettingsUseCase(settingsRepository, scheduler, authRepository)
    }

    @Test
    fun `invoke saves settings for signed-in user`() = runTest {
        // Given: User is signed in
        authRepository.setSignedInUser(testUser)

        // When: Saving notification settings
        val result = useCase(enabled = true, hour = 14, minute = 30)

        // Then: Settings saved for user's ID
        assertTrue(result is Result.Success)
        val savedSettings = settingsRepository.getSettingsDirectly(testUser.userId)
        assertEquals(true, savedSettings?.enabled)
        assertEquals(14, savedSettings?.hour)
        assertEquals(30, savedSettings?.minute)
    }

    @Test
    fun `invoke saves settings for anonymous user when not signed in`() = runTest {
        // Given: No user signed in
        authRepository.setSignedInUser(null)

        // When: Saving notification settings
        val result = useCase(enabled = true, hour = 9, minute = 0)

        // Then: Settings saved for anonymous user ID
        assertTrue(result is Result.Success)
        val savedSettings = settingsRepository.getSettingsDirectly(ANONYMOUS_USER_ID)
        assertEquals(true, savedSettings?.enabled)
        assertEquals(9, savedSettings?.hour)
        assertEquals(0, savedSettings?.minute)
    }

    @Test
    fun `invoke schedules notification when enabled`() = runTest {
        // Given: User signed in
        authRepository.setSignedInUser(testUser)

        // When: Saving with enabled = true
        useCase(enabled = true, hour = 18, minute = 45)

        // Then: Notification scheduled at correct time
        assertTrue(scheduler.updateNotificationTimeCalled)
        assertTrue(scheduler.isScheduledAt(18, 45))
    }

    @Test
    fun `invoke cancels notification when disabled`() = runTest {
        // Given: User signed in
        authRepository.setSignedInUser(testUser)

        // When: Saving with enabled = false
        useCase(enabled = false, hour = 9, minute = 0)

        // Then: Notifications cancelled
        assertTrue(scheduler.cancelNotificationsCalled)
    }

    @Test
    fun `invoke returns error when repository fails`() = runTest {
        // Given: Repository configured to fail
        settingsRepository.shouldReturnError = true
        settingsRepository.errorToReturn = DataError.Local.DATABASE_ERROR
        authRepository.setSignedInUser(testUser)

        // When: Saving settings
        val result = useCase(enabled = true, hour = 9, minute = 0)

        // Then: Error returned
        assertTrue(result is Result.Error)
        assertEquals(DataError.Local.DATABASE_ERROR, (result as Result.Error).error)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws on invalid hour`() = runTest {
        // When: Saving with invalid hour (24)
        useCase(enabled = true, hour = 24, minute = 0)

        // Then: IllegalArgumentException thrown
    }

    @Test(expected = IllegalArgumentException::class)
    fun `invoke throws on invalid minute`() = runTest {
        // When: Saving with invalid minute (60)
        useCase(enabled = true, hour = 9, minute = 60)

        // Then: IllegalArgumentException thrown
    }
}
