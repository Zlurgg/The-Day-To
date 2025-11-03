package uk.co.zlurgg.thedayto.journal.ui.overview

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.fake.FakeNotificationRepository
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository
import uk.co.zlurgg.thedayto.fake.createFakeOverviewUseCases
import uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewAction

/**
 * Unit tests for OverviewViewModel notification functionality.
 *
 * Tests notification settings management including:
 * - Loading notification settings on initialization
 * - Permission handling
 * - Saving notification settings
 * - Dialog state management
 * - Error handling
 */
@OptIn(ExperimentalCoroutinesApi::class)
class OverviewViewModelTest {

    // Test dispatcher for controlling coroutine execution
    private val testDispatcher = StandardTestDispatcher()
    private val testScheduler get() = testDispatcher.scheduler

    // Fake repositories
    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var fakeNotificationRepository: FakeNotificationRepository

    // System under test
    private lateinit var viewModel: OverviewViewModel

    @Before
    fun setup() {
        // Set main dispatcher to test dispatcher
        Dispatchers.setMain(testDispatcher)

        // Initialize fake repositories
        fakePreferencesRepository = FakePreferencesRepository()
        fakeNotificationRepository = FakeNotificationRepository()

        // Create ViewModel with fake dependencies
        val useCases = createFakeOverviewUseCases(
            preferencesRepository = fakePreferencesRepository,
            notificationRepository = fakeNotificationRepository
        )
        viewModel = OverviewViewModel(useCases)
    }

    @After
    fun tearDown() {
        // Reset main dispatcher
        Dispatchers.resetMain()
    }

    @Test
    fun `loadNotificationSettings - loads settings on init`() = runTest {
        // Given: Set up notification preferences
        fakePreferencesRepository.setNotificationEnabled(true)
        fakePreferencesRepository.setNotificationTime(14, 30)
        fakeNotificationRepository.hasPermission = true

        // When: Create new ViewModel (triggers init)
        val useCases = createFakeOverviewUseCases(
            preferencesRepository = fakePreferencesRepository,
            notificationRepository = fakeNotificationRepository
        )
        val testViewModel = OverviewViewModel(useCases)
        testScheduler.advanceUntilIdle()

        // Then: State should reflect loaded settings
        val state = testViewModel.uiState.value
        assertTrue("Notifications should be enabled", state.notificationsEnabled)
        assertEquals("Hour should be 14", 14, state.notificationHour)
        assertEquals("Minute should be 30", 30, state.notificationMinute)
        assertTrue("Permission should be granted", state.hasNotificationPermission)
    }

    @Test
    fun `loadNotificationSettings - handles default values`() = runTest {
        // Given: Default preferences (notification disabled, 9:00 AM)
        // (FakePreferencesRepository initializes with defaults)

        // When: ViewModel initializes
        // (already initialized in @Before)

        // Then: State should have default values
        val state = viewModel.uiState.value
        assertFalse("Notifications should be disabled by default", state.notificationsEnabled)
        assertEquals("Default hour should be 9", 9, state.notificationHour)
        assertEquals("Default minute should be 0", 0, state.notificationMinute)
    }

    @Test
    fun `onNotificationPermissionGranted - updates state and shows confirm dialog`() = runTest {
        // Given: Permission not yet granted
        fakeNotificationRepository.hasPermission = false
        val useCases = createFakeOverviewUseCases(
            preferencesRepository = fakePreferencesRepository,
            notificationRepository = fakeNotificationRepository
        )
        val testViewModel = OverviewViewModel(useCases)
        testScheduler.advanceUntilIdle()

        val initialState = testViewModel.uiState.value
        assertFalse("Should not have permission initially", initialState.hasNotificationPermission)
        assertFalse("Dialog should not be shown initially", initialState.showNotificationConfirmDialog)

        // When: Permission is granted
        testViewModel.onNotificationPermissionGranted()
        testScheduler.advanceUntilIdle()

        // Then: State should be updated
        val newState = testViewModel.uiState.value
        assertTrue("Should have permission", newState.hasNotificationPermission)
        assertTrue("Confirm dialog should be shown", newState.showNotificationConfirmDialog)
    }

