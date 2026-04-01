package uk.co.zlurgg.thedayto.fake

import android.app.Activity
import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
import uk.co.zlurgg.thedayto.auth.domain.model.GoogleCredential
import uk.co.zlurgg.thedayto.auth.ui.CredentialProviderFactory
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result

/**
 * Fake implementation of CredentialProviderFactory for testing.
 * Allows controlling the credential result without needing Activity context.
 */
class FakeCredentialProviderFactory : CredentialProviderFactory {

    // Control what the created provider returns
    var shouldReturnError = false
    var authError: DataError.Auth = DataError.Auth.FAILED
    var googleCredential = GoogleCredential("test_id_token")

    // Track calls for verification
    var createCallCount = 0
        private set
    var lastServerClientId: String? = null
        private set

    override fun create(activity: Activity, serverClientId: String): CredentialProvider {
        createCallCount++
        lastServerClientId = serverClientId

        return {
            if (shouldReturnError) {
                Result.Error(authError)
            } else {
                Result.Success(googleCredential)
            }
        }
    }

    /**
     * Reset all values to defaults.
     * Useful for cleaning up between tests.
     */
    fun reset() {
        shouldReturnError = false
        authError = DataError.Auth.FAILED
        googleCredential = GoogleCredential("test_id_token")
        createCallCount = 0
        lastServerClientId = null
    }
}
