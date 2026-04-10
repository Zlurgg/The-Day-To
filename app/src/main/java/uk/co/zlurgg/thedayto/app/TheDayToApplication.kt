package uk.co.zlurgg.thedayto.app

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.java.KoinJavaComponent.get
import timber.log.Timber
import uk.co.zlurgg.thedayto.BuildConfig
import uk.co.zlurgg.thedayto.app.di.appModule
import uk.co.zlurgg.thedayto.journal.domain.usecases.shared.moodcolor.SeedDefaultMoodColorsUseCase
import uk.co.zlurgg.thedayto.sync.data.worker.SyncScheduler

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

        // Seed default mood colors on first launch (idempotent)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            val seedDefaultMoodColorsUseCase: SeedDefaultMoodColorsUseCase =
                get(SeedDefaultMoodColorsUseCase::class.java)
            seedDefaultMoodColorsUseCase()
        }

        // Sync on app backgrounding
        setupAppLifecycleSync()
    }

    /**
     * Setup ProcessLifecycleOwner observer to sync when app goes to background.
     * Uses ExistingWorkPolicy.KEEP to avoid canceling in-progress sync.
     */
    private fun setupAppLifecycleSync() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStop(owner: LifecycleOwner) {
                    Timber.d("App going to background, triggering sync")
                    val syncScheduler: SyncScheduler = get(SyncScheduler::class.java)
                    syncScheduler.requestImmediateSync()
                }
            },
        )
    }
}