    @Test
    fun `DismissNotificationConfirmDialog - hides dialog`() = runTest {
        // Given: Dialog is shown
        viewModel.onNotificationPermissionGranted()
        testScheduler.advanceUntilIdle()
        assertTrue("Dialog should be shown", viewModel.uiState.value.showNotificationConfirmDialog)

        // When: User dismisses dialog
        viewModel.onAction(OverviewAction.DismissNotificationConfirmDialog)
        testScheduler.advanceUntilIdle()

        // Then: Dialog should be hidden
        assertFalse("Dialog should be hidden", viewModel.uiState.value.showNotificationConfirmDialog)
    }

    @Test
    fun `OpenNotificationSettings - shows settings dialog`() = runTest {
        // Given: Settings dialog not shown
        assertFalse("Dialog should not be shown initially", viewModel.uiState.value.showNotificationSettingsDialog)

        // When: User opens settings
        viewModel.onAction(OverviewAction.OpenNotificationSettings)
        testScheduler.advanceUntilIdle()

        // Then: Settings dialog should be shown
        assertTrue("Settings dialog should be shown", viewModel.uiState.value.showNotificationSettingsDialog)
    }

    @Test
    fun `DismissNotificationSettings - hides settings dialog`() = runTest {
        // Given: Settings dialog is shown
        viewModel.onAction(OverviewAction.OpenNotificationSettings)
        testScheduler.advanceUntilIdle()
        assertTrue("Dialog should be shown", viewModel.uiState.value.showNotificationSettingsDialog)

        // When: User dismisses settings
        viewModel.onAction(OverviewAction.DismissNotificationSettings)
        testScheduler.advanceUntilIdle()

        // Then: Dialog should be hidden
        assertFalse("Dialog should be hidden", viewModel.uiState.value.showNotificationSettingsDialog)
    }

    @Test
    fun `SaveNotificationSettings - saves settings and updates state when enabled`() = runTest {
        // Given: Initial state
        assertFalse("Notifications should be disabled initially", viewModel.uiState.value.notificationsEnabled)

        // When: User saves settings with enabled=true
        viewModel.onAction(OverviewAction.SaveNotificationSettings(
            enabled = true,
            hour = 15,
            minute = 45
        ))
        testScheduler.advanceUntilIdle()

        // Then: State should be updated
        val state = viewModel.uiState.value
        assertTrue("Notifications should be enabled", state.notificationsEnabled)
        assertEquals("Hour should be 15", 15, state.notificationHour)
        assertEquals("Minute should be 45", 45, state.notificationMinute)
        assertFalse("Settings dialog should be closed", state.showNotificationSettingsDialog)

        // And: Repository should have saved settings
        assertTrue("Repository should have enabled=true", fakePreferencesRepository.isNotificationEnabled())
        assertEquals("Repository should have hour=15", 15, fakePreferencesRepository.getNotificationHour())
        assertEquals("Repository should have minute=45", 45, fakePreferencesRepository.getNotificationMinute())

        // And: Notification should be scheduled
        assertTrue("Notification should be scheduled", fakeNotificationRepository.updateNotificationTimeCalled)
        assertTrue("Notification should be scheduled at 15:45", fakeNotificationRepository.isScheduledAt(15, 45))
    }

    @Test
    fun `SaveNotificationSettings - cancels notifications when disabled`() = runTest {
        // Given: Notifications are currently enabled
        fakePreferencesRepository.setNotificationEnabled(true)
        fakePreferencesRepository.setNotificationTime(10, 0)

        // When: User disables notifications
        viewModel.onAction(OverviewAction.SaveNotificationSettings(
            enabled = false,
            hour = 10,
            minute = 0
        ))
        testScheduler.advanceUntilIdle()

        // Then: State should be updated
        assertFalse("Notifications should be disabled", viewModel.uiState.value.notificationsEnabled)

        // And: Repository should have saved disabled state
        assertFalse("Repository should have enabled=false", fakePreferencesRepository.isNotificationEnabled())

        // And: Notifications should be cancelled
        assertTrue("Notifications should be cancelled", fakeNotificationRepository.cancelNotificationsCalled)
    }

    @Test
    fun `SaveNotificationSettings - shows snackbar on error`() = runTest {
        // Given: Create a failing use case
        val failingRepository = FakeNotificationRepository().apply {
            updateNotificationTimeThrows = true
        }
        val useCases = createFakeOverviewUseCases(
            preferencesRepository = fakePreferencesRepository,
            notificationRepository = failingRepository
        )
        val testViewModel = OverviewViewModel(useCases)
        testScheduler.advanceUntilIdle()

        // When: User tries to save settings
        testViewModel.uiEvents.test {
            testViewModel.onAction(OverviewAction.SaveNotificationSettings(
                enabled = true,
                hour = 10,
                minute = 0
            ))
            testScheduler.advanceUntilIdle()

            // Then: Snackbar event should be emitted with error message
            val event = awaitItem()
            assertTrue("Should show snackbar", event is uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiEvent.ShowSnackbar)
            val snackbarEvent = event as uk.co.zlurgg.thedayto.journal.ui.overview.state.OverviewUiEvent.ShowSnackbar
            assertTrue("Error message should contain failure text", snackbarEvent.message.contains("Failed"))
        }
    }

    @Test
    fun `notification permission state - reflects repository state`() = runTest {
        // Given: Permission not granted
        fakeNotificationRepository.hasPermission = false

        // When: ViewModel loads settings
        val useCases = createFakeOverviewUseCases(
            preferencesRepository = fakePreferencesRepository,
            notificationRepository = fakeNotificationRepository
        )
        val testViewModel = OverviewViewModel(useCases)
        testScheduler.advanceUntilIdle()

        // Then: State should reflect no permission
        assertFalse("Should not have permission", testViewModel.uiState.value.hasNotificationPermission)

        // When: Permission is granted
        fakeNotificationRepository.hasPermission = true
        testViewModel.onNotificationPermissionGranted()
        testScheduler.advanceUntilIdle()

        // Then: State should reflect permission granted
        assertTrue("Should have permission", testViewModel.uiState.value.hasNotificationPermission)
    }

    @Test
    fun `notification workflow - complete user journey`() = runTest {
        // Scenario: User grants permission, opens settings, and configures notifications

        // Step 1: User grants permission
        viewModel.onNotificationPermissionGranted()
        testScheduler.advanceUntilIdle()

        var state = viewModel.uiState.value
        assertTrue("Permission should be granted", state.hasNotificationPermission)
        assertTrue("Confirm dialog should be shown", state.showNotificationConfirmDialog)

        // Step 2: User clicks "Change Time" to open settings
        viewModel.onAction(OverviewAction.DismissNotificationConfirmDialog)
        viewModel.onAction(OverviewAction.OpenNotificationSettings)
        testScheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        assertFalse("Confirm dialog should be hidden", state.showNotificationConfirmDialog)
        assertTrue("Settings dialog should be shown", state.showNotificationSettingsDialog)

        // Step 3: User configures notification for 8:30 AM
        viewModel.onAction(OverviewAction.SaveNotificationSettings(
            enabled = true,
            hour = 8,
            minute = 30
        ))
        testScheduler.advanceUntilIdle()

        state = viewModel.uiState.value
        assertTrue("Notifications should be enabled", state.notificationsEnabled)
        assertEquals("Hour should be 8", 8, state.notificationHour)
        assertEquals("Minute should be 30", 30, state.notificationMinute)
        assertFalse("Settings dialog should be closed", state.showNotificationSettingsDialog)

        // Verify notification was scheduled
        assertTrue("Notification should be scheduled at 8:30", fakeNotificationRepository.isScheduledAt(8, 30))
    }
}
