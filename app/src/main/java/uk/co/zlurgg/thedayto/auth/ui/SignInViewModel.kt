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
import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.auth.ui.state.SignInState
import uk.co.zlurgg.thedayto.auth.ui.state.SignInUiEvent
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.journal.domain.usecases.entry.EntryUseCases
import java.time.LocalDate
import java.time.ZoneOffset

class SignInViewModel(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val authStateRepository: AuthStateRepository,
    private val entryUseCases: EntryUseCases
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

            val result = googleAuthUiClient.signIn(activityContext)

            if (result.data != null) {
                // Sign-in successful
                _state.update { it.copy(isSignInSuccessful = true, signInError = null) }

                // Save sign-in state
                authStateRepository.setSignedInState(true)

                // Check if entry exists for today
                val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                val todayEntry = entryUseCases.getEntryByDate(todayStart)

                // Navigate based on entry existence
                if (todayEntry != null) {
                    _uiEvents.emit(SignInUiEvent.NavigateToOverview)
                } else {
                    _uiEvents.emit(SignInUiEvent.NavigateToEditor(entryDate = todayStart))
                }
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
            val isSignedIn = authStateRepository.getSignedInState()
            val currentUser = googleAuthUiClient.getSignedInUser()

            if (isSignedIn && currentUser != null) {
                // Check if entry exists for today
                val todayStart = LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
                val todayEntry = entryUseCases.getEntryByDate(todayStart)

                // Navigate based on entry existence
                if (todayEntry != null) {
                    _uiEvents.emit(SignInUiEvent.NavigateToOverview)
                } else {
                    _uiEvents.emit(SignInUiEvent.NavigateToEditor(entryDate = todayStart))
                }
            }
        }
    }

    fun resetState() {
        _state.update { SignInState() }
    }
}