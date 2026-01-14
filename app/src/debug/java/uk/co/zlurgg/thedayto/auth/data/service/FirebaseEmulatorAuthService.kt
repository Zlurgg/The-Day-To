package uk.co.zlurgg.thedayto.auth.data.service

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import uk.co.zlurgg.thedayto.auth.domain.model.UserData
import uk.co.zlurgg.thedayto.auth.domain.service.DevAuthService
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
import java.util.concurrent.CancellationException

/**
 * Debug implementation that connects to Firebase Auth Emulator.
 *
 * Emulator must be running at localhost:9099
 */
class FirebaseEmulatorAuthService : DevAuthService {

    private val auth = FirebaseAuth.getInstance()

    init {
        try {
            auth.useEmulator(EMULATOR_HOST, EMULATOR_PORT)
            Timber.d("Connected to Firebase Auth Emulator at %s:%d", EMULATOR_HOST, EMULATOR_PORT)
        } catch (e: IllegalStateException) {
            Timber.d("Firebase Auth Emulator already connected")
        }
    }

    override suspend fun signInWithEmailPassword(
        email: String,
        password: String
    ): Result<UserData, DataError.Auth> {
        return try {
            Timber.d("Dev sign-in attempt with email: %s", email)

            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val user = authResult.user

            if (user != null) {
                Timber.d("Dev sign-in successful for user: %s", user.uid)
                Result.Success(
                    UserData(
                        userId = user.uid,
                        username = user.displayName ?: DEV_USER_DISPLAY_NAME,
                        profilePictureUrl = user.photoUrl?.toString()
                    )
                )
            } else {
                Timber.e("Dev sign-in returned null user")
                Result.Error(DataError.Auth.FAILED)
            }
        } catch (e: Exception) {
            Timber.e(e, "Dev sign-in failed")
            if (e is CancellationException) throw e

            val error = when {
                e.message?.contains("network", ignoreCase = true) == true ->
                    DataError.Auth.NETWORK_ERROR
                e.message?.contains("password", ignoreCase = true) == true ->
                    DataError.Auth.FAILED
                e.message?.contains("user", ignoreCase = true) == true ->
                    DataError.Auth.NO_CREDENTIAL
                else -> DataError.Auth.FAILED
            }
            Result.Error(error)
        }
    }

    override fun isAvailable(): Boolean = true

    companion object {
        private const val EMULATOR_HOST = "10.0.2.2"
        private const val EMULATOR_PORT = 9099
        private const val DEV_USER_DISPLAY_NAME = "Dev User"
    }
}
