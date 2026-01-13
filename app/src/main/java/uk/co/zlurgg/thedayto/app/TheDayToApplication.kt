package uk.co.zlurgg.thedayto.app

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import timber.log.Timber
import uk.co.zlurgg.thedayto.BuildConfig
import uk.co.zlurgg.thedayto.app.di.appModule

class TheDayToApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Initialize Timber for logging (DEBUG builds only)
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        startKoin {
            androidContext(this@TheDayToApplication)
            modules(appModule)
        }
    }
}