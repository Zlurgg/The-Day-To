package uk.co.zlurgg.thedayto.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import timber.log.Timber
import uk.co.zlurgg.thedayto.BuildConfig
import uk.co.zlurgg.thedayto.auth.di.authModule
import uk.co.zlurgg.thedayto.core.di.coreModule
import uk.co.zlurgg.thedayto.journal.di.journalModule
import uk.co.zlurgg.thedayto.journal.di.journalViewModelModule
import uk.co.zlurgg.thedayto.update.di.updateModule

class TheDayToApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Initialize Koin dependency injection
        startKoin {
            androidContext(this@TheDayToApplication)
            androidLogger()

            modules(
                coreModule,
                updateModule,
                authModule,
                journalModule,
                journalViewModelModule
            )
        }
    }
}