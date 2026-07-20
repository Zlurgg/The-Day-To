# Project Overview

The Day To is a daily mood logging Android application built with Kotlin and Jetpack Compose.

## Branch Strategy

> ⚠️ **`local-only` is the current release branch — it is what ships to the Play Store.**

- **`local-only`** — the active **release branch**. Google Sign-In / cloud sync are disabled (see [Local-Only Mode](#local-only-mode)). All Play Store releases are cut from here (tag `vX.Y.Z` → see [Releasing](#releasing)).
- **`main`** — development-only. Holds the full cloud-sync feature set; **not** shipped.

Cut releases from `local-only` and cherry-pick fixes between the two branches.

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

## Releasing

Releases ship from the **`local-only`** branch via GitHub Actions (`.github/workflows/release.yml`):

1. Bump `versionCode` + `versionName` in `app/build.gradle.kts` (commit as `chore(release): bump version to X.Y.Z`).
2. Push the branch, then push a tag `vX.Y.Z` — **pushing a `v*` tag triggers the release workflow.**
3. CI builds the signed AAB (`bundleRelease`) and uploads it to the Play Store **internal** track as a **draft** (it does not auto-publish).
4. Review/roll out the draft in the Play Console, test, then **promote to production manually** (or run the workflow's `workflow_dispatch` with `track: production`).

Each upload needs a unique, higher `versionCode`. Signing keys come from repo secrets in CI (locally: `keystore.properties` + `.jks` at repo root).

## Local-Only Mode

Cloud sync and Google sign-in are currently disabled. Firestore does not encrypt journal data at rest, so all data stays on-device until encryption is implemented.

- **Feature flag:** `CLOUD_SYNC_ENABLED` in `core/data/util/Constants.kt`
- **Set to `true`** to re-enable the account button, Google sign-in, and cloud sync
- **Content to revert:** `strings.xml` about dialog text and `docs/privacy.html` were simplified for local-only and will need updating when cloud sync returns

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
