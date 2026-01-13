# Template Alignment Plan

This document tracks the alignment of The-Day-To with the shared project templates.

**Branch**: `feature/template-alignment`
**Created**: 2026-01-12
**Last Updated**: 2026-01-13

---

## Progress Summary

| Phase | Status | Notes |
|-------|--------|-------|
| Phase 1: Build Tooling | **COMPLETE** | Gradle, EditorConfig, Detekt, pre-commit hooks |
| Phase 2: Compose Optimizations | **COMPLETE** | @Stable and @Immutable annotations |
| Phase 3: Accessibility | **NOT STARTED** | |
| Phase 4: Error Handling | **COMPLETE** | Result types fully migrated |
| Phase 5: State-Based Navigation | **SKIPPED** | Current SharedFlow pattern works well |
| Phase 6: Documentation | **PARTIAL** | CLAUDE.md needs update |

---

## Phase 1: Build Tooling & Static Analysis - COMPLETE

### 1.1 Gradle Build Optimizations - COMPLETE
- [x] Enable parallel builds in `gradle.properties`
- [x] Enable build caching
- [x] Enable configuration cache

**Commit**: `ad7cfab` - `perf(compose): Add stability annotations to state and domain classes`
(Note: Gradle optimizations were already in place)

---

### 1.2 EditorConfig Setup - COMPLETE
- [x] Create `.editorconfig` in project root
- [x] Configure Kotlin formatting rules
- [x] Configure ktlint rule overrides

**File**: `.editorconfig` created

---

### 1.3 Detekt Setup - COMPLETE
- [x] Add Detekt version to `libs.versions.toml`
- [x] Add Detekt plugin to `libs.versions.toml`
- [x] Add detekt-formatting library to `libs.versions.toml`
- [x] Apply Detekt plugin in root `build.gradle.kts`
- [x] Configure Detekt in `app/build.gradle.kts`
- [x] Create `app/detekt.yml` configuration
- [x] Run `./gradlew detekt` - passes with warnings only

---

### 1.4 Pre-commit Hooks - COMPLETE
- [x] Create `scripts/` directory
- [x] Create `scripts/pre-commit` hook script
- [x] Create `scripts/install-hooks.sh` installation script

---

## Phase 2: Compose Optimizations - COMPLETE

### 2.1 Stability Annotations for State Classes - COMPLETE
- [x] `journal/ui/overview/state/OverviewUiState.kt`
- [x] `journal/ui/editor/state/EditorUiState.kt`
- [x] `journal/ui/stats/state/StatsUiState.kt`
- [x] `journal/ui/moodcolormanagement/state/MoodColorManagementUiState.kt`
- [x] `auth/ui/state/SignInState.kt`

**Commit**: `ad7cfab` - `perf(compose): Add stability annotations to state and domain classes`

---

### 2.2 Immutable Annotations for Domain Models - COMPLETE
- [x] `journal/domain/model/Entry.kt`
- [x] `journal/domain/model/MoodColor.kt`
- [x] `journal/domain/model/EntryWithMoodColor.kt`
- [x] `update/domain/model/UpdateInfo.kt`

**Commit**: `ad7cfab` - `perf(compose): Add stability annotations to state and domain classes`

---

## Phase 3: Accessibility Improvements - NOT STARTED

### 3.1 Content Description Audit
**Effort**: 1-2 hours

- [ ] Audit `journal/ui/overview/` screens and components
- [ ] Audit `journal/ui/editor/` screens and components
- [ ] Audit `journal/ui/stats/` screens and components
- [ ] Audit `journal/ui/moodcolormanagement/` screens and components
- [ ] Audit `auth/ui/` screens and components
- [ ] Add missing `contentDescription` to all `IconButton` components
- [ ] Set `contentDescription = null` for decorative images
- [ ] Add string resources for accessibility descriptions

---

### 3.2 Touch Target Verification
**Effort**: 30 minutes

- [ ] Verify all touch targets are minimum 48dp
- [ ] Fix any undersized touch targets using `Modifier.size(48.dp)` or padding

