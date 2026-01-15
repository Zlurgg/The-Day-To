# Plan: Create Independent `:core` Module

## Overview

Extract foundational types into a standalone, fully testable `:core` Gradle library module. These types are pure Kotlin with no Android dependencies.

**Package name:** `io.github.zlurgg.core` (generic, reusable across projects)

**Files to extract (7 total):**
- Error handling: `Error.kt`, `DataError.kt`, `ErrorFormatter.kt`, `ErrorMapper.kt`
- Result type: `Result.kt`
- Utilities: `OrderType.kt`, `DateUtils.kt`

---

## Phase 1: Project Configuration

### 1.1 Add android-library plugin to root `gradle/libs.versions.toml`
```toml
[plugins]
android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
```

### 1.2 Register plugin in root `build.gradle.kts`
```kotlin
alias(libs.plugins.android.library) apply false
```

### 1.3 Update root `settings.gradle`
```groovy
include ':app'
include ':core'
```

**Note:** The root project still needs the plugin registered for Gradle sync, even though the core module has its own version catalog.

---

## Phase 2: Create `:core` Module Structure

### 2.1 Create directories
```
core/
├── build.gradle.kts
├── settings.gradle.kts          # Enables standalone builds
├── consumer-rules.pro
├── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml       # Module's own version catalog
└── src/
    ├── main/java/io/github/zlurgg/core/domain/
    │   ├── error/
    │   │   ├── Error.kt
    │   │   ├── DataError.kt
    │   │   ├── ErrorFormatter.kt
    │   │   └── ErrorMapper.kt
    │   ├── result/
    │   │   └── Result.kt
    │   └── util/
    │       ├── OrderType.kt
    │       └── DateUtils.kt
    └── test/java/io/github/zlurgg/core/domain/
        ├── error/
        │   ├── ErrorFormatterTest.kt
        │   └── ErrorMapperTest.kt
        └── result/
            └── ResultTest.kt
```

### 2.2 Create `core/gradle/libs.versions.toml` (module's own version catalog)
```toml
[versions]
kotlin = "2.3.0"
androidGradlePlugin = "8.13.2"
coroutines = "1.10.2"
ktor = "3.3.3"
timber = "5.0.1"
junit = "4.13.2"

[libraries]
kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
ktor-client-core = { group = "io.ktor", name = "ktor-client-core", version.ref = "ktor" }
timber = { group = "com.jakewharton.timber", name = "timber", version.ref = "timber" }
junit = { group = "junit", name = "junit", version.ref = "junit" }

[plugins]
android-library = { id = "com.android.library", version.ref = "androidGradlePlugin" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
```

### 2.3 Create `core/settings.gradle.kts` (enables version catalog for module)
```kotlin
dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "core"
```

### 2.4 Create `core/build.gradle.kts`
```kotlin
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.github.zlurgg.core"
    compileSdk = 36

    defaultConfig {
        minSdk = 27
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlin {
        compilerOptions {
            jvmTarget = org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11
        }
    }
}

dependencies {
    // Coroutines (for ErrorMapper cancellation handling)
    implementation(libs.kotlinx.coroutines.core)

    // Ktor (for network exception types in ErrorMapper)
    implementation(libs.ktor.client.core)

    // Logging
    implementation(libs.timber)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}
```

---

## Phase 3: Move Source Files

| From (app) | To (core) |
|------------|-----------|
| `app/.../uk/co/zlurgg/thedayto/core/domain/error/Error.kt` | `core/.../io/github/zlurgg/core/domain/error/Error.kt` |
| `app/.../uk/co/zlurgg/thedayto/core/domain/error/DataError.kt` | `core/.../io/github/zlurgg/core/domain/error/DataError.kt` |
| `app/.../uk/co/zlurgg/thedayto/core/domain/error/ErrorFormatter.kt` | `core/.../io/github/zlurgg/core/domain/error/ErrorFormatter.kt` |
| `app/.../uk/co/zlurgg/thedayto/core/domain/error/ErrorMapper.kt` | `core/.../io/github/zlurgg/core/domain/error/ErrorMapper.kt` |
| `app/.../uk/co/zlurgg/thedayto/core/domain/result/Result.kt` | `core/.../io/github/zlurgg/core/domain/result/Result.kt` |
| `app/.../uk/co/zlurgg/thedayto/core/domain/util/OrderType.kt` | `core/.../io/github/zlurgg/core/domain/util/OrderType.kt` |
| `app/.../uk/co/zlurgg/thedayto/core/domain/util/DateUtils.kt` | `core/.../io/github/zlurgg/core/domain/util/DateUtils.kt` |

### 3.1 Update package declarations in moved files
Change from:
```kotlin
package uk.co.zlurgg.thedayto.core.domain.error
```
To:
```kotlin
package io.github.zlurgg.core.domain.error
```

