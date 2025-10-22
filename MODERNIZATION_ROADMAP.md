# The Day To - Modernization Roadmap

**Target:** Bring this app up to portfolio quality matching [My-Bookshelf](https://github.com/Zlurgg/My-Bookshelf)

**Standards:** Google's Modern Android Development (MAD) + SOLID/CLEAN/DRY/SRP principles

See [CLAUDE.md](./CLAUDE.md) for detailed coding standards and architectural guidelines.

---

## Progress Overview

**Current Phase:** Not Started
**Overall Progress:** 0/10 tasks completed
**Last Updated:** 2025-10-22

---

## Phase 1: Foundation & Quick Wins (High Priority)

**Goal:** Set up infrastructure and clean up dependencies
**Estimated Time:** 1-1.5 hours

### Task 1.1: Add Timber Logging ⚡
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 15 minutes
**Assigned To:** -

**Subtasks:**
- [ ] Add Timber dependency to `app/build.gradle.kts`
  ```kotlin
  implementation("com.jakewharton.timber:timber:5.0.1")
  ```
- [ ] Initialize Timber in `TheDayToApplication.onCreate()`
  ```kotlin
  if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
  }
  ```
- [ ] Search codebase for existing `Log.d`, `Log.e`, `println()` calls
- [ ] Replace with appropriate Timber calls (`Timber.d()`, `Timber.e()`)
- [ ] Test logging works in debug builds

**Notes:**
- Remember: Never use `println()` or `Log.*` directly going forward
- Use Timber.d() for debug, Timber.e() for errors, Timber.w() for warnings

---

### Task 1.2: Remove Hilt Dependencies ⚡
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 10 minutes
**Assigned To:** -

**Subtasks:**
- [ ] Open `app/build.gradle.kts`
- [ ] Remove Hilt dependencies:
  - [ ] `com.google.dagger:hilt-android`
  - [ ] `com.google.dagger:hilt-android-compiler`
  - [ ] Any hilt-related plugins
- [ ] Search codebase for Hilt annotations/imports:
  - [ ] `@HiltAndroidApp`
  - [ ] `@AndroidEntryPoint`
  - [ ] `import dagger.hilt.*`
- [ ] Remove any found Hilt references
- [ ] Sync Gradle
- [ ] Verify project still builds

**Notes:**
- We're standardizing on Koin only
- Double-check build.gradle.kts (project level) for Hilt plugin

---

### Task 1.3: Standardize Dependency Injection
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 30-45 minutes
**Assigned To:** -

**Subtasks:**
- [ ] **GoogleAuthUiClient to Koin**
  - [ ] Add GoogleAuthUiClient to Koin module in `di/AppModule.kt`
  - [ ] Inject into MainActivity using `by inject()`
  - [ ] Remove manual instantiation: `by lazy { GoogleAuthUiClient(this) }`
  - [ ] Test sign-in still works

- [ ] **PreferencesRepository to Koin**
  - [ ] Create interface `PreferencesRepository` (if not exists)
  - [ ] Create `PreferencesRepositoryImpl` wrapping SharedPreferences
  - [ ] Add to Koin module
  - [ ] Inject into AddEditEntryViewModel (remove Context parameter)
  - [ ] Inject into any other places using `TheDayToPrefRepository`
  - [ ] Remove direct SharedPreferences access

- [ ] **Verify all DI is consistent**
  - [ ] No manual instantiation anywhere
  - [ ] All ViewModels use constructor injection
  - [ ] All repositories injected via Koin
  - [ ] Run app and test all features

**Files to Modify:**
- `app/src/main/java/uk/co/zlurgg/thedayto/core/di/AppModule.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/MainActivity.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_daily_entry/presentation/AddEditEntryViewModel.kt`
- Create: `app/src/main/java/uk/co/zlurgg/thedayto/core/data/PreferencesRepository.kt`

**Notes:**
- This is foundational - everything after depends on clean DI
- Test thoroughly after changes

---

## Phase 2: Architecture Improvements (High Priority)

**Goal:** Modernize ViewModels to Google's recommended patterns
**Estimated Time:** 2-3 hours

### Task 2.1: Create UiState Data Classes
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 30 minutes
**Assigned To:** -

**Subtasks:**
- [ ] Create `feature_daily_entry/presentation/state/AddEditEntryUiState.kt`
  ```kotlin
  data class AddEditEntryUiState(
      val entryDate: Long = System.currentTimeMillis(),
      val entryMood: String = "",
      val entryContent: String = "",
      val entryColor: String = "",
      val currentEntryId: Int? = null,
      val isLoading: Boolean = false,
      val error: String? = null
  )
  ```

- [ ] Create `feature_daily_entry/presentation/state/EntriesUiState.kt`
  ```kotlin
  data class EntriesUiState(
      val entries: List<DailyEntry> = emptyList(),
      val selectedMonth: Int = Calendar.getInstance().get(Calendar.MONTH),
      val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
      val isLoading: Boolean = false,
      val error: String? = null,
      val deletedEntry: DailyEntry? = null // For undo
  )
  ```

- [ ] Create `feature_mood_color/presentation/state/AddEditMoodColorUiState.kt`

- [ ] Create `feature_sign_in/presentation/state/SignInUiState.kt`

**Notes:**
- All properties should be `val` (immutable)
- Include loading and error states
- Use meaningful defaults

---

### Task 2.2: Refactor AddEditEntryViewModel
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 45 minutes
**Assigned To:** -

**Subtasks:**
- [ ] Replace 6 separate `mutableStateOf` properties with:
  ```kotlin
  private val _uiState = MutableStateFlow(AddEditEntryUiState())
  val uiState: StateFlow<AddEditEntryUiState> = _uiState.asStateFlow()
  ```

- [ ] Update all state mutations to use `.update { it.copy(...) }`
  - [ ] `updateDate()` → `_uiState.update { it.copy(entryDate = ...) }`
  - [ ] `updateMood()` → `_uiState.update { it.copy(entryMood = ...) }`
  - [ ] `updateContent()` → `_uiState.update { it.copy(entryContent = ...) }`
  - [ ] `updateColor()` → `_uiState.update { it.copy(entryColor = ...) }`

- [ ] Inject PreferencesRepository instead of Context
  - [ ] Remove `context: Context` parameter
  - [ ] Add `private val preferencesRepository: PreferencesRepository`
  - [ ] Update all pref access to use repository

- [ ] Update event flow to use sealed interface
  ```kotlin
  sealed interface UiEvent {
      data object SaveEntry : UiEvent
      data object NavigateBack : UiEvent
      data class ShowSnackbar(val message: String) : UiEvent
  }
  ```

- [ ] Update `AddEditEntryScreen.kt` to collect state:
  ```kotlin
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()
  ```

- [ ] Test entry creation and editing still works

**Files to Modify:**
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_daily_entry/presentation/AddEditEntryViewModel.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_daily_entry/presentation/AddEditEntryScreen.kt`

**Notes:**
- This is the biggest ViewModel to refactor
- Take time to test thoroughly
- Use `.update {}` for all state changes (concurrency safe)

---

### Task 2.3: Refactor EntriesViewModel
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 30 minutes
**Assigned To:** -

**Subtasks:**
- [ ] Replace separate state properties with single StateFlow
- [ ] Update screen to use `collectAsStateWithLifecycle()`
- [ ] Test entries display and filtering works
- [ ] Test deletion and undo functionality

**Files to Modify:**
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_daily_entry/presentation/EntriesViewModel.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_daily_entry/presentation/EntriesScreen.kt`

---

### Task 2.4: Refactor AddEditMoodColorViewModel
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 20 minutes
**Assigned To:** -

**Subtasks:**
- [ ] Replace state properties with single StateFlow
- [ ] Update screen to collect state
- [ ] Test mood color creation/editing

**Files to Modify:**
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_mood_color/presentation/AddEditMoodColorViewModel.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_mood_color/presentation/AddEditMoodColorScreen.kt`

---

### Task 2.5: Refactor SignInViewModel
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 15 minutes
**Assigned To:** -

**Subtasks:**
- [ ] Consolidate state into single StateFlow
- [ ] Update sign-in screen
- [ ] Test Google sign-in flow

**Files to Modify:**
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_sign_in/presentation/SignInViewModel.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_sign_in/presentation/SignInScreen.kt`

---

### Task 2.6: Update Google Sign-In (Optional - Can defer)
**Status:** [ ] Not Started
**Priority:** Medium (Can defer to later)
**Estimated Time:** 1-2 hours
**Assigned To:** -

**Subtasks:**
- [ ] Research Google Identity Services migration
- [ ] Update Firebase Auth to latest
- [ ] Consider Credential Manager API
- [ ] Update GoogleAuthUiClient implementation
- [ ] Test sign-in flow thoroughly

**Notes:**
- This can be deferred if current implementation works
- Mark as technical debt for post-release
- Current deprecated API still functional for now

---

## Phase 3: Quality & Polish (Medium Priority)

**Goal:** Add error handling, clean up code, add tests
**Estimated Time:** 3-4 hours

### Task 3.1: Implement Error Handling
**Status:** [ ] Not Started
**Priority:** Medium
**Estimated Time:** 1-2 hours
**Assigned To:** -

**Subtasks:**
- [ ] Create `core/util/Resource.kt` sealed class:
  ```kotlin
  sealed class Resource<T> {
      data class Success<T>(val data: T) : Resource<T>()
      data class Error<T>(val message: String, val data: T? = null) : Resource<T>()
      data class Loading<T>(val data: T? = null) : Resource<T>()
  }
  ```

- [ ] Update repositories to wrap results:
  - [ ] DailyEntryRepositoryImpl
  - [ ] MoodColorRepositoryImpl
  - [ ] Catch database exceptions
  - [ ] Return Resource<T> instead of raw data

- [ ] Update ViewModels to handle Resource states:
  - [ ] Set `isLoading = true` on Loading
  - [ ] Set `error = message` on Error
  - [ ] Update data on Success

- [ ] Update UI to display error states:
  - [ ] Show error Snackbar/Toast
  - [ ] Display error message in UI
  - [ ] Add retry buttons where appropriate

**Files to Create:**
- `app/src/main/java/uk/co/zlurgg/thedayto/core/util/Resource.kt`

**Files to Modify:**
- All Repository implementations
- All ViewModels
- All Screens (add error UI)

**Notes:**
- This makes the app much more robust
- Handle network errors (for future API calls)
- Handle database errors gracefully

---

### Task 3.2: Code Cleanup
**Status:** [ ] Not Started
**Priority:** Medium
**Estimated Time:** 1 hour
**Assigned To:** -

**Subtasks:**
- [ ] **Remove commented code:**
  - [ ] Search for `//` blocks of commented code
  - [ ] Remove from MoodColor.kt (companion object)
  - [ ] Remove from EntriesViewModel.kt (year selection)
  - [ ] Remove from any other files

- [ ] **Extract magic numbers/strings:**
  - [ ] Create `core/util/Constants.kt`
  - [ ] Move date format strings
  - [ ] Move navigation route strings
  - [ ] Move database name/version
  - [ ] Move any other constants

- [ ] **Improve naming consistency:**
  - [ ] Review all variable/function names
  - [ ] Ensure camelCase for functions/variables
  - [ ] Ensure PascalCase for classes/composables
  - [ ] Fix any inconsistencies

- [ ] **Add KDoc comments:**
  - [ ] Add to all public functions
  - [ ] Add to all data classes
  - [ ] Add to complex business logic

**Files to Create:**
- `app/src/main/java/uk/co/zlurgg/thedayto/core/util/Constants.kt`

**Notes:**
- Use Android Studio's "Optimize Imports" feature
- Run code formatter on all files
- Follow kotlin.style guide

---

### Task 3.3: Add Unit Tests
**Status:** [ ] Not Started
**Priority:** Medium
**Estimated Time:** 2 hours
**Assigned To:** -

**Subtasks:**
- [ ] **Setup test dependencies:**
  - [ ] Add MockK to build.gradle.kts
  - [ ] Add Coroutines test dependency
  - [ ] Add Turbine for Flow testing

- [ ] **Test AddEditEntryViewModel:**
  - [ ] Test initial state
  - [ ] Test state updates (mood, content, color, date)
  - [ ] Test save entry success
  - [ ] Test save entry error
  - [ ] Test validation

- [ ] **Test EntriesViewModel:**
  - [ ] Test entries loading
  - [ ] Test entry deletion
  - [ ] Test undo deletion
  - [ ] Test filtering by month/year

- [ ] **Test Use Cases:**
  - [ ] Test GetDailyEntriesUseCase
  - [ ] Test AddDailyEntryUseCase
  - [ ] Test UpdateDailyEntryUseCase
  - [ ] Test DeleteDailyEntryUseCase

- [ ] **Test Repositories (with fakes):**
  - [ ] Create FakeDailyEntryDao
  - [ ] Test DailyEntryRepositoryImpl
  - [ ] Test error scenarios

**Target Coverage:**
- ViewModels: 80%+
- Use Cases: 90%+
- Repositories: 70%+

**Files to Create:**
- `app/src/test/java/uk/co/zlurgg/thedayto/feature_daily_entry/presentation/AddEditEntryViewModelTest.kt`
- `app/src/test/java/uk/co/zlurgg/thedayto/feature_daily_entry/presentation/EntriesViewModelTest.kt`
- `app/src/test/java/uk/co/zlurgg/thedayto/feature_daily_entry/domain/use_case/*Test.kt`
- `app/src/test/java/uk/co/zlurgg/thedayto/fake/FakeDailyEntryDao.kt`

**Notes:**
- Testing makes refactoring safer
- Tests serve as documentation
- Will help catch regressions

---

## Phase 4: Release Preparation (Low Priority)

**Goal:** Polish for public GitHub release
**Estimated Time:** 2-3 hours

### Task 4.1: Update README
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 1 hour
**Assigned To:** -

**Reference:** [My-Bookshelf README](https://github.com/Zlurgg/My-Bookshelf/blob/main/README.md)

**Subtasks:**
- [ ] Add app description and purpose
- [ ] Add features list
- [ ] Add screenshots (light and dark mode)
- [ ] Add architecture section with diagram
- [ ] Add tech stack section
- [ ] Add setup/installation instructions
- [ ] Add license section
- [ ] Add contact/contributing section
- [ ] Make it visually appealing with badges

**Files to Modify:**
- `README.md`

**Notes:**
- Screenshots are crucial for portfolio
- Show calendar view, mood entry, color picker
- Include architecture diagram (can use mermaid)

---

### Task 4.2: Add LICENSE
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 5 minutes
**Assigned To:** -

**Subtasks:**
- [ ] Choose license (MIT recommended like My-Bookshelf)
- [ ] Create LICENSE file
- [ ] Add copyright year and name
- [ ] Reference in README

**Files to Create:**
- `LICENSE`

---

### Task 4.3: Privacy & Documentation
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 30 minutes
**Assigned To:** -

**Subtasks:**
- [ ] Add privacy policy statement (if collecting data)
- [ ] Document data storage (local only)
- [ ] Document Google Sign-In data usage
- [ ] Add to README or separate PRIVACY.md
- [ ] Review for any hardcoded secrets/API keys
- [ ] Ensure .gitignore is comprehensive

**Notes:**
- Important for Play Store if planning to publish
- Good practice for portfolio piece

---

### Task 4.4: Notification Improvements
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 1 hour
**Assigned To:** -

**Subtasks:**
- [ ] Remove network constraint from NotificationWorker
- [ ] Add user settings for notification time
- [ ] Improve notification content/text
- [ ] Add notification channel importance
- [ ] Test notifications work reliably

**Files to Modify:**
- `app/src/main/java/uk/co/zlurgg/thedayto/core/notification/NotificationWorker.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/core/notification/Notifications.kt`

---

### Task 4.5: Final Polish
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 1 hour
**Assigned To:** -

**Subtasks:**
- [ ] Run code formatter on all files
- [ ] Fix any IDE warnings
- [ ] Optimize imports
- [ ] Run lint and fix issues
- [ ] Test app on multiple devices/screen sizes
- [ ] Test light and dark themes
- [ ] Verify all features work end-to-end
- [ ] Update version code and version name
- [ ] Create release notes

**Final Checklist:**
- [ ] All tests passing
- [ ] No commented code
- [ ] Comprehensive README with screenshots
- [ ] LICENSE file added
- [ ] .gitignore properly configured
- [ ] No hardcoded secrets/API keys
- [ ] Privacy policy statement
- [ ] Architecture documentation
- [ ] Code coverage >70%
- [ ] App icons and branding complete
- [ ] Version code/name updated
- [ ] Release notes prepared

---

## Session Notes

### Session 1 (2025-10-22)
- Created CLAUDE.md with comprehensive coding standards
- Created this MODERNIZATION_ROADMAP.md
- Ready to begin Phase 1

**Next Session Plan:** Start with Task 1.1 (Add Timber Logging)

---

### Session 2 (Date: ___)
**Tasks Completed:**
-

**Issues Encountered:**
-

**Next Session Plan:**
-

---

### Session 3 (Date: ___)
**Tasks Completed:**
-

**Issues Encountered:**
-

**Next Session Plan:**
-

---

## Notes & Decisions

### Technical Decisions
- **DI Framework:** Koin (remove Hilt completely)
- **State Management:** StateFlow (not LiveData)
- **UI Framework:** Jetpack Compose with Material 3
- **Database:** Room (no migrations needed pre-release)
- **Logging:** Timber
- **Testing:** JUnit 4 + MockK
- **License:** MIT (to be confirmed)

### Deferred Items
- Google Sign-In update (current implementation works, can defer)
- Advanced notification features (can be post-release)

### Questions/Blockers
- None currently

---

## References

- [CLAUDE.md](./CLAUDE.md) - Coding standards and architectural guidelines
- [My-Bookshelf](https://github.com/Zlurgg/My-Bookshelf) - Target quality standard
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Modern Android Development](https://developer.android.com/modern-android-development)

---

**Last Updated:** 2025-10-22
**Current Status:** Planning phase complete, ready to begin implementation