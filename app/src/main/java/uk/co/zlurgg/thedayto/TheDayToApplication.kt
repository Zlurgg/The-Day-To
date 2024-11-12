package uk.co.zlurgg.thedayto

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.HiltAndroidApp
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.di.addEditEntryModule
import uk.co.zlurgg.thedayto.di.addEditMoodColorModule
import uk.co.zlurgg.thedayto.di.appModule
import uk.co.zlurgg.thedayto.di.entriesModule
import uk.co.zlurgg.thedayto.di.signInModule
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.add_edit_daily_entry.AddEditEntryViewModel
import uk.co.zlurgg.thedayto.feature_daily_entry.presentation.display_daily_entries.EntriesViewModel
import uk.co.zlurgg.thedayto.feature_mood_color.presentation.AddEditMoodColorViewModel

class TheDayToApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TheDayToApplication)
            androidLogger()

            modules(appModule, addEditEntryModule, addEditMoodColorModule, entriesModule, signInModule)
        }
    }
}