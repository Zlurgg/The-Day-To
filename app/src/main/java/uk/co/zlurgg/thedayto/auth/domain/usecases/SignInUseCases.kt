package uk.co.zlurgg.thedayto.auth.domain.usecases

/**
 * Aggregator for all SignIn-related UseCases.
 * Injected into SignInViewModel.
 */
data class SignInUseCases(
    val checkTodayEntry: CheckTodayEntryUseCase
)
