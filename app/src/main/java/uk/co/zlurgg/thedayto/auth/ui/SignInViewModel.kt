package uk.co.zlurgg.thedayto.auth.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import uk.co.zlurgg.thedayto.auth.ui.state.SignInState
import uk.co.zlurgg.thedayto.auth.ui.state.SignInUiEvent
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCases

class SignInViewModel(
    private val signInUseCases: SignInUseCases
) : ViewModel() {

    // UI State
    private val _state = MutableStateFlow(SignInState())
    val state = _state.asStateFlow()

    // One-time UI events
    private val _uiEvents = MutableSharedFlow<SignInUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    /**
     * Initiates Google Sign-In flow
     * @param activityContext Required for credential UI
     */
    fun signIn(activityContext: Context) {
        viewModelScope.launch {
            _state.update { it.copy(isSignInSuccessful = false, signInError = null) }

            // Sign in via UseCase
            val result = signInUseCases.signIn(activityContext)

            if (result.data != null) {
                // Sign-in successful
                _state.update { it.copy(isSignInSuccessful = true, signInError = null) }
                // Always navigate to Overview
                _uiEvents.emit(SignInUiEvent.NavigateToOverview)
            } else {
                // Sign-in failed
                val errorMessage = result.errorMessage ?: "Unknown sign-in error"
                _state.update { it.copy(isSignInSuccessful = false, signInError = errorMessage) }
                _uiEvents.emit(SignInUiEvent.ShowSnackbar(errorMessage))
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
                _uiEvents.emit(SignInUiEvent.NavigateToOverview)
            }
        }
    }
}