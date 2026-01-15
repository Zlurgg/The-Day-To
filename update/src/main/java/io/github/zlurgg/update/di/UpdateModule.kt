package io.github.zlurgg.update.di

import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import io.github.zlurgg.update.data.remote.api.GitHubApiService
import io.github.zlurgg.update.data.repository.UpdateRepositoryImpl
import io.github.zlurgg.update.data.service.ApkDownloadService
import io.github.zlurgg.update.domain.model.UpdateConfig
import io.github.zlurgg.update.domain.repository.UpdateRepository
import io.github.zlurgg.update.domain.usecases.CheckForUpdateUseCase
import io.github.zlurgg.update.domain.usecases.DismissUpdateUseCase
import io.github.zlurgg.update.domain.usecases.DownloadUpdateUseCase
import io.github.zlurgg.update.domain.usecases.GetCurrentVersionInfoUseCase

private const val GITHUB_API_BASE_URL = "https://api.github.com"

/**
 * Koin module for the update feature.
 *
 * Consumer must provide before loading this module:
 * - UpdateConfig: Configuration for GitHub repo and app name
 * - HttpClient: Ktor HTTP client for API calls
 * - UpdatePreferencesRepository: Implementation for storing dismissed versions
 * - Named "currentVersion": String with the app's current version
 *
 * Example setup in consumer app:
 * ```kotlin
 * single { UpdateConfig(gitHubOwner = "...", gitHubRepo = "...", appName = "...") }
 * single(named("currentVersion")) { BuildConfig.VERSION_NAME }
 * single<UpdatePreferencesRepository> { get<PreferencesRepositoryImpl>() }
 * ```
 */
val updateModule = module {

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

    // Use Cases
    single {
        CheckForUpdateUseCase(
            updateRepository = get(),
            updatePreferencesRepository = get(),
            currentVersion = get(named("currentVersion"))
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
            currentVersion = get(named("currentVersion"))
        )
    }
}
