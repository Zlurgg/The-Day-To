# Project Overview

The Day To is a daily mood logging Android application built with Kotlin and Jetpack Compose.

## Quick Start

```bash
# Build
./gradlew assembleDebug

# Run tests
./gradlew test                   # Unit tests
./gradlew connectedAndroidTest   # Integration tests

# Code quality
./gradlew detekt                 # Static analysis

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

### Emulator Host Configuration

The emulator host is configured in `local.properties` (not checked into git):

```properties
# For Android Emulator (default)
firebase.emulator.host=10.0.2.2

# For physical device (use your machine's IP)
firebase.emulator.host=192.168.1.x
```

- **Android Emulator**: Uses `10.0.2.2` (localhost alias) - this is the default
- **Physical Device**: Must use your machine's actual IP address, and phone must be on same network
- **Release builds**: Use production Google Sign-In (emulator not used)

## Package Structure

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
└── journal/               # Main journal feature
    ├── data/              # Room entities, repositories
    ├── domain/            # Entry/MoodColor models, UseCases
    └── ui/                # Screens, ViewModels, components
```

## Tech Stack

| Component | Technology |
|-----------|------------|
| UI | Jetpack Compose, Material 3 |
| Architecture | MVVM, Clean Architecture |
| DI | Koin |
| Database | Room |
| Background Work | WorkManager |
| Auth | Firebase Auth, Credential Manager API |
| Logging | Timber |
| Testing | JUnit 4, MockK, Turbine |
| Static Analysis | Detekt |

## Build Configuration

- Target SDK: 36
- Min SDK: 27
- Java: 11
- R8/ProGuard enabled for release

## Source Sets

```
app/src/
├── main/     # Shared code
├── debug/    # Firebase emulator auth, DevSignInButton
└── release/  # No-op stubs (empty DevSignInButton)
```

## Koin Modules

- `CoreModule` - Infrastructure (database, preferences)
- `AuthModule` - Authentication
- `JournalModule` - Main feature
- `DebugModule` - Debug-only bindings (empty in release)

## Documentation

| Document | Purpose |
|----------|---------|
| `docs/specs/constitution.md` | Architectural principles |
| `docs/specs/style/code-style.md` | Naming conventions, testing |
| `docs/specs/patterns/` | Implementation patterns |
| `CLAUDE.local.md` | AI assistant guidance |
