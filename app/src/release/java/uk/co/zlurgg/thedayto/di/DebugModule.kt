package uk.co.zlurgg.thedayto.di

import com.google.firebase.firestore.FirebaseFirestore
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Release module providing production Firebase services.
 * Debug-only features (emulators, dev sign-in) are not included.
 */
val releaseModule = module {
    // Production Firestore (no emulator)
    single { FirebaseFirestore.getInstance() }
}

/**
 * Module list for release builds.
 */
val debugModules: List<Module> = listOf(releaseModule)
