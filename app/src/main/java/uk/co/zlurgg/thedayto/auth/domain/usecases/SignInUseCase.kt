package uk.co.zlurgg.thedayto.auth.domain.usecases

import android.content.Context
import uk.co.zlurgg.thedayto.auth.data.service.GoogleAuthUiClient
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.auth.domain.model.SignInResult

/**
 * Initiates Google Sign-In flow.
 * Returns SignInResult with user data or error message.
 */
class SignInUseCase(
    private val googleAuthUiClient: GoogleAuthUiClient,
    private val authStateRepository: AuthStateRepository
) {
    suspend operator fun invoke(activityContext: Context): SignInResult {
        val result = googleAuthUiClient.signIn(activityContext)

        if (result.data != null) {
            // Save sign-in state on success
            authStateRepository.setSignedInState(true)
        }

        return result
    }
}
