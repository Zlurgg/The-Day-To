package uk.co.zlurgg.thedayto.auth.data.service

import android.app.Activity
import uk.co.zlurgg.thedayto.auth.domain.model.CredentialProvider
import uk.co.zlurgg.thedayto.auth.ui.CredentialProviderFactory

/**
 * Implementation of CredentialProviderFactory that uses CredentialManagerUtil.
 *
 * This class bridges the UI layer interface with the data layer implementation,
 * allowing the UI to create credential providers without depending directly
 * on CredentialManagerUtil.
 */
internal class CredentialProviderFactoryImpl : CredentialProviderFactory {

    override fun create(activity: Activity, serverClientId: String): CredentialProvider = {
        CredentialManagerUtil.getGoogleCredential(activity, serverClientId)
    }
}
