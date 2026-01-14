package uk.co.zlurgg.thedayto.auth.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.auth.data.repository.AuthRepositoryImpl
import uk.co.zlurgg.thedayto.auth.data.repository.AuthStateRepositoryImpl
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckSignInStatusUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckTodayEntryUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.MarkWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCases
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignOutUseCase
import uk.co.zlurgg.thedayto.auth.ui.SignInViewModel

val authModule = module {

    // Auth Repository (wraps GoogleAuthUiClient)
    single<AuthRepository> {
        AuthRepositoryImpl(context = androidContext())
    }

    // Auth State Repository
    single<AuthStateRepository> {
        AuthStateRepositoryImpl(androidContext())
    }

    // Auth UseCases
    single {
        SignInUseCases(
            signIn = SignInUseCase(
                authRepository = get(),
                authStateRepository = get()
            ),
            checkSignInStatus = CheckSignInStatusUseCase(
                authRepository = get(),
                authStateRepository = get()
            ),
            checkTodayEntry = CheckTodayEntryUseCase(entryRepository = get()),
            seedDefaultMoodColors = get(),
            checkWelcomeDialogSeen = CheckWelcomeDialogSeenUseCase(
                preferencesRepository = get()
            ),
            markWelcomeDialogSeen = MarkWelcomeDialogSeenUseCase(
                preferencesRepository = get()
            ),
            devSignIn = getOrNull()
        )
    }

    // Standalone SignOutUseCase - injected separately into OverviewViewModel
    single {
        SignOutUseCase(
            authRepository = get(),
            authStateRepository = get()
        )
    }

    // SignIn ViewModel
    viewModel {
        SignInViewModel(signInUseCases = get())
    }
}