---

## Phase 4: Write Unit Tests

### 4.1 `ResultTest.kt` - Test all extension functions
- `map()` - transforms success data
- `flatMap()` - chains Result operations
- `onSuccess()` / `onError()` - side effects
- `getOrNull()` / `getOrDefault()` / `getOrElse()`
- `fold()` - pattern matching
- `asEmptyResult()`

### 4.2 `ErrorFormatterTest.kt` - Test all error formats
- Each `DataError.Remote` variant
- Each `DataError.Local` variant
- Each `DataError.Validation` variant
- Each `DataError.Auth` variant
- With and without operation context

### 4.3 `ErrorMapperTest.kt` - Test exception mapping
- `IOException` → `NO_INTERNET`
- `SocketTimeoutException` → `REQUEST_TIMEOUT`
- `UnknownHostException` → `NO_INTERNET`
- `UnresolvedAddressException` → `NO_INTERNET`
- Generic `Exception` → `UNKNOWN`
- `safeSuspendCall()` success and error paths

---

## Phase 5: Update `:app` Module

### 5.1 Add dependency in `app/build.gradle.kts`
```kotlin
dependencies {
    implementation(project(":core"))
    // ... existing dependencies
}
```

### 5.2 Update imports throughout app
Since the package name changed, update all imports from:
```kotlin
import uk.co.zlurgg.thedayto.core.domain.error.DataError
import uk.co.zlurgg.thedayto.core.domain.result.Result
```
To:
```kotlin
import io.github.zlurgg.core.domain.error.DataError
import io.github.zlurgg.core.domain.result.Result
```

**Files that need import updates (~59 files using these types):**
- All repository implementations
- All use cases
- All ViewModels
- Test fakes

### 5.3 Clean up duplicate files
Delete the moved files from `app/src/main/java/uk/co/zlurgg/thedayto/core/domain/error/` and `.../result/` and `.../util/`

---

## Phase 6: Verification

### Build
```bash
./gradlew clean assembleDebug
```

### Test core module specifically
```bash
./gradlew :core:test
```

### Test entire project
```bash
./gradlew test
```

### Verify app still works
1. Launch app
2. Test a flow that uses Result (e.g., sign in, load entries)
3. Verify error handling works (e.g., turn off network, try sign in)

---

## Files Summary

**Create:**
- `core/build.gradle.kts`
- `core/settings.gradle.kts`
- `core/gradle/libs.versions.toml`
- `core/consumer-rules.pro`
- `core/proguard-rules.pro`
- `core/src/test/.../ResultTest.kt`
- `core/src/test/.../ErrorFormatterTest.kt`
- `core/src/test/.../ErrorMapperTest.kt`

**Move (7 files):**
- 4 error files
- 1 result file
- 2 util files

**Modify:**
- `settings.gradle` - add `:core`
- `build.gradle.kts` (root) - add library plugin
- `gradle/libs.versions.toml` - add android-library plugin
- `app/build.gradle.kts` - add `implementation(project(":core"))`

**Delete from app (after verification):**
- `app/src/main/java/uk/co/zlurgg/thedayto/core/domain/error/*`
- `app/src/main/java/uk/co/zlurgg/thedayto/core/domain/result/*`
- `app/src/main/java/uk/co/zlurgg/thedayto/core/domain/util/OrderType.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/core/domain/util/DateUtils.kt`

---

## Reusing in Another Project

The module has its own `settings.gradle.kts` and `libs.versions.toml`, making it fully self-contained.

### Option A: Composite Build (Recommended)
Uses the module's own version catalog:
```kotlin
// other-project/settings.gradle.kts
includeBuild("../path-to/core")

// other-project/app/build.gradle.kts
dependencies {
    implementation("io.github.zlurgg.core:core")
}
```

### Option B: Copy as Submodule
```kotlin
// Copy core/ folder, then in settings.gradle:
include ':core'

// app/build.gradle.kts
dependencies {
    implementation(project(":core"))
}
```
Note: With `include`, the parent project's version catalog is used instead.

### Option C: Build Standalone
```bash
cd core
./gradlew assembleRelease
# Produces core/build/outputs/aar/core-release.aar
```

Import from: `import io.github.zlurgg.core.domain.result.Result`

---

## SDK Learning Points

1. **Library vs Application** - Libraries use `com.android.library` plugin, can't have activities/application class
2. **Consumer ProGuard** - Rules in `consumer-rules.pro` automatically apply to consuming apps
3. **Namespace** - Required for library modules, generates unique R class
4. **No BuildConfig** - Libraries don't get app's BuildConfig; pass config via constructors/DI
5. **Test isolation** - `:core:test` runs only core's tests, faster feedback loop
