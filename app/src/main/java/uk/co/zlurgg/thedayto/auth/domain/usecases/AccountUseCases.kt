package uk.co.zlurgg.thedayto.auth.domain.usecases

/**
 * Aggregator for account-related UseCases.
 *
 * Provides a convenient way to inject all account management UseCases together.
 * Used by SyncSettingsViewModel for sign-in, sign-out, and account deletion operations.
 */
data class AccountUseCases(
    val getSignedInUser: GetSignedInUserUseCase,
    val signIn: SignInUseCase,
    val signOut: SignOutUseCase,
    val reauthenticate: ReauthenticateUseCase,
    val deleteAccount: DeleteAccountUseCase,
    val devSignIn: DevSignInUseCase? = null
)
