package uk.co.zlurgg.thedayto.auth.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.auth.data.repository.AuthRepositoryImpl
import uk.co.zlurgg.thedayto.auth.data.repository.AuthStateRepositoryImpl
import uk.co.zlurgg.thedayto.auth.data.service.CredentialProviderFactoryImpl
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthRepository
import uk.co.zlurgg.thedayto.auth.domain.repository.AuthStateRepository
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckSignInStatusUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckTodayEntryUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.CheckWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.MarkWelcomeDialogSeenUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCase
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignInUseCases
import uk.co.zlurgg.thedayto.auth.domain.usecases.SignOutUseCase
import uk.co.zlurgg.thedayto.auth.ui.CredentialProviderFactory
import uk.co.zlurgg.thedayto.auth.ui.SignInViewModel

val authModule = module {

    // Credential Provider Factory (injected into UI layer)
    single<CredentialProviderFactory> { CredentialProviderFactoryImpl() }

    // Auth Repository (wraps GoogleAuthUiClient)
    single<AuthRepository> {
        AuthRepositoryImpl(context = androidContext())
    }

    // Auth State Repository
    single<AuthStateRepository> {
        AuthStateRepositoryImpl(androidContext())
    }

    // Standalone SignInUseCase - used by SyncSettingsViewModel
    single {
        SignInUseCase(
            authRepository = get(),
            authStateRepository = get()
        )
    }

    // Standalone SignOutUseCase - used by SyncSettingsViewModel
    single {
        SignOutUseCase(
            authRepository = get(),
            authStateRepository = get()
        )
    }

    // Standalone CheckWelcomeDialogSeenUseCase - used by TheDayToApp
    single {
        CheckWelcomeDialogSeenUseCase(
            preferencesRepository = get()
        )
    }

    // Standalone MarkWelcomeDialogSeenUseCase - used by TheDayToApp
    single {
        MarkWelcomeDialogSeenUseCase(
            preferencesRepository = get()
        )
    }

    // Auth UseCases
    single {
        SignInUseCases(
            signIn = get(),
            checkSignInStatus = CheckSignInStatusUseCase(
                authRepository = get(),
                authStateRepository = get()
            ),
            checkTodayEntry = CheckTodayEntryUseCase(entryRepository = get()),
            seedDefaultMoodColors = get(),
            checkWelcomeDialogSeen = get(),
            markWelcomeDialogSeen = get(),
            devSignIn = getOrNull()
        )
    }

    // SignIn ViewModel
    viewModel {
        SignInViewModel(signInUseCases = get())
    }
}
