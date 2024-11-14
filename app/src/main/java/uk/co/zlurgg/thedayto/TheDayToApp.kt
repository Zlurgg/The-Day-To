package uk.co.zlurgg.thedayto

import android.app.Application
import com.google.android.gms.auth.api.identity.Identity
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import uk.co.zlurgg.thedayto.di.addEditEntryModule
import uk.co.zlurgg.thedayto.di.addEditMoodColorModule
import uk.co.zlurgg.thedayto.di.appModule
import uk.co.zlurgg.thedayto.di.entriesModule
import uk.co.zlurgg.thedayto.di.signInModule
import uk.co.zlurgg.thedayto.feature_sign_in.presentation.GoogleAuthUiClient

class TheDayToApp : Application() {

    /** Firebase auth for google sign in **/
    val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TheDayToApp)
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