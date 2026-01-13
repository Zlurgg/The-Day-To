# Update Package Integration Guide

This document explains how to integrate the reusable `update` package into another Android project. The update package provides GitHub Releases-based in-app update checking and APK downloading.

## Overview

The update package allows users to:
- Check for new app versions via GitHub Releases API
- View changelog/release notes
- Download and install APK updates directly
- Dismiss updates they don't want to install

## Prerequisites

Ensure your project has these dependencies in `build.gradle.kts`:

```kotlin
// Ktor for API calls (Kotlin-first HTTP client)
implementation("io.ktor:ktor-client-core:3.1.3")
implementation("io.ktor:ktor-client-okhttp:3.1.3")
implementation("io.ktor:ktor-client-content-negotiation:3.1.3")
implementation("io.ktor:ktor-serialization-kotlinx-json:3.1.3")
implementation("io.ktor:ktor-client-logging:3.1.3")

// Kotlin Serialization (for JSON parsing)
implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")

// Timber for logging
implementation("com.jakewharton.timber:timber:5.0.1")

// Koin for DI (or adapt to your DI framework)
implementation("io.insert-koin:koin-android:4.1.1")
implementation("io.insert-koin:koin-androidx-compose:4.1.1")
```

Also ensure you have the Kotlin Serialization plugin in your `build.gradle.kts`:

```kotlin
plugins {
    // ... other plugins
    alias(libs.plugins.kotlin.serialization)
}
```

## Files to Copy

Copy the entire `update/` package from the source project:

```
app/src/main/java/uk/co/zlurgg/thedayto/update/
├── data/
│   ├── mapper/
│   │   └── UpdateMapper.kt
│   ├── remote/
│   │   ├── api/
│   │   │   └── GitHubApiService.kt
│   │   └── dto/
│   │       └── GitHubReleaseDto.kt
│   ├── repository/
│   │   └── UpdateRepositoryImpl.kt
│   └── service/
│       └── ApkDownloadService.kt
├── domain/
│   ├── model/
│   │   ├── UpdateConfig.kt
│   │   └── UpdateInfo.kt
│   ├── repository/
│   │   ├── UpdatePreferencesRepository.kt
│   │   └── UpdateRepository.kt
│   └── usecases/
│       ├── CheckForUpdateUseCase.kt
│       ├── DismissUpdateUseCase.kt
│       ├── DownloadUpdateUseCase.kt
│       └── GetCurrentVersionInfoUseCase.kt
└── ui/
    └── components/
        ├── UpdateDialog.kt
        └── UpToDateDialog.kt
```

After copying, update the package declarations in all files from `uk.co.zlurgg.thedayto.update` to your project's package (e.g., `uk.co.zlurgg.mybookshelf.update`).

## Required Interface Implementation

The update package requires an implementation of `UpdatePreferencesRepository`:

```kotlin
interface UpdatePreferencesRepository {
    suspend fun getDismissedVersion(): String?
    suspend fun setDismissedVersion(version: String)
}
```

### Option 1: Add to existing PreferencesRepository

If you have an existing preferences repository, add these methods and implement the interface:

```kotlin
class PreferencesRepositoryImpl(
    context: Context
) : PreferencesRepository, UpdatePreferencesRepository {

    private val prefs = context.getSharedPreferences("your_prefs", Context.MODE_PRIVATE)

    // ... your existing methods ...

    override suspend fun getDismissedVersion(): String? {
        return prefs.getString("dismissed_update_version", null)
    }

    override suspend fun setDismissedVersion(version: String) {
        prefs.edit { putString("dismissed_update_version", version) }
    }
}
```

### Option 2: Create dedicated implementation

```kotlin
class UpdatePreferencesRepositoryImpl(
    context: Context
) : UpdatePreferencesRepository {

    private val prefs = context.getSharedPreferences("update_prefs", Context.MODE_PRIVATE)

    override suspend fun getDismissedVersion(): String? {
        return prefs.getString("dismissed_update_version", null)
    }

    override suspend fun setDismissedVersion(version: String) {
        prefs.edit { putString("dismissed_update_version", version) }
    }
}
```

## DI Setup (Koin)

Add the following to your Koin module:

```kotlin
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import timber.log.Timber
import java.util.concurrent.TimeUnit

// Constants - customize these for your app
private const val GITHUB_API_BASE_URL = "https://api.github.com"
private const val NETWORK_TIMEOUT_SECONDS = 30L
private const val GITHUB_OWNER = "YourGitHubUsername"    // e.g., "Zlurgg"
private const val GITHUB_REPO = "Your-Repo-Name"         // e.g., "My-Bookshelf"
private const val APP_NAME = "your-app-name"             // Used in APK filename
private const val CURRENT_VERSION = BuildConfig.VERSION_NAME

// In your module definition:
val appModule = module {

    // Network - Ktor HttpClient
    single {
        HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    readTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                    writeTimeout(NETWORK_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                }
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d(message)
                    }
                }
                level = LogLevel.INFO
            }
        }
    }

    // GitHub API Service
    single {
        GitHubApiService(
            httpClient = get(),
            baseUrl = GITHUB_API_BASE_URL
        )
    }

    // Update Feature - Configuration
    single {
        UpdateConfig(
            gitHubOwner = GITHUB_OWNER,
            gitHubRepo = GITHUB_REPO,
            appName = APP_NAME
        )
    }

    // Update Feature - APK Download Service
    single {
        val config = get<UpdateConfig>()
        ApkDownloadService(
            context = androidContext(),
            downloadTitle = config.downloadTitle
        )
    }

    // Update Feature - Repository
    single<UpdateRepository> {
        UpdateRepositoryImpl(
            gitHubApiService = get(),
            apkDownloadService = get(),
            config = get()
        )
    }

    // Update Feature - Preferences Repository
    // Option 1: If using existing PreferencesRepositoryImpl
    single<UpdatePreferencesRepository> { get<PreferencesRepositoryImpl>() }

    // Option 2: If using dedicated implementation
    // single<UpdatePreferencesRepository> { UpdatePreferencesRepositoryImpl(androidContext()) }

    // Update Use Cases
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
```

