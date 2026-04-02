package uk.co.zlurgg.thedayto.di

import com.google.firebase.firestore.FirebaseFirestore
import org.koin.dsl.module
import timber.log.Timber
import uk.co.zlurgg.thedayto.BuildConfig
import uk.co.zlurgg.thedayto.auth.data.service.FirebaseEmulatorAuthService
import uk.co.zlurgg.thedayto.auth.domain.service.DevAuthService
import uk.co.zlurgg.thedayto.auth.domain.usecases.DevSignInUseCase

/**
 * Debug-only Koin module providing Firebase Emulator services.
 *
 * Connects to Firebase Auth and Firestore emulators for local development.
 */
val debugModule = module {
    // Dev Auth Service for emulator sign-in
    single<DevAuthService> { FirebaseEmulatorAuthService() }
    single { DevSignInUseCase(devAuthService = get(), authStateRepository = get()) }

    // Override Firestore to use emulator (must be defined before syncModule loads)
    single {
        val firestore = FirebaseFirestore.getInstance()
        try {
            firestore.useEmulator(BuildConfig.FIREBASE_EMULATOR_HOST, FIRESTORE_EMULATOR_PORT)
            Timber.i(
                "Firestore Emulator configured at %s:%d. " +
                    "If not running, start with: scripts\\start-emulator.bat",
                BuildConfig.FIREBASE_EMULATOR_HOST,
                FIRESTORE_EMULATOR_PORT
            )
        } catch (e: IllegalStateException) {
            Timber.d("Firebase Firestore Emulator already connected")
        }
        firestore
    }
}

private const val FIRESTORE_EMULATOR_PORT = 8080

/**
 * List of debug modules to be added to appModule.
 * Note: debugModule must come BEFORE syncModule to override Firestore binding.
 */
val debugModules = listOf(debugModule)
