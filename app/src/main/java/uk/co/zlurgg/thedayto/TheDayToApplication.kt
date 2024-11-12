package uk.co.zlurgg.thedayto

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import uk.co.zlurgg.thedayto.di.addEditEntryModule
import uk.co.zlurgg.thedayto.di.addEditMoodColorModule
import uk.co.zlurgg.thedayto.di.appModule
import uk.co.zlurgg.thedayto.di.entriesModule
import uk.co.zlurgg.thedayto.di.signInModule

class TheDayToApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TheDayToApplication)
            androidLogger()

            modules(
                appModule,
                addEditEntryModule,
                addEditMoodColorModule,
                entriesModule,
                signInModule
            )
        }
    }
}