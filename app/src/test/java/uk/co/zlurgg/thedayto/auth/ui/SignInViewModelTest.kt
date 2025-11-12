package uk.co.zlurgg.thedayto.auth.ui

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckSignInStatusUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckTodayEntryUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.MarkWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCases
import uk.co.zlurgg.thedayto.auth.ui.state.SignInUiEvent
import uk.co.zlurgg.thedayto.fake.FakeAuthRepository
import uk.co.zlurgg.thedayto.fake.FakeAuthStateRepository
import uk.co.zlurgg.thedayto.fake.FakeEntryRepository
import uk.co.zlurgg.thedayto.fake.FakeMoodColorRepository
import uk.co.zlurgg.thedayto.fake.FakePreferencesRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SeedDefaultMoodColorsUseCase

/**
 * Unit tests for SignInViewModel.
 *
 * Tests cover:
 * - Initialization and welcome dialog logic (first-time vs returning users)
 * - Sign-in flow (success and error scenarios)
 * - Check sign-in status (auto-navigation)
 * - Welcome dialog dismissal
 * - Default mood colors seeding on first sign-in
 *
 * Following Google's 2025 best practices:
 * - ViewModels tested with fake repositories (NOT real database)
 * - Focus on business logic and state management
 * - Test UI events (navigation, snackbars)
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private lateinit var viewModel: SignInViewModel
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeAuthStateRepository: FakeAuthStateRepository
    private lateinit var fakePreferencesRepository: FakePreferencesRepository
    private lateinit var fakeEntryRepository: FakeEntryRepository
    private lateinit var fakeMoodColorRepository: FakeMoodColorRepository

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Initialize fake repositories
        fakeAuthRepository = FakeAuthRepository()
        fakeAuthStateRepository = FakeAuthStateRepository()
        fakePreferencesRepository = FakePreferencesRepository()
        fakeEntryRepository = FakeEntryRepository()
        fakeMoodColorRepository = FakeMoodColorRepository()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SignInViewModel {
        val signInUseCases = SignInUseCases(
            signIn = SignInUseCase(fakeAuthRepository, fakeAuthStateRepository),
            checkSignInStatus = CheckSignInStatusUseCase(fakeAuthRepository, fakeAuthStateRepository),
            checkTodayEntry = CheckTodayEntryUseCase(fakeEntryRepository),
            seedDefaultMoodColors = SeedDefaultMoodColorsUseCase(fakeMoodColorRepository, fakePreferencesRepository),
            checkWelcomeDialogSeen = CheckWelcomeDialogSeenUseCase(fakePreferencesRepository),
            markWelcomeDialogSeen = MarkWelcomeDialogSeenUseCase(fakePreferencesRepository)
        )
        return SignInViewModel(signInUseCases)
    }

    // ============================================================
    // Initialization & Welcome Dialog Tests
    // ============================================================

    @Test
    fun `init shows welcome dialog for first-time users`() = runTest {
        // Given: First launch (default state - welcomeDialogSeen = false)
        viewModel = createViewModel()

        // Then: Welcome dialog should be shown
        viewModel.state.test {
            val state = awaitItem()
            assertTrue("Welcome dialog should be shown for first-time users", state.showWelcomeDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `init does not show welcome dialog for returning users`() = runTest {
        // Given: User has seen welcome dialog before
        fakePreferencesRepository.markWelcomeDialogSeen()

        // When: ViewModel initialized
        viewModel = createViewModel()

        // Then: Welcome dialog should NOT be shown
        viewModel.state.test {
            val state = awaitItem()
            assertFalse("Welcome dialog should not be shown for returning users", state.showWelcomeDialog)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissWelcomeDialog hides dialog and marks as seen`() = runTest {
        // Given: ViewModel with welcome dialog shown
        viewModel = createViewModel()

        // When: User dismisses welcome dialog
        viewModel.dismissWelcomeDialog()

        // Then: Dialog should be hidden
        viewModel.state.test {
            val state = awaitItem()
            assertFalse("Welcome dialog should be hidden after dismissal", state.showWelcomeDialog)
            cancelAndIgnoreRemainingEvents()
        }

        // And: Preference should be marked as seen
        assertTrue(
            "Welcome dialog seen preference should be true",
            fakePreferencesRepository.hasSeenWelcomeDialog()
        )
    }

    // ============================================================
    // Sign-In Flow Tests
    // ============================================================

    @Test
    fun `signIn successful updates state and navigates to overview`() = runTest {
        // Given: Auth repository configured for success
        fakeAuthRepository.shouldReturnError = false
        viewModel = createViewModel()

        // When: User signs in (collect events first, then trigger action)
        viewModel.uiEvents.test {
            viewModel.signIn()

            // Then: Should emit navigate to overview event
            val event = awaitItem()
            assertTrue("Should navigate to overview", event is SignInUiEvent.NavigateToOverview)
            cancelAndIgnoreRemainingEvents()
        }

        // And: State should reflect successful sign-in
        viewModel.state.test {
            val state = awaitItem()
            assertTrue("Sign-in should be successful", state.isSignInSuccessful)
            assertEquals("Error should be null", null, state.signInError)
            cancelAndIgnoreRemainingEvents()
        }

        // And: Auth state should be updated
        assertTrue("Auth state should be signed in", fakeAuthStateRepository.getSignedInState())
    }

    @Test
    fun `signIn successful seeds default mood colors on first launch`() = runTest {
        // Given: First launch and successful auth
        fakeAuthRepository.shouldReturnError = false
        viewModel = createViewModel()

        // When: User signs in
        viewModel.signIn()

        // Then: Default mood colors should be seeded
        val moodColors = fakeMoodColorRepository.getMoodColorsSync()
        assertEquals("Should have 7 default mood colors", 7, moodColors.size)

        // And: First launch should be marked complete
        assertFalse("First launch should be marked complete", fakePreferencesRepository.isFirstLaunch())
    }

    @Test
    fun `signIn failure updates state with error and shows snackbar`() = runTest {
        // Given: Auth repository configured for failure
        fakeAuthRepository.shouldReturnError = true
        fakeAuthRepository.errorMessage = "Network error"
        viewModel = createViewModel()

        // When: User signs in (collect events first, then trigger action)
        viewModel.uiEvents.test {
            viewModel.signIn()

            // Then: Should emit snackbar event
            val event = awaitItem()
            assertTrue("Should show error snackbar", event is SignInUiEvent.ShowSnackbar)
            assertEquals(
                "Snackbar message should match",
                "Network error",
                (event as SignInUiEvent.ShowSnackbar).message
            )
            cancelAndIgnoreRemainingEvents()
        }

        // And: State should reflect failed sign-in
        viewModel.state.test {
            val state = awaitItem()
            assertFalse("Sign-in should not be successful", state.isSignInSuccessful)
            assertEquals("Error message should match", "Network error", state.signInError)
            cancelAndIgnoreRemainingEvents()
        }

        // And: Auth state should NOT be updated
        assertFalse("Auth state should not be signed in", fakeAuthStateRepository.getSignedInState())
    }

    @Test
    fun `signIn clears previous error state before attempting sign-in`() = runTest {
        // Given: ViewModel with previous error state
        fakeAuthRepository.shouldReturnError = true
        fakeAuthRepository.errorMessage = "First error"
        viewModel = createViewModel()
        viewModel.signIn()

        // When: User attempts sign-in again (this time successfully)
        fakeAuthRepository.shouldReturnError = false
        viewModel.signIn()

        // Then: Error should be cleared
        viewModel.state.test {
            val state = awaitItem()
            assertTrue("Sign-in should be successful", state.isSignInSuccessful)
            assertEquals("Error should be null", null, state.signInError)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ============================================================
    // Check Sign-In Status Tests
    // ============================================================

    @Test
    fun `checkSignInStatus navigates to overview when user is signed in`() = runTest {
        // Given: User is signed in
        val testUser = UserData(
            userId = "test_123",
            username = "Test User",
            profilePictureUrl = null
        )
        fakeAuthRepository.setSignedInUser(testUser)
        fakeAuthStateRepository.setSignedInState(true)
        viewModel = createViewModel()

        // When: Checking sign-in status (collect events first, then trigger action)
        viewModel.uiEvents.test {
            viewModel.checkSignInStatus()

            // Then: Should emit navigate to overview event
            val event = awaitItem()
            assertTrue("Should navigate to overview", event is SignInUiEvent.NavigateToOverview)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `checkSignInStatus does not navigate when user is not signed in`() = runTest {
        // Given: User is NOT signed in
        fakeAuthStateRepository.setSignedInState(false)
        fakeAuthRepository.setSignedInUser(null)
        viewModel = createViewModel()

        // When: Checking sign-in status
        viewModel.checkSignInStatus()

        // Then: Should NOT emit any events
        viewModel.uiEvents.test {
            expectNoEvents()
        }
    }

    @Test
    fun `checkSignInStatus does not navigate when auth state is true but user is null`() = runTest {
        // Given: Auth state says signed in, but no user data (inconsistent state)
        fakeAuthStateRepository.setSignedInState(true)
        fakeAuthRepository.setSignedInUser(null)
        viewModel = createViewModel()

        // When: Checking sign-in status
        viewModel.checkSignInStatus()

        // Then: Should NOT navigate (both conditions must be true)
        viewModel.uiEvents.test {
            expectNoEvents()
        }
    }
}
