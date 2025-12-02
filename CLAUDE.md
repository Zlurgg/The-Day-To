# CLAUDE.md

This file provides guidance to Claude Code when working with code in this repository.

---

# The Day To - Modernization Guide

## Project Overview

**The Day To** is a daily mood logging Android application that allows users to track their emotional wellbeing through a visual calendar interface.

### Core Features
- **Daily Mood Logging**: One entry per day with user-defined mood, color, and notes
- **Custom Mood Colors**: Users create their own mood-to-color mappings via color picker
- **Calendar View**: Month-style calendar displaying each day's mood by color
- **Note Management**: Optional text notes for each mood entry, listed below calendar
- **Daily Notifications**: WorkManager-based reminders to log daily mood
- **Google Sign-In**: User authentication via Google/Firebase
- **Offline-First**: All data stored locally in Room database
- **In-App Updates**: Automatic update checks via GitHub Releases API with direct APK download

### Development Standards
This project follows **Google's official Modern Android Development (MAD)** recommendations:
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- [Jetpack Compose Best Practices](https://developer.android.com/jetpack/compose/architecture)
- [Material Design 3 Guidelines](https://m3.material.io/)
- [Guide to App Architecture](https://developer.android.com/topic/architecture/recommendations)

---

## Development Commands

### Building & Running
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew installDebug           # Install on device
./gradlew clean build            # Clean and rebuild
```

### Testing
```bash
./gradlew test                   # Run all unit tests (162 tests)
./gradlew connectedAndroidTest   # Run instrumented tests (20 tests)
./gradlew check                  # Run all checks (lint + tests)
```

**Test Coverage**: ✅ 182 tests total (162 unit + 20 instrumented) - All passing
- ViewModels: 78 unit tests (100% coverage)
- Use Cases: 84 unit tests (including update feature)
- Repositories: 20 integration tests with real Room database

### Code Quality
```bash
./gradlew lint                   # Run lint checks
./gradlew lintFix                # Auto-fix lint issues
```

---

## Architecture & Design Principles

### Architecture Pattern
**Clean Architecture with Unidirectional Data Flow (UDF)**

**Layer Structure:**
- **UI Layer** (`ui/`) - Compose UI + ViewModels (state holders)
- **Domain Layer** (`domain/`) - Pure business logic, models, repository interfaces, use cases
- **Data Layer** (`data/`) - Room entities, DAOs, repository implementations, mappers

**Key Principles:**
- Unidirectional Data Flow: Data flows down, events flow up
- Single Source of Truth: Each piece of data has one source
- Separation of Concerns: UI, domain, and data layers are independent
- Immutability: Use immutable data classes for state

### SOLID Principles
1. **Single Responsibility** - Each class/function has ONE clear purpose
2. **Open/Closed** - Open for extension, closed for modification
3. **Liskov Substitution** - Implementations should be interchangeable
4. **Interface Segregation** - Keep interfaces focused and minimal
5. **Dependency Inversion** - Depend on abstractions, not implementations

---

## Tech Stack

### Core
- **Language**: Kotlin 2.2.21
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture
- **Navigation**: Jetpack Navigation Compose v2.9.5

### Key Libraries
- **DI**: Koin 4.1.1 (constructor injection)
- **Database**: Room 2.8.3 (offline-first)
- **Networking**: Ktor 3.1.3 + OkHttp engine (GitHub API for updates)
- **Background Work**: WorkManager 2.11.0
- **Auth**: Credential Manager API (androidx.credentials v1.5.0, GoogleID v1.1.1)
- **Logging**: Timber 5.0.1
- **Testing**: JUnit 4, MockK 1.14.6, Turbine 1.2.1, kotlinx-coroutines-test 1.10.2

---

## Coding Standards

### Kotlin Best Practices
1. **Naming Conventions**
   - Classes/Objects: PascalCase
   - Functions/Variables: camelCase
   - Constants: UPPER_SNAKE_CASE
   - Composables: PascalCase

2. **Logging**
   - Use Timber for all logging (Timber.d/e/w)
   - Never use println() or Log.*

3. **Null Safety**
   - Avoid !! operator - use safe calls ?. or let/run
   - Use requireNotNull() with descriptive messages

4. **Coroutines & Flow**
   - Use viewModelScope in ViewModels
   - Prefer Flow over LiveData
   - Use collectAsStateWithLifecycle() in Composables

5. **Sealed Classes**
   - Use sealed interfaces for UI state variants
   - Use for one-time events
   - Prefer sealed interface over sealed class

### ViewModel State Management
**Pattern**: Single StateFlow<UiState> + SharedFlow for events

**Structure:**
- Private MutableStateFlow, public StateFlow (use .asStateFlow())
- Use .update {} for state changes
- Separate SharedFlow for one-time events (navigation, snackbar)
- Single source of truth for UI state

### Dependency Injection Rules
1. **ALL dependencies must be injected via Koin** (no manual instantiation)
2. **Use constructor injection** throughout
3. **No Context in ViewModels** (violates separation of concerns)

### Compose Best Practices
1. **Root/Presenter Pattern**
   - Root composable: [Feature]ScreenRoot - handles ViewModel, state collection, side effects
   - Presenter composable: [Feature]Screen (private) - pure UI with state + callbacks
   - Use koinViewModel() in Root, collectAsStateWithLifecycle()

2. **State Hoisting**
   - Pass state down, events up (unidirectional data flow)
   - Keep Composables stateless when possible

3. **Side Effects**
   - Use LaunchedEffect for one-time events
   - Use DisposableEffect for cleanup
   - Never launch coroutines directly in Composable body

4. **Preview Annotations**
   - Add @Preview for all major Composables
   - Include light and dark theme previews

---

## Data/Domain Separation

**Domain Layer** - Pure Kotlin, NO framework dependencies
- Models: Pure Kotlin data classes (no @Entity)
- Repository interfaces: Return/accept domain models
- Use cases: Single-responsibility business logic

**Data Layer** - Framework-specific implementations
- Entities: Room @Entity annotations
- DAOs: Work with entities
- Repository implementations: Use mappers to convert entity ↔ domain
- Mappers: Extension functions (toEntity/toDomain)

**Benefits:**
- Domain is framework-agnostic
- Easy testing with domain models
- Swap data sources without touching domain

---

## Testing Philosophy

Following Google's 2025 Android Testing Guidelines - prioritize fast, reliable unit tests.

### Testing Strategy
**ViewModels** - Unit tests with fake repositories (78 tests)
**Use Cases** - Unit tests with fake/mock repositories (59 tests)
**Repositories** - Integration tests with real Room in-memory database (20 tests)
**UI Components** - Future work (lower priority)

### Key Patterns
- Use fake repositories with MutableStateFlow for reactive behavior
- Collect SharedFlow events BEFORE triggering actions
- Test outcomes, not timing (use testScheduler.advanceUntilIdle())
- Use UnconfinedTestDispatcher for deterministic tests
- Use Turbine for Flow testing

---

## Release Status

**Current Version:** v1.0.6
**Release Date:** 2025-12-01
**Status:** ✅ Released on GitHub

### Release Configuration
- Signed APK with keystore (credentials in `keystore.properties`, gitignored)
- Release builds configured in `build.gradle.kts`
- Available at: [Releases](https://github.com/Zlurgg/The-Day-To/releases)

---

## Implementation Status

### ✅ Completed
- Clean Architecture with proper data/domain separation
- Modern package structure (journal/, auth/, core/, update/)
- All ViewModels follow StateFlow + SharedFlow pattern
- Root/Presenter pattern for all screens
- Comprehensive test coverage (182 tests, all passing)
- Timber logging throughout
- Modern Google Sign-In (Credential Manager API)
- Error handling with Resource wrapper
- Magic numbers extracted to constants
- Standardized padding values (Dimensions.kt + UiConstants)
- Portfolio-quality README with screenshots
- MIT License
- Release signing configuration
- R8 minification and resource shrinking enabled
- In-app update checker via GitHub Releases API

### 📋 Future Enhancements
- Resolve remaining TODOs in code
- UI component tests

---

## File Organization

### Feature Module Structure
```
[feature]/                        (e.g., journal/, auth/, update/)
├── data/
│   ├── model/                    <- Room entities (@Entity)
│   ├── mapper/                   <- Entity ↔ Domain conversion
│   ├── dao/                      <- Room DAOs
│   ├── remote/                   <- API services, DTOs (for network features)
│   └── repository/               <- Repository implementations
├── domain/
│   ├── model/                    <- Pure Kotlin models (no @Entity)
│   ├── repository/               <- Repository interfaces
│   └── usecases/                 <- Business logic
└── ui/
    └── [screen]/                 <- Screen UI (Screen, ViewModel, state/)
```

### Core Module
```
core/
├── data/                         <- Database config, shared repositories
├── domain/                       <- Shared repository interfaces, utilities
├── di/                           <- Koin DI modules
├── service/                      <- Notifications, background work
└── ui/                           <- Theme, shared UI components
```

---

## Anti-Patterns to Avoid

❌ Don't use !! operator
❌ Don't manually instantiate dependencies
❌ Don't use multiple mutable states in ViewModels
❌ Don't pass Context to ViewModels
❌ Don't use LiveData in new code (prefer StateFlow/Flow)
❌ Don't use magic numbers/strings
❌ Don't put business logic in Composables
❌ Don't collect Flow in Composable body (use side effects)
❌ Don't use cold Flow in fake repositories (use MutableStateFlow)

---

## References

### Official Google Documentation
- [Modern Android Development (MAD)](https://developer.android.com/modern-android-development)
- [Guide to App Architecture](https://developer.android.com/topic/architecture)
- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- [Jetpack Compose Architecture](https://developer.android.com/jetpack/compose/architecture)
- [ViewModel Best Practices](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Room Database Guide](https://developer.android.com/training/data-storage/room)
- [Android Testing Guide](https://developer.android.com/training/testing)

### Project Reference
- **Target Standard**: [My-Bookshelf](https://github.com/Zlurgg/My-Bookshelf)

---

Last Updated: 2025-11-28