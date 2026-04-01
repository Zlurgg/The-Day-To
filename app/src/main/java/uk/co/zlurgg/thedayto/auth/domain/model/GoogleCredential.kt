package uk.co.zlurgg.thedayto.auth.domain.model

import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result

/**
 * Domain model for Google credential - no Android dependencies.
 *
 * This allows the domain layer to remain pure Kotlin while
 * the data layer handles mapping from Android's GoogleIdTokenCredential.
 */
data class GoogleCredential(val idToken: String)

/**
 * Type alias for a suspend lambda that provides Google credentials.
 *
 * This callback pattern allows the UI layer (which has Activity context)
 * to provide credentials to the domain layer without passing Android types.
 *
 * Usage:
 * - UI creates the provider using CredentialManagerUtil with Activity context
 * - ViewModel passes the provider to UseCase
 * - Repository invokes the provider to get credentials
 */
typealias CredentialProvider = suspend () -> Result<GoogleCredential, DataError.Auth>
