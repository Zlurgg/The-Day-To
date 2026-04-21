package uk.co.zlurgg.thedayto.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.zlurgg.thedayto.auth.domain.model.IdToken
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCases
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.auth.ui.state.SignInNavigationTarget
import uk.co.zlurgg.thedayto.auth.ui.state.SignInState
import uk.co.zlurgg.thedayto.auth.ui.state.SignInUiEvent
import uk.co.zlurgg.thedayto.core.domain.error.ErrorFormatter
import uk.co.zlurgg.thedayto.core.domain.result.Result
import uk.co.zlurgg.thedayto.sync.DevCredentials

class SignInViewModel(
    private val signInUseCases: SignInUseCases,
) : ViewModel() {

    // UI State
    private val _state = MutableStateFlow(
        SignInState(
            isDevSignInAvailable = signInUseCases.devSignIn?.isAvailable() == true,
        ),
    )
    val state = _state.asStateFlow()

    // One-time UI events
    private val _uiEvents = MutableSharedFlow<SignInUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    init {
        // Check if user should see welcome dialog (first-time users)
        viewModelScope.launch {
            val hasSeenWelcome = signInUseCases.checkWelcomeDialogSeen()
            if (!hasSeenWelcome) {
                _state.update { it.copy(showWelcomeDialog = true) }
            }
        }
    }

    /**
     * Dismisses the welcome dialog and marks it as seen
     * Called when user closes the first-time welcome dialog
     */
    fun dismissWelcomeDialog() {
        viewModelScope.launch {
            signInUseCases.markWelcomeDialogSeen()
            _state.update { it.copy(showWelcomeDialog = false) }
        }
    }

    /**
     * Initiates Google Sign-In flow.
     *
     * The ViewModel owns the coroutine — it invokes the fetch lambda, then
     * passes the resulting IdToken to the UseCase. Both credential fetch
     * errors and sign-in errors flow through the same error path.
     *
     * @param fetchCredential Suspend lambda that fetches Google credentials.
     *   Lambda captures Activity — invoke immediately, do not store.
     */
    fun signIn(fetchCredential: suspend () -> Result<IdToken, DataError.Auth>) {
        viewModelScope.launch {
            _state.update { it.copy(isSignInSuccessful = false, signInError = null) }

            val idToken = when (val fetchResult = fetchCredential()) {
                is Result.Success -> fetchResult.data
                is Result.Error -> {
                    val errorMessage = ErrorFormatter.format(fetchResult.error, "sign in")
                    _state.update { it.copy(isSignInSuccessful = false, signInError = errorMessage) }
                    _uiEvents.emit(SignInUiEvent.ShowSnackbar(errorMessage))
                    return@launch
                }
            }

            when (val result = signInUseCases.signIn(idToken)) {
                is Result.Success -> {
                    // Sign-in successful
                    _state.update { it.copy(isSignInSuccessful = true, signInError = null) }

                    // Seed default mood colors on first launch
                    signInUseCases.seedDefaultMoodColors()

                    // Always navigate to Overview
                    _state.update { it.copy(navigationTarget = SignInNavigationTarget.ToOverview) }
                }

                is Result.Error -> {
                    // Sign-in failed
                    val errorMessage = ErrorFormatter.format(result.error, "sign in")
                    _state.update { it.copy(isSignInSuccessful = false, signInError = errorMessage) }
                    _uiEvents.emit(SignInUiEvent.ShowSnackbar(errorMessage))
                }
            }
        }
    }

    /**
     * Initiates dev sign-in via Firebase Auth Emulator
     * Uses predefined test credentials
     */
    fun devSignIn() {
        val devSignInUseCase = signInUseCases.devSignIn ?: return

        viewModelScope.launch {
            _state.update { it.copy(isSignInSuccessful = false, signInError = null) }

            when (val result = devSignInUseCase(DevCredentials.DEV_EMAIL, DevCredentials.DEV_PASSWORD)) {
                is Result.Success -> {
                    _state.update { it.copy(isSignInSuccessful = true, signInError = null) }
                    signInUseCases.seedDefaultMoodColors()
                    _state.update { it.copy(navigationTarget = SignInNavigationTarget.ToOverview) }
                }

                is Result.Error -> {
                    val errorMessage = ErrorFormatter.format(result.error, "dev sign in")
                    _state.update { it.copy(isSignInSuccessful = false, signInError = errorMessage) }
                    _uiEvents.emit(SignInUiEvent.ShowSnackbar(errorMessage))
                }
            }
        }
    }

    /**
     * Check if user is already signed in
     * Called on app start to determine initial navigation
     */
    fun checkSignInStatus() {
        viewModelScope.launch {
            // Check sign-in status via UseCase
            if (signInUseCases.checkSignInStatus()) {
                // Always navigate to Overview
                _state.update { it.copy(navigationTarget = SignInNavigationTarget.ToOverview) }
            }
        }
    }

    /**
     * Called after navigation has been handled by the UI
     */
    fun onNavigationHandled() {
        _state.update { it.copy(navigationTarget = null) }
    }
}