---

## Phase 4: Error Handling Refinement - COMPLETE

### 4.1 Result Type Implementation - COMPLETE
- [x] Create `core/domain/result/Result.kt` sealed interface
- [x] Create `core/domain/error/DataError.kt` sealed interface
- [x] Create `core/domain/error/ErrorMapper.kt` utility (safeSuspendCall)
- [x] Create `core/domain/error/ErrorFormatter.kt` for user-friendly messages
- [x] Add extension functions: `getOrNull()`, `onSuccess()`, `onError()`, `fold()`

**Files Created**:
- `core/domain/result/Result.kt`
- `core/domain/error/DataError.kt`
- `core/domain/error/ErrorMapper.kt`
- `core/domain/error/ErrorFormatter.kt`

**Commits**:
- `2945c76` - `feat(core): Add Result type and DataError for typed error handling`
- `6f51f50` - `feat(core): Add fold extension to Result type`

---

### 4.2 Repository Error Handling Refactor - COMPLETE
- [x] `journal/data/repository/EntryRepositoryImpl.kt`
- [x] `journal/data/repository/MoodColorRepositoryImpl.kt`
- [x] `auth/data/repository/AuthRepositoryImpl.kt`
- [x] `auth/data/service/GoogleAuthUiClient.kt`
- [x] `update/data/repository/UpdateRepositoryImpl.kt`
- [x] Update corresponding repository interfaces in domain layer

**Commit**: `4e6e9ce` - `refactor(core): Migrate to typed Result pattern across all layers`

---

### 4.3 UseCase Error Handling Updates - COMPLETE
- [x] `journal/domain/usecases/editor/AddEntryUseCase.kt`
- [x] `journal/domain/usecases/overview/UpdateEntryUseCase.kt`
- [x] `journal/domain/usecases/overview/DeleteEntryUseCase.kt`
- [x] `journal/domain/usecases/shared/entry/GetEntryUseCase.kt`
- [x] `journal/domain/usecases/shared/entry/GetEntryByDateUseCase.kt`
- [x] `journal/domain/usecases/shared/moodcolor/GetMoodColorUseCase.kt`
- [x] `journal/domain/usecases/shared/moodcolor/AddMoodColorUseCase.kt`
- [x] `journal/domain/usecases/shared/moodcolor/UpdateMoodColorUseCase.kt`
- [x] `journal/domain/usecases/shared/moodcolor/UpdateMoodColorNameUseCase.kt`
- [x] `journal/domain/usecases/overview/CheckTodayEntryExistsUseCaseImpl.kt`
- [x] `auth/domain/usecases/SignInUseCase.kt`
- [x] `auth/domain/usecases/CheckTodayEntryUseCase.kt`
- [x] `update/domain/usecases/CheckForUpdateUseCase.kt`
- [x] `update/domain/usecases/GetCurrentVersionInfoUseCase.kt`

**Commits**:
- `4e6e9ce` - `refactor(core): Migrate to typed Result pattern across all layers`
- `f8c09d2` - `fix: Complete Result type migration and fix tests`

---

### 4.4 ViewModel Error Handling Updates - COMPLETE
- [x] `journal/ui/editor/EditorViewModel.kt`
- [x] `journal/ui/overview/OverviewViewModel.kt`
- [x] `auth/ui/SignInViewModel.kt`

**Commits**:
- `4e6e9ce` - `refactor(core): Migrate to typed Result pattern across all layers`
- `f8c09d2` - `fix: Complete Result type migration and fix tests`

---

### 4.5 Test Updates - COMPLETE

**Unit Tests (246 passing):**
- [x] Update fake repositories to return Result types
- [x] Fix `SignInViewModelTest.kt`
- [x] Fix `UpdateMoodColorNameUseCaseTest.kt`
- [x] Fix `UpdateMoodColorUseCaseTest.kt`
- [x] Fix `EditorViewModelTest.kt`
- [x] Fix `MoodColorManagementViewModelTest.kt`
- [x] Fix `OverviewViewModelTest.kt`

