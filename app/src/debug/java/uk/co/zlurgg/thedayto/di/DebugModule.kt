package uk.co.zlurgg.thedayto.di

import org.koin.dsl.module
import uk.co.zlurgg.thedayto.auth.data.service.FirebaseEmulatorAuthService
import uk.co.zlurgg.thedayto.auth.domain.service.DevAuthService
import uk.co.zlurgg.thedayto.auth.domain.usecases.DevSignInUseCase

/**
 * Debug-only Koin module providing Firebase Emulator authentication.
 */
val debugModule = module {
    single<DevAuthService> { FirebaseEmulatorAuthService() }
    single { DevSignInUseCase(devAuthService = get(), authStateRepository = get()) }
}

/**
 * List of debug modules to be added to appModule.
 */
val debugModules = listOf(debugModule)
