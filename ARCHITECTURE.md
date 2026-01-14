# Architecture

The Day To is a daily mood logging Android application built with Kotlin and Jetpack Compose.

## Quick Start

```bash
# Build
./gradlew assembleDebug

# Run tests
./gradlew test                   # 257 unit tests
./gradlew connectedAndroidTest   # 49 integration tests

# Code quality
./gradlew detekt                 # Static analysis
./gradlew lint                   # Android lint

# Release
./gradlew assembleRelease
```

## Firebase Emulator (Debug Builds)

Debug builds auto-connect to local Firebase Auth Emulator.

```bash
# Start emulator
firebase emulators:start

# Emulator UI: http://localhost:4000
# Create test user: test@example.com / password123
```

- Debug builds connect to `10.0.2.2` (Android emulator's localhost alias)
- Release builds use production Google Sign-In

## Architecture

### Clean Architecture Layers

```
Presentation (ViewModels, Compose UI)
        ↓
    Domain (UseCases, Models, Repository Interfaces)
        ↓
    Data (Room, Ktor, Repository Implementations)
```

**Key principle**: ViewModels depend only on UseCases, never repositories.

### Package Structure

```
uk.co.zlurgg.thedayto/
├── app/                    # Application setup and navigation
├── di/                     # Root DI aggregator
├── core/                   # Shared infrastructure
│   ├── data/              # Database config, shared repositories
│   ├── domain/            # Error types, Result pattern
│   ├── service/           # Notifications, background work
│   └── ui/                # Theme, shared UI components
├── auth/                   # Authentication feature
├── update/                 # In-app update feature
└── journal/               # Main journal feature
    ├── data/              # Room entities, repositories
    ├── domain/            # Entry/MoodColor models, UseCases
    └── ui/                # Screens, ViewModels, components
```

### Key Patterns

- **Repository Pattern**: Interfaces in domain, implementations in data
- **UseCase Pattern**: Single-responsibility business logic classes
- **Result Pattern**: `Result<T, DataError>` for error handling
- **State Management**: ViewModel + StateFlow, unidirectional data flow
- **Mapper Pattern**: Entities ↔ Domain models

### Dependency Injection (Koin)

Feature-scoped modules:
- `CoreModule` - Infrastructure (database, preferences)
- `AuthModule` - Authentication
- `UpdateModule` - In-app updates
- `JournalModule` - Main feature
- `DebugModule` - Debug-only bindings (empty in release)

## Tech Stack

| Component | Technology |
|-----------|------------|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Clean Architecture |
| DI | Koin 4.1.1 |
| Database | Room 2.8.3 |
| Network | Ktor 3.1.3 |
| Background Work | WorkManager 2.11.0 |
| Auth | Firebase Auth, Credential Manager API |
| Logging | Timber |
| Testing | JUnit 4, MockK, Turbine |
| Static Analysis | Detekt |

## Build Configuration

- Android SDK: Target 35, Min 28
- Kotlin: 2.2.21
- R8/ProGuard enabled for release
- Room schema version: 2

## Debug/Release Source Sets

```
app/src/
├── main/     # Shared code
├── debug/    # Firebase emulator auth, DevSignInButton
└── release/  # No-op stubs (empty DevSignInButton)
```

## Metrics

- 306 tests (257 unit + 49 integration)
- All tests passing
- Current version: v1.0.9
