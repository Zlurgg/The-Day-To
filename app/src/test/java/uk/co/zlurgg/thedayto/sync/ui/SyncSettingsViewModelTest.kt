package uk.co.zlurgg.thedayto.sync.ui

import app.cash.turbine.test
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
import uk.co.zlurgg.thedayto.auth.domain.model.GoogleCredential
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.usecases.AccountUseCases
import uk.co.zlurgg.thedayto.auth.domain.usecases.DeleteAccountUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.DevSignInUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.GetSignedInUserUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.ReauthenticateUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignOutUseCase
import uk.co.zlurgg.thedayto.core.domain.repository.LocalDataClearer
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakeAuthStateRepository
import uk.co.zlurgg.thedayto.fake.FakeDevAuthService
import uk.co.zlurgg.thedayto.fake.FakeNotificationScheduler
import uk.co.zlurgg.thedayto.fake.FakeNotificationSettingsRepository
import uk.co.zlurgg.thedayto.fake.FakeNotificationSyncService
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository
import uk.co.zlurgg.thedayto.fake.FakeSyncRepository
import uk.co.zlurgg.thedayto.notification.domain.usecase.NotificationAuthUseCase
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler
import uk.co.zlurgg.thedayto.sync.domain.model.SyncResult
import uk.co.zlurgg.thedayto.sync.domain.model.SyncState
import uk.co.zlurgg.thedayto.sync.domain.usecase.GetLastSyncTimestampUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.ObserveSyncStateUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.PerformSyncUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.PrepareForSyncUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.SetSyncEnabledUseCase
import uk.co.zlurgg.thedayto.sync.domain.usecase.SyncUseCases

/**
 * Unit tests for SyncSettingsViewModel.
 *
 * Tests cover:
 * - Initial state loading
 * - Sign-in success triggers sync preparation and scheduling
 * - Sign-in error shows message
 * - Sign-out clears data and cancels sync
 * - Sync now requests immediate sync
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SyncSettingsViewModelTest {

    private lateinit var viewModel: SyncSettingsViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeAuthStateRepository: FakeAuthStateRepository
    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var fakeSyncRepository: FakeSyncRepository
    private lateinit var mockSyncScheduler: SyncScheduler
    private lateinit var fakeDevAuthService: FakeDevAuthService
    private lateinit var fakeNotificationSettingsRepository: FakeNotificationSettingsRepository
    private lateinit var fakeNotificationScheduler: FakeNotificationScheduler
    private lateinit var fakeNotificationSyncService: FakeNotificationSyncService
    private lateinit var mockLocalDataClearer: LocalDataClearer

    private val testDispatcher = UnconfinedTestDispatcher()

    private val testUser = UserData(
        userId = "test_user_123",
        username = "Test User",
        profilePictureUrl = null
    )

    // Mock credential provider for tests
    private val mockCredentialProvider: CredentialProvider = {
        Result.Success(GoogleCredential("mock_id_token"))
    }

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        fakeAuthRepository = FakeAuthRepository()
        fakeAuthStateRepository = FakeAuthStateRepository()
        fakePreferencesRepository = FakePreferencesRepository()
        fakeSyncRepository = FakeSyncRepository()
        mockSyncScheduler = mockk(relaxed = true)
        fakeDevAuthService = FakeDevAuthService()
        fakeNotificationSettingsRepository = FakeNotificationSettingsRepository()
        fakeNotificationScheduler = FakeNotificationScheduler()
        fakeNotificationSyncService = FakeNotificationSyncService()
        mockLocalDataClearer = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(includeDevSignIn: Boolean = false): SyncSettingsViewModel {
        val devSignInUseCase = if (includeDevSignIn) {
            DevSignInUseCase(fakeDevAuthService, fakeAuthStateRepository)
        } else {
            null
        }

        val syncUseCases = SyncUseCases(
            performSync = PerformSyncUseCase(
                authRepository = fakeAuthRepository,
                preferencesRepository = fakePreferencesRepository,
                syncRepository = fakeSyncRepository
            ),
            observeSyncState = ObserveSyncStateUseCase(fakeSyncRepository),
            getLastSyncTimestamp = GetLastSyncTimestampUseCase(fakePreferencesRepository),
            prepareForSync = PrepareForSyncUseCase(fakeSyncRepository),
            setSyncEnabled = SetSyncEnabledUseCase(fakePreferencesRepository)
        )

        val accountUseCases = AccountUseCases(
            getSignedInUser = GetSignedInUserUseCase(fakeAuthRepository),
            signIn = SignInUseCase(fakeAuthRepository, fakeAuthStateRepository),
            signOut = SignOutUseCase(fakeAuthRepository, fakeAuthStateRepository),
            reauthenticate = ReauthenticateUseCase(fakeAuthRepository),
            deleteAccount = DeleteAccountUseCase(
                authRepository = fakeAuthRepository,
                syncRepository = fakeSyncRepository,
                localDataClearer = mockLocalDataClearer,
                syncScheduler = mockSyncScheduler
            ),
            devSignIn = devSignInUseCase
        )

        return SyncSettingsViewModel(
            syncUseCases = syncUseCases,
            accountUseCases = accountUseCases,
            syncScheduler = mockSyncScheduler,
            notificationAuthUseCase = NotificationAuthUseCase(
                settingsRepository = fakeNotificationSettingsRepository,
                notificationScheduler = fakeNotificationScheduler,
                syncService = fakeNotificationSyncService
            )
        )
    }

    // ============================================================
    // Initialization Tests
    // ============================================================

    @Test
    fun `init loads user and sync state correctly when signed in`() = runTest {
        // Given: User is signed in with last sync timestamp
        fakeAuthRepository.setSignedInUser(testUser)
        fakePreferencesRepository.setLastSyncTimestamp(1000L)

        // When: ViewModel initialized
        viewModel = createViewModel()

        // Then: State reflects signed-in user
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("User should be signed in", state.isUserSignedIn)
            assertEquals("Test User", state.userEmail)
            assertEquals(1000L, state.lastSyncTimestamp)
            assertFalse("Should not be loading", state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init loads state correctly when not signed in`() = runTest {
        // Given: No user signed in
        fakeAuthRepository.setSignedInUser(null)

        // When: ViewModel initialized
        viewModel = createViewModel()

        // Then: State reflects no user
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("User should not be signed in", state.isUserSignedIn)
            assertNull("Email should be null", state.userEmail)
            assertFalse("Should not be loading", state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init shows dev sign-in available when configured`() = runTest {
        // Given: Dev sign-in available
        fakeDevAuthService.isDevAvailable = true

        // When: ViewModel initialized with dev sign-in
        viewModel = createViewModel(includeDevSignIn = true)

        // Then: Dev sign-in is available
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Dev sign-in should be available", state.isDevSignInAvailable)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Sign-In Success Tests
    // ============================================================

    @Test
    fun `signIn success updates state and triggers sync preparation`() = runTest {
        // Given: Auth configured for success
        fakeAuthRepository.shouldReturnError = false
        viewModel = createViewModel()

        // When: User signs in
        viewModel.signIn(mockCredentialProvider)

        // Then: State reflects successful sign-in
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("User should be signed in", state.isUserSignedIn)
            assertFalse("Should not be signing in", state.isSigningIn)
            assertNull("Error should be null", state.error)
            cancelAndIgnoreRemainingEvents()
        }

        // And: Sync preparation was called (adopts orphaned data, marks for sync)
        assertTrue("adoptOrphanedData should be called", fakeSyncRepository.adoptOrphanedDataCalled)
        assertTrue("markLocalDataForSync should be called", fakeSyncRepository.markLocalDataForSyncCalled)
    }

    @Test
    fun `signIn success enables sync and starts scheduler`() = runTest {
        // Given: Auth configured for success
        fakeAuthRepository.shouldReturnError = false
        viewModel = createViewModel()

        // When: User signs in
        viewModel.signIn(mockCredentialProvider)

        // Then: Sync is enabled
        assertTrue("Sync should be enabled", fakePreferencesRepository.isSyncEnabled())

        // And: Scheduler started
        verify { mockSyncScheduler.startPeriodicSync() }
        verify { mockSyncScheduler.requestImmediateSync() }
    }

    // ============================================================
    // Sign-In Error Tests
    // ============================================================

    @Test
    fun `signIn error updates state with error message`() = runTest {
        // Given: Auth configured for failure
        fakeAuthRepository.shouldReturnError = true
        viewModel = createViewModel()

        // When: User signs in
        viewModel.signIn(mockCredentialProvider)

        // Then: State reflects error
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("User should not be signed in", state.isUserSignedIn)
            assertFalse("Should not be signing in", state.isSigningIn)
            assertTrue("Error should be set", state.error != null)
            cancelAndIgnoreRemainingEvents()
        }

        // And: Sync not started
        verify(exactly = 0) { mockSyncScheduler.startPeriodicSync() }
    }

    // ============================================================
    // Sign-Out Tests
    // ============================================================

    @Test
    fun `signOut cancels sync and keeps local data`() = runTest {
        // Given: User is signed in
        fakeAuthRepository.setSignedInUser(testUser)
        fakeAuthStateRepository.setSignedInState(true)
        viewModel = createViewModel()

        // When: User signs out
        viewModel.signOut()

        // Then: Sync cancelled
        verify { mockSyncScheduler.cancelAllSync() }

        // And: Sync disabled
        assertFalse("Sync should be disabled", fakePreferencesRepository.isSyncEnabled())

        // And: Local data is preserved (no clearUserData call)
        assertFalse("User data should NOT be cleared", fakeSyncRepository.clearUserDataCalled)
    }

    @Test
    fun `signOut updates state correctly`() = runTest {
        // Given: User is signed in
        fakeAuthRepository.setSignedInUser(testUser)
        fakeAuthStateRepository.setSignedInState(true)
        viewModel = createViewModel()

        // When: User signs out
        viewModel.signOut()

        // Then: State reflects signed out
        viewModel.uiState.test {
            val state = awaitItem()
            assertFalse("User should not be signed in", state.isUserSignedIn)
            assertNull("Email should be null", state.userEmail)
            assertFalse("Should not be loading", state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Sync Now Tests
    // ============================================================

    @Test
    fun `onSyncNowClicked requests immediate sync`() = runTest {
        // Given: ViewModel initialized
        viewModel = createViewModel()

        // When: Sync now clicked
        viewModel.onSyncNowClicked()

        // Then: Immediate sync requested
        verify { mockSyncScheduler.requestImmediateSync() }
    }

    // ============================================================
    // Error Dismissal Tests
    // ============================================================

    @Test
    fun `onErrorDismissed clears error state`() = runTest {
        // Given: ViewModel with error state
        fakeAuthRepository.shouldReturnError = true
        viewModel = createViewModel()
        viewModel.signIn(mockCredentialProvider)

        // Verify error exists
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Error should be set", state.error != null)
            cancelAndIgnoreRemainingEvents()
        }

        // When: Error dismissed
        viewModel.onErrorDismissed()

        // Then: Error cleared
        viewModel.uiState.test {
            val state = awaitItem()
            assertNull("Error should be cleared", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Sync State Observation Tests
    // ============================================================

    @Test
    fun `observes sync state changes and updates UI`() = runTest {
        // Given: ViewModel initialized
        viewModel = createViewModel()

        // When: Sync state changes
        fakeSyncRepository.emitSyncState(SyncState.Syncing(0.5f))

        // Then: UI state updated
        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue("Should be syncing", state.syncState is SyncState.Syncing)
            assertEquals(0.5f, (state.syncState as SyncState.Syncing).progress)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `updates last sync timestamp on sync success`() = runTest {
        // Given: ViewModel initialized with a last sync timestamp
        fakePreferencesRepository.setLastSyncTimestamp(5000L)
        viewModel = createViewModel()

        // When: Sync succeeds
        fakeSyncRepository.emitSyncState(SyncState.Success(SyncResult()))

        // Then: Last sync timestamp is available in state
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(5000L, state.lastSyncTimestamp)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Notification Auth Integration Tests
    // ============================================================

    @Test
    fun `signIn migrates anonymous notification settings to user`() = runTest {
        // Given: Anonymous user has notification settings
        fakeNotificationSettingsRepository.setSettings(
            "anonymous",
            uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings(
                enabled = true,
                hour = 8,
                minute = 30
            )
        )
        fakeAuthRepository.shouldReturnError = false
        viewModel = createViewModel()

        // When: User signs in
        viewModel.signIn(mockCredentialProvider)

        // Then: Settings migrated to signed-in user
        val userSettings = fakeNotificationSettingsRepository.getSettingsDirectly(testUser.userId)
        assertEquals(true, userSettings?.enabled)
        assertEquals(8, userSettings?.hour)
        assertEquals(30, userSettings?.minute)

        // And: Anonymous settings deleted
        assertNull(fakeNotificationSettingsRepository.getSettingsDirectly("anonymous"))
    }

    @Test
    fun `signOut copies settings to anonymous and clears account settings`() = runTest {
        // Given: User is signed in with notification settings
        fakeAuthRepository.setSignedInUser(testUser)
        fakeAuthStateRepository.setSignedInState(true)
        fakeNotificationSettingsRepository.setSettings(
            testUser.userId,
            uk.co.zlurgg.thedayto.notification.domain.model.NotificationSettings(
                enabled = true,
                hour = 9,
                minute = 0
            )
        )
        viewModel = createViewModel()

        // When: User signs out
        viewModel.signOut()

        // Then: Settings copied to anonymous and rescheduled
        val anonymousSettings = fakeNotificationSettingsRepository.getSettingsDirectly("anonymous")
        assertEquals(true, anonymousSettings?.enabled)
        assertEquals(9, anonymousSettings?.hour)
        assertTrue(fakeNotificationScheduler.isScheduledAt(9, 0))

        // And: User's account settings deleted
        assertNull(fakeNotificationSettingsRepository.getSettingsDirectly(testUser.userId))
    }
}