**Instrumented Tests (49 passing):**
- [x] Fix `EntryRepositoryTest.kt` - Add `.getOrNull()` to repository calls
- [x] Fix `MoodColorRepositoryTest.kt` - Add `.getOrNull()` to repository calls
- [x] Fix `UpdateRepositoryImplTest.kt` - Update for custom Result type
- [x] Fix `UpdateDialogTest.kt` - Fix assertions to match actual UI text

**Total: 295 tests passing (246 unit + 49 instrumented)**

**Commits**:
- `f8c09d2` - `fix: Complete Result type migration and fix tests`
- `840acaf` - `fix: Update instrumented tests for Result type migration`

---

## Phase 5: State-Based Navigation - SKIPPED

Decided to skip this phase. The current `SharedFlow<UiEvent>` pattern works well and is a common pattern in production apps. The state-based navigation pattern has benefits but would require significant refactoring with minimal practical benefit for this project.

---

## Phase 6: Documentation Updates - PARTIAL

### 6.1 Update CLAUDE.md
- [ ] Add Detekt commands to Development Commands section
- [ ] Document pre-commit hook installation
- [ ] Update patterns section with Result type usage
- [ ] Update test count (now 246+ tests)

---

## Verification Checklist

After completing all phases:

- [x] `./gradlew assembleDebug` - Builds successfully
- [x] `./gradlew test` - All 246 unit tests pass
- [x] `./gradlew connectedAndroidTest` - All 49 instrumented tests pass
- [x] `./gradlew detekt` - Passes with warnings only
- [ ] Manual test on device - App functions correctly
- [ ] TalkBack test - Accessibility works correctly

---

## Next Steps

1. **Manual testing** on device to verify app functionality
2. **Phase 3: Accessibility** - Content descriptions and touch targets
3. **Phase 6: Documentation** - Update CLAUDE.md with Result type patterns and test counts

---

## Commits Made

| Commit | Message |
|--------|---------|
| `ad7cfab` | `perf(compose): Add stability annotations to state and domain classes` |
| `2945c76` | `feat(core): Add Result type and DataError for typed error handling` |
| `6f51f50` | `feat(core): Add fold extension to Result type` |
| `4e6e9ce` | `refactor(core): Migrate to typed Result pattern across all layers` |
| `f8c09d2` | `fix: Complete Result type migration and fix tests` |
| `3c50a94` | `docs: Update template alignment plan with completion status` |
| `840acaf` | `fix: Update instrumented tests for Result type migration` |

---

## Result Type Pattern Reference

### Usage in Repositories
```kotlin
override suspend fun getEntryById(id: Int): Result<Entry?, DataError.Local> {
    return ErrorMapper.safeSuspendCall(TAG) {
        dao.getEntryById(id)?.toDomain()
    }
}
```

### Usage in UseCases
```kotlin
// Pass through Result
suspend operator fun invoke(id: Int): Result<Entry?, DataError.Local> {
    return repository.getEntryById(id)
}

// Unwrap for internal logic
val entry = repository.getEntryById(id).getOrNull()
```

### Usage in ViewModels
```kotlin
when (val result = useCases.signIn()) {
    is Result.Success -> {
        _state.update { it.copy(isSignInSuccessful = true) }
    }
    is Result.Error -> {
        val message = ErrorFormatter.format(result.error, "sign in")
        _uiEvents.emit(UiEvent.ShowSnackbar(message))
    }
}
```

### DataError Types
- `DataError.Local`: DATABASE_ERROR, NOT_FOUND, DUPLICATE_ENTRY, UNKNOWN
- `DataError.Remote`: REQUEST_TIMEOUT, NO_INTERNET, SERVER_ERROR, NOT_FOUND, UNKNOWN
- `DataError.Validation`: EMPTY_MOOD, EMPTY_COLOR, CONTENT_TOO_LONG, etc.
- `DataError.Auth`: CANCELLED, NO_CREDENTIAL, FAILED, NETWORK_ERROR
