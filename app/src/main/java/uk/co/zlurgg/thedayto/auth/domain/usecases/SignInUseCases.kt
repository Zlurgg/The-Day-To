package uk.co.zlurgg.thedayto.auth.domain.usecases

import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SeedDefaultMoodColorsUseCase

/**
 * Aggregator for all SignIn-related UseCases.
 * Injected into SignInViewModel.
 */
data class SignInUseCases(
    val signIn: SignInUseCase,
    val checkSignInStatus: CheckSignInStatusUseCase,
    val checkTodayEntry: CheckTodayEntryUseCase,
    val seedDefaultMoodColors: SeedDefaultMoodColorsUseCase,
    val checkWelcomeDialogSeen: CheckWelcomeDialogSeenUseCase,
    val markWelcomeDialogSeen: MarkWelcomeDialogSeenUseCase
)
