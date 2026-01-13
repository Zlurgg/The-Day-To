package uk.co.zlurgg.thedayto.update.di

import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import uk.co.zlurgg.thedayto.BuildConfig
import uk.co.zlurgg.thedayto.core.data.repository.PreferencesRepositoryImpl
import uk.co.zlurgg.thedayto.update.data.remote.api.GitHubApiService
import uk.co.zlurgg.thedayto.update.data.repository.UpdateRepositoryImpl
import uk.co.zlurgg.thedayto.update.data.service.ApkDownloadService
import uk.co.zlurgg.thedayto.update.domain.model.UpdateConfig
import uk.co.zlurgg.thedayto.update.domain.repository.UpdatePreferencesRepository
import uk.co.zlurgg.thedayto.update.domain.repository.UpdateRepository
import uk.co.zlurgg.thedayto.update.domain.usecases.CheckForUpdateUseCase
import uk.co.zlurgg.thedayto.update.domain.usecases.DismissUpdateUseCase
import uk.co.zlurgg.thedayto.update.domain.usecases.DownloadUpdateUseCase
import uk.co.zlurgg.thedayto.update.domain.usecases.GetCurrentVersionInfoUseCase

private const val GITHUB_API_BASE_URL = "https://api.github.com"
private const val CURRENT_VERSION = BuildConfig.VERSION_NAME

// Update feature configuration - change these values when using in another project
private const val GITHUB_OWNER = "Zlurgg"
private const val GITHUB_REPO = "The-Day-To"
private const val APP_NAME = "the-day-to"

val updateModule = module {

    // Configuration
    single {
        UpdateConfig(
            gitHubOwner = GITHUB_OWNER,
            gitHubRepo = GITHUB_REPO,
            appName = APP_NAME
        )
    }

    // GitHub API Service
    single {
        GitHubApiService(
            httpClient = get(),
            baseUrl = GITHUB_API_BASE_URL
        )
    }

    // APK Download Service
    single {
        val config = get<UpdateConfig>()
        ApkDownloadService(
            context = androidContext(),
            downloadTitle = config.downloadTitle
        )
    }

    // Update Repository
    single<UpdateRepository> {
        UpdateRepositoryImpl(
            gitHubApiService = get(),
            apkDownloadService = get(),
            config = get()
        )
    }

    // Update Preferences Repository binding
    single<UpdatePreferencesRepository> { get<PreferencesRepositoryImpl>() }

    // Use Cases
    single {
        CheckForUpdateUseCase(
            updateRepository = get(),
            updatePreferencesRepository = get(),
            currentVersion = CURRENT_VERSION
        )
    }

    single {
        DismissUpdateUseCase(updatePreferencesRepository = get())
    }

    single {
        DownloadUpdateUseCase(
            updateRepository = get(),
            config = get()
        )
    }

    single {
        GetCurrentVersionInfoUseCase(
            updateRepository = get(),
            currentVersion = CURRENT_VERSION
        )
    }
}
