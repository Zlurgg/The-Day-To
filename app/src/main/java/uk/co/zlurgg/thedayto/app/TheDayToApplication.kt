package uk.co.zlurgg.thedayto.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import uk.co.zlurgg.thedayto.BuildConfig
import uk.co.zlurgg.thedayto.di.addEditEntryModule
import uk.co.zlurgg.thedayto.di.appModule
import uk.co.zlurgg.thedayto.di.entriesModule
import uk.co.zlurgg.thedayto.di.signInModule

class TheDayToApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@TheDayToApplication)
            androidLogger()

            modules(
                appModule,
                addEditEntryModule,
                entriesModule,
                signInModule
            )
        }
    }
}