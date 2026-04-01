package uk.co.zlurgg.thedayto.auth.ui

import android.app.Activity
import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider

/**
 * Factory for creating CredentialProvider instances.
 *
 * This interface lives in the UI layer because it's the contract
 * the UI uses to create credential providers. The implementation
 * lives in the data layer and is injected via Koin.
 *
 * This pattern allows SignInScreen to depend on an interface rather
 * than directly importing from the data layer, maintaining clean architecture.
 */
interface CredentialProviderFactory {

    /**
     * Creates a CredentialProvider that will fetch Google credentials.
     *
     * @param activity The Activity context required by Credential Manager
     * @param serverClientId The OAuth 2.0 web client ID
     * @return A CredentialProvider that can be passed to the ViewModel
     */
    fun create(activity: Activity, serverClientId: String): CredentialProvider
}