## String Resources

Add these to your `res/values/strings.xml`:

```xml
<!-- Update Dialog -->
<string name="update_available_title">Update Available</string>
<string name="update_version_label">Version</string>
<string name="update_whats_new_label">What\'s New</string>
<string name="update_download_button">Download</string>
<string name="update_not_now_button">Not Now</string>
<string name="update_up_to_date_title">You\'re Up to Date</string>
<string name="update_current_version_label">Current Version</string>
<string name="update_in_this_version_label">In This Version</string>
<string name="update_ok_button">OK</string>
```

## UI Integration

### Using UpdateDialog

```kotlin
@Composable
fun YourScreen(
    // ...
) {
    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateInfo by remember { mutableStateOf<UpdateInfo?>(null) }

    // Show dialog when update is available
    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            updateInfo = updateInfo!!,
            onDownload = {
                // Call downloadUpdate use case
                viewModel.downloadUpdate(updateInfo!!)
                showUpdateDialog = false
            },
            onDismiss = {
                // Call dismissUpdate use case
                viewModel.dismissUpdate(updateInfo!!.versionName)
                showUpdateDialog = false
            }
        )
    }
}
```

### Using UpToDateDialog

```kotlin
if (showUpToDateDialog && currentVersionInfo != null) {
    UpToDateDialog(
        updateInfo = currentVersionInfo!!,
        onDismiss = { showUpToDateDialog = false }
    )
}
```

## ViewModel Integration Example

```kotlin
class YourViewModel(
    private val checkForUpdateUseCase: CheckForUpdateUseCase,
    private val dismissUpdateUseCase: DismissUpdateUseCase,
    private val downloadUpdateUseCase: DownloadUpdateUseCase,
    private val getCurrentVersionInfoUseCase: GetCurrentVersionInfoUseCase
) : ViewModel() {

    private val _updateInfo = MutableStateFlow<UpdateInfo?>(null)
    val updateInfo: StateFlow<UpdateInfo?> = _updateInfo.asStateFlow()

    fun checkForUpdate(forceCheck: Boolean = false) {
        viewModelScope.launch {
            val update = checkForUpdateUseCase(forceCheck)
            _updateInfo.value = update
        }
    }

    fun dismissUpdate(version: String) {
        viewModelScope.launch {
            dismissUpdateUseCase(version)
            _updateInfo.value = null
        }
    }

    fun downloadUpdate(updateInfo: UpdateInfo) {
        downloadUpdateUseCase(updateInfo)
    }

    fun getCurrentVersionInfo() {
        viewModelScope.launch {
            val info = getCurrentVersionInfoUseCase()
            // Handle showing "up to date" dialog
        }
    }
}
```

## GitHub Release Setup

For the update checker to work, your GitHub releases must:

1. **Use semantic versioning tags**: `v1.0.0`, `v1.0.1`, etc.
2. **Attach APK as release asset**: Upload your signed APK to each release
3. **Include release notes**: The body of the release becomes the changelog

Example release structure:
```
Tag: v1.0.1
Title: Version 1.0.1
Body:
## Features
- New feature X
- Improved performance

## Bug Fixes
- Fixed crash on startup

Assets:
- my-bookshelf-v1.0.1.apk
```

## Testing

For unit tests, copy the test fake:

```kotlin
class FakeUpdateRepository : UpdateRepository {
    private var latestReleaseResult: Result<UpdateInfo>? = null
    // ... see FakeUpdateRepository.kt in source project
}
```

Your existing `FakePreferencesRepository` should implement `UpdatePreferencesRepository`:

```kotlin
class FakePreferencesRepository : PreferencesRepository, UpdatePreferencesRepository {
    private var dismissedVersion: String? = null

    override suspend fun getDismissedVersion(): String? = dismissedVersion
    override suspend fun setDismissedVersion(version: String) {
        dismissedVersion = version
    }

    // Helper for tests
    fun setDismissedVersionForTest(version: String?) {
        dismissedVersion = version
    }
}
```

## Checklist

- [ ] Copy `update/` package and update package names
- [ ] Add Ktor and kotlinx-serialization dependencies (if not present)
- [ ] Add Kotlin Serialization plugin to build.gradle.kts
- [ ] Implement `UpdatePreferencesRepository`
- [ ] Configure DI with your GitHub owner/repo/app name
- [ ] Add string resources
- [ ] Integrate UI dialogs into your screens
- [ ] Create GitHub releases with APK attachments
- [ ] Test update flow end-to-end

## Notes

- The APK filename format is `{appName}-{version}.apk` (e.g., `my-bookshelf-v1.0.1.apk`)
- Downloads use Android's `DownloadManager` for background downloading
- Users can dismiss updates; dismissed versions won't prompt again unless force-checked
- The `forceCheck` parameter bypasses the dismissed version check (useful for manual "Check for updates" button)

## Why Ktor?

This package uses Ktor instead of Retrofit because:
- **Kotlin-first**: Ktor is built from the ground up with Kotlin and coroutines
- **kotlinx.serialization**: Uses Kotlin's official serialization library instead of Gson
- **Lightweight**: Simpler API for basic HTTP calls
- **Recommended**: Ktor is the recommended HTTP client for modern Android/Kotlin development
