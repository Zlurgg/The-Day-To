# The Day To - Modernization Roadmap

**Target:** Bring this app up to portfolio quality matching [My-Bookshelf](https://github.com/Zlurgg/My-Bookshelf)

**Standards:** Google's Modern Android Development (MAD) + SOLID/CLEAN/DRY/SRP principles

See [CLAUDE.md](./CLAUDE.md) for detailed coding standards and architectural guidelines.

---

## Progress Overview

**Current Phase:** Phase 1 Complete ✅ → Moving to Phase 2 (Architecture Overhaul)
**Overall Progress:** Phase 1: 3/3 ✅ | Phase 2: 0/9 | Overall: ~10% complete
**Last Updated:** 2025-10-23

---

## Phase 1: Foundation & Quick Wins (High Priority) ✅ COMPLETE

**Goal:** Set up infrastructure and clean up dependencies
**Estimated Time:** 1-1.5 hours
**Actual Time:** ~2 hours (including Google Sign-In modernization)

### Task 1.1: Add Timber Logging ⚡ ✅
**Status:** [✅] Complete
**Priority:** High
**Completed:** 2025-10-22

**Subtasks:**
- [✅] Add Timber dependency to `app/build.gradle.kts`
- [✅] Initialize Timber in `TheDayToApplication.onCreate()`
- [✅] Replace existing logging with Timber calls
- [✅] Enable BuildConfig generation for DEBUG check
- [✅] Test logging works in debug builds

**Notes:**
- BuildConfig generation was disabled and needed to be enabled in build.gradle.kts
- KSP version compatibility issue resolved (updated to 2.2.20-2.0.4)

---

### Task 1.2: Remove Hilt Dependencies ⚡ ✅
**Status:** [✅] Complete
**Priority:** High
**Completed:** 2025-10-22

**Subtasks:**
- [✅] Remove Hilt dependencies from build.gradle.kts
- [✅] Remove Hilt plugins
- [✅] Verify no Hilt annotations remain in codebase
- [✅] Sync Gradle successfully
- [✅] Verify project builds

**Notes:**
- Hilt was already partially removed, finalized cleanup

---

### Task 1.3: Standardize Dependency Injection ✅
**Status:** [✅] Complete
**Priority:** High
**Completed:** 2025-10-22

**Subtasks:**
- [✅] **GoogleAuthUiClient to Koin**
  - [✅] Add GoogleAuthUiClient to Koin module in `di/AppModule.kt`
  - [✅] Inject into MainActivity using `by inject()`
  - [✅] Remove manual instantiation
  - [✅] Modernized to use Credential Manager API (bonus!)

- [✅] **PreferencesRepository to Koin**
  - [✅] Create interface `PreferencesRepository`
  - [✅] Create `PreferencesRepositoryImpl` wrapping SharedPreferences
  - [✅] Add backward-compatible typealias for TheDayToPrefRepository
  - [✅] Add to Koin module
  - [✅] Inject into AddEditEntryViewModel
  - [✅] Update ViewModelModules

- [✅] **Verify all DI is consistent**
  - [✅] No manual instantiation anywhere
  - [✅] All ViewModels use constructor injection
  - [✅] All repositories injected via Koin
  - [✅] App runs and all features work

**Files Modified:**
- `app/src/main/java/uk/co/zlurgg/thedayto/di/AppModule.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/MainActivity.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/di/ViewModelModules.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/feature_daily_entry/presentation/AddEditEntryViewModel.kt`
- Created: `app/src/main/java/uk/co/zlurgg/thedayto/core/domain/repository/PreferencesRepository.kt`
- Modified: `app/src/main/java/uk/co/zlurgg/thedayto/core/domain/repository/TheDayToPrefRepository.kt` (renamed to impl)

**Bonus Work Completed:**
- [✅] Modernized Google Sign-In from deprecated BeginSignInRequest to Credential Manager API
- [✅] Updated TheDayToApp.kt to use new direct signIn() flow
- [✅] Reorganized feature_sign_in structure (domain/model, domain/service)
- [✅] Updated Gradle wrapper to 8.14.3
- [✅] Fixed KSP compatibility (2.2.20-2.0.4)
- [✅] Created CLAUDE.md and MODERNIZATION_ROADMAP.md documentation

**Notes:**
- Google Sign-In modernization was not originally planned for Phase 1 but completed due to deprecation
- Project now builds successfully and all features functional
- Foundation is solid for Phase 2 work

---

## Phase 2: Architecture Overhaul (CRITICAL Priority) 🏗️

**Goal:** Fix fundamental architecture issues, modernize navigation, and create all screens before ViewModel refactoring
**Estimated Time:** 8-11 hours (includes type-safe navigation migration)
**Status:** [ ] Not Started

**Deliverables:**
- MoodColorPickerDialog component (reusable)
- MoodColorManagerScreen (complete with Root/Presenter pattern)
- Type-safe navigation with Kotlin Serialization (modern MAD standard)
- Standardized naming across all features (`*UiState`, `*Action`, `*UiEvent`)
- Clean architecture with proper boundaries
- All 4 screens ready for Phase 3 ViewModel work

### 🚨 Critical Architectural Issues Identified

During ViewModel audit (Session 4, 2025-10-23), we discovered that **jumping into ViewModel refactoring would be premature**. The following fundamental architecture issues must be addressed first:

1. **MoodColorScreen should be a Dialog** - Currently a full screen, should be a modal dialog
2. **Dual ViewModel Anti-Pattern** - AddEditMoodColorScreen injects TWO ViewModels (tight coupling)
3. **Inconsistent Naming** - Mix of `*State`, `*Event` patterns across features
4. **Missing Future Requirements** - Need to plan for standalone mood color management screen
5. **Unclear Feature Boundaries** - MoodColor vs DailyEntry relationship unclear

### 🎯 Target Architecture

**Feature Structure (Best Practice):**
```
feature_mood_color/                    ← Separate bounded context
├── data/
│   ├── data_source/MoodColorDao.kt
│   └── repository/MoodColorRepositoryImpl.kt
├── domain/
│   ├── model/MoodColor.kt
│   ├── repository/MoodColorRepository.kt
│   └── use_case/MoodColorUseCases.kt
└── presentation/
    ├── components/
    │   ├── MoodColorPickerDialog.kt   ← NEW: Reusable dialog (not full screen)
    │   ├── ColorPicker.kt
    │   └── MoodTextField.kt
    └── mood_color_manager/            ← FUTURE: Standalone management screen
        ├── MoodColorManagerScreenRoot.kt
        ├── MoodColorManagerScreen.kt
        ├── MoodColorManagerViewModel.kt
        └── state/
            ├── MoodColorManagerUiState.kt
            └── MoodColorManagerAction.kt

feature_daily_entry/
├── data/...
├── domain/...
└── presentation/
    ├── add_edit_entry/
    │   ├── AddEditEntryScreenRoot.kt  ← Root/Presenter pattern
    │   ├── AddEditEntryScreen.kt      ← Pure presenter
    │   ├── AddEditEntryViewModel.kt   ← NO mood color logic
    │   └── state/
    │       ├── AddEditEntryUiState.kt
    │       ├── AddEditEntryAction.kt
    │       └── AddEditEntryUiEvent.kt
    └── entries/                       ← Renamed from display_daily_entries
        ├── EntriesScreenRoot.kt
        ├── EntriesScreen.kt
        ├── EntriesViewModel.kt
        └── state/
            ├── EntriesUiState.kt
            └── EntriesAction.kt

feature_sign_in/
└── presentation/
    ├── SignInScreenRoot.kt
    ├── SignInScreen.kt
    ├── SignInViewModel.kt
    └── state/
        ├── SignInUiState.kt           ← Renamed from SignInState
        └── (uses direct methods, no Action pattern)
```

**Key Architectural Principles:**
- ✅ MoodColor as separate bounded context (can be managed independently)
- ✅ Dialog component instead of full screen (reusable)
- ✅ No ViewModel coupling (callback pattern)
- ✅ Future-proof for standalone mood color management
- ✅ Root/Presenter pattern for all screens
- ✅ Consistent naming: `*UiState`, `*Action`, `*UiEvent`

---

### Task 2.1: Convert AddEditMoodColorScreen → Dialog Component
**Status:** [ ] Not Started
**Priority:** CRITICAL
**Estimated Time:** 1.5 hours

**Current Problem:**
- `AddEditMoodColorScreen` is a full screen composable
- Accessed when user clicks "add new mood color" in AddEditEntryScreen
- Should be a **dialog/modal** instead
- Currently injects `AddEditEntryViewModel` (tight coupling)

**Target:**
- Convert to `MoodColorPickerDialog` composable dialog
- Use callback pattern instead of ViewModel injection
- Can be reused in future MoodColorManagerScreen

**Subtasks:**
- [ ] Create `feature_mood_color/presentation/components/MoodColorPickerDialog.kt`:
  ```kotlin
  @Composable
  fun MoodColorPickerDialog(
      showDialog: Boolean,
      moodColors: List<MoodColor>,
      onDismiss: () -> Unit,
      onSaveMoodColor: (mood: String, color: String) -> Unit,
      onDeleteMoodColor: (MoodColor) -> Unit,
      modifier: Modifier = Modifier
  )
  ```
- [ ] Move ColorPicker, MoodCreator, MoodTextField to components/ directory
- [ ] Remove AddEditMoodColorScreen.kt (full screen implementation)
- [ ] Remove `isMoodColorSectionVisible` state from AddEditEntryViewModel
- [ ] Update AddEditEntryScreen to use MoodColorPickerDialog with local state
- [ ] Test mood color creation flow with dialog

**Files to Create:**
- `feature_mood_color/presentation/components/MoodColorPickerDialog.kt`

**Files to Delete:**
- `feature_mood_color/presentation/AddEditMoodColorScreen.kt`

**Files to Modify:**
- `feature_daily_entry/presentation/add_edit_daily_entry/AddEditEntryScreen.kt`
- `feature_daily_entry/presentation/add_edit_daily_entry/AddEditEntryViewModel.kt`
- Move component files to components/ directory

**Notes:**
- This solves the dual ViewModel anti-pattern
- Dialog uses callback pattern (clean separation)
- Future: Same dialog can be used in MoodColorManagerScreen

---

### Task 2.2: Standardize Naming - Event → Action Pattern
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 45 minutes

**Current State:**
- Mixed naming: `AddEditEntryEvent`, `EntriesEvent`, `AddEditMoodColorEvent`
- SignInViewModel uses direct methods (no event/action pattern)

**Target:**
- Rename all `*Event` → `*Action` (user interactions)
- Keep `*UiEvent` for one-time UI events (navigation, snackbars)
- Consistent with Google's Now in Android architecture

**Subtasks:**
- [ ] Rename `AddEditEntryEvent` → `AddEditEntryAction`
- [ ] Rename `EntriesEvent` → `EntriesAction`
- [ ] Rename `AddEditMoodColorEvent` → `AddEditMoodColorAction`
- [ ] Update all usages and imports
- [ ] Update `onEvent()` methods → `onAction()`

**Files to Rename:**
- `AddEditEntryEvent.kt` → `AddEditEntryAction.kt`
- `EntriesEvent.kt` → `EntriesAction.kt`
- `AddEditMoodColorEvent.kt` → `AddEditMoodColorAction.kt`

**Files to Modify:**
- All ViewModels (update method signatures)
- All Screens (update event dispatch calls)

**Pattern:**
```kotlin
// User interactions → Actions (imperative)
sealed interface AddEditEntryAction {
    data class EnterMood(val mood: String) : AddEditEntryAction
    data class EnterContent(val value: String) : AddEditEntryAction
    data object SaveEntry : AddEditEntryAction
}

// One-time UI events → UiEvent (these stay as "Event")
sealed interface AddEditEntryUiEvent {
    data class ShowSnackbar(val message: String) : AddEditEntryUiEvent
    data object NavigateBack : AddEditEntryUiEvent
}
```

---

### Task 2.3: Standardize Naming - State → UiState Pattern
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 30 minutes

**Current State:**
- Mixed naming: `EntriesState`, `SignInState`, `MoodColorState`
- Inconsistent with `UiState` convention

**Target:**
- All state classes named `*UiState`
- Move to dedicated `state/` packages

**Subtasks:**
- [ ] Rename `SignInState` → `SignInUiState`
- [ ] Rename `EntriesState` → `EntriesUiState`
- [ ] Rename `MoodColorState` → (will be merged into AddEditMoodColorUiState later)
- [ ] Update all usages and imports

**Files to Rename:**
- `feature_sign_in/presentation/SignInState.kt` → `state/SignInUiState.kt`
- `feature_daily_entry/presentation/display_daily_entries/EntriesState.kt` → `state/EntriesUiState.kt`

**Files to Modify:**
- SignInViewModel.kt
- EntriesViewModel.kt
- All screens using these states

---

### Task 2.4: Rename display_daily_entries → entries
**Status:** [ ] Not Started
**Priority:** Medium
**Estimated Time:** 30 minutes

**Current State:**
- Package: `presentation/display_daily_entries/`
- Verbose and inconsistent with other feature naming

**Target:**
- Package: `presentation/entries/`
- Cleaner, more concise naming

**Subtasks:**
- [ ] Rename directory `display_daily_entries/` → `entries/`
- [ ] Update all package declarations
- [ ] Update all imports across codebase
- [ ] Update navigation routes if needed

**Files to Move:**
- All files in `display_daily_entries/` → `entries/`

---

### Task 2.5: Create state/ Directory Structure
**Status:** [ ] Not Started
**Priority:** Medium
**Estimated Time:** 15 minutes

**Target Structure:**
```
presentation/
├── add_edit_entry/
│   ├── AddEditEntryScreen.kt
│   ├── AddEditEntryViewModel.kt
│   └── state/                     ← NEW
│       ├── AddEditEntryUiState.kt
│       ├── AddEditEntryAction.kt
│       └── AddEditEntryUiEvent.kt
├── entries/
│   └── state/                     ← NEW
│       ├── EntriesUiState.kt
│       └── EntriesAction.kt
└── mood_color_manager/ (future)
    └── state/
```

**Subtasks:**
- [ ] Create `state/` directories in each presentation package
- [ ] Move/create state files into state/ directories
- [ ] Update imports

---

### Task 2.6: Remove AddEditMoodColorViewModel
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 30 minutes

**Rationale:**
- After converting to dialog, AddEditMoodColorViewModel is no longer needed
- MoodColorPickerDialog uses callback pattern
- Simplifies architecture

**Subtasks:**
- [ ] Remove `AddEditMoodColorViewModel.kt`
- [ ] Remove from Koin DI modules
- [ ] Verify AddEditEntryViewModel doesn't depend on it
- [ ] Update any navigation that referenced it

**Files to Delete:**
- `feature_mood_color/presentation/AddEditMoodColorViewModel.kt`

**Files to Modify:**
- `di/ViewModelModules.kt`

---

### Task 2.7: Create MoodColorManagerScreen
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 2 hours

**Goal:** Create standalone mood color management screen

**Requirements:**
- Accessible from EntriesScreen (main page)
- Allows users to create/edit/delete mood colors without creating entry
- Reuses MoodColorPickerDialog for create/edit
- Lists all existing mood colors with edit/delete actions
- Follows Root/Presenter pattern from the start

**Target Structure:**
```
feature_mood_color/presentation/mood_color_manager/
├── MoodColorManagerScreenRoot.kt    ← State management + navigation
├── MoodColorManagerScreen.kt        ← Pure UI (private)
├── MoodColorManagerViewModel.kt     ← Single StateFlow
└── state/
    ├── MoodColorManagerUiState.kt   ← All state in one class
    ├── MoodColorManagerAction.kt    ← User interactions
    └── MoodColorManagerUiEvent.kt   ← One-time events
```

**Subtasks:**
- [ ] Create `state/MoodColorManagerUiState.kt`:
  ```kotlin
  data class MoodColorManagerUiState(
      val moodColors: List<MoodColor> = emptyList(),
      val showDialog: Boolean = false,
      val editingMoodColor: MoodColor? = null,
      val isLoading: Boolean = false,
      val error: String? = null
  )
  ```

- [ ] Create `state/MoodColorManagerAction.kt`:
  ```kotlin
  sealed interface MoodColorManagerAction {
      data object AddNewMoodColor : MoodColorManagerAction
      data class EditMoodColor(val moodColor: MoodColor) : MoodColorManagerAction
      data class DeleteMoodColor(val moodColor: MoodColor) : MoodColorManagerAction
      data object DismissDialog : MoodColorManagerAction
      data class SaveMoodColor(val mood: String, val color: String) : MoodColorManagerAction
  }
  ```

- [ ] Create `state/MoodColorManagerUiEvent.kt`:
  ```kotlin
  sealed interface MoodColorManagerUiEvent {
      data class ShowSnackbar(val message: String) : MoodColorManagerUiEvent
      data object NavigateBack : MoodColorManagerUiEvent
  }
  ```

- [ ] Create `MoodColorManagerViewModel.kt`:
  - Inject `MoodColorUseCases`
  - Single `MutableStateFlow<MoodColorManagerUiState>`
  - Observe mood colors from repository
  - Handle all actions (add, edit, delete)

- [ ] Create `MoodColorManagerScreen.kt` (private Presenter):
  - Display list of mood colors
  - Each item shows mood name and color preview
  - Edit/Delete buttons per item
  - FAB to add new mood color
  - Uses MoodColorPickerDialog when adding/editing

- [ ] Create `MoodColorManagerScreenRoot.kt`:
  - Inject ViewModel via Koin
  - Collect state with `collectAsStateWithLifecycle()`
  - Handle navigation events
  - Pass state + callbacks to Presenter

- [ ] Add navigation route to Screen.kt
- [ ] Add button in EntriesScreen to navigate to MoodColorManagerScreen
- [ ] Update navigation graph to include new screen
- [ ] Test full flow: navigate, add, edit, delete mood colors

**UI Design:**
```
┌─────────────────────────────┐
│ ← Mood Colors               │  ← TopBar with back button
├─────────────────────────────┤
│                             │
│ 😊 Happy      [🟢] [✏️][🗑️]│  ← Color preview, edit, delete
│ 😢 Sad        [🔵] [✏️][🗑️]│
│ 😠 Angry      [🔴] [✏️][🗑️]│
│ 😌 Calm       [🟣] [✏️][🗑️]│
│                             │
│            [+]              │  ← FAB to add new
└─────────────────────────────┘
```

**Files to Create:**
- `feature_mood_color/presentation/mood_color_manager/MoodColorManagerScreenRoot.kt`
- `feature_mood_color/presentation/mood_color_manager/MoodColorManagerScreen.kt`
- `feature_mood_color/presentation/mood_color_manager/MoodColorManagerViewModel.kt`
- `feature_mood_color/presentation/mood_color_manager/state/MoodColorManagerUiState.kt`
- `feature_mood_color/presentation/mood_color_manager/state/MoodColorManagerAction.kt`
- `feature_mood_color/presentation/mood_color_manager/state/MoodColorManagerUiEvent.kt`

**Files to Modify:**
- `core/presentation/Screen.kt` (add navigation route)
- `feature_daily_entry/presentation/entries/EntriesScreen.kt` (add navigation button)
- Navigation setup
- `di/ViewModelModules.kt` (add ViewModel to DI)

**Notes:**
- This gives us 4 screens total for Phase 3 ViewModel work
- All screens will follow same architectural patterns
- MoodColorPickerDialog is reused (created in Task 2.1)
- Sets the standard for Root/Presenter pattern

---

### Task 2.8: Update DI Modules for New Structure
**Status:** [ ] Not Started
**Priority:** Medium
**Estimated Time:** 30 minutes

**Subtasks:**
- [ ] Remove AddEditMoodColorViewModel from ViewModelModules
- [ ] Verify all remaining ViewModels properly injected
- [ ] Update any module comments/documentation
- [ ] Test DI graph builds correctly

**Files to Modify:**
- `di/ViewModelModules.kt`
- `di/AppModule.kt` (if needed)

---

### Task 2.9: Migrate to Type-Safe Navigation (Modern Pattern)
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 2-3 hours

**Current State:**
- Using string-based navigation with sealed class routes (older pattern)
- Manual argument parsing from route strings
- No compile-time safety for navigation

**Target:**
- Migrate to **type-safe navigation** with Kotlin Serialization
- Follows Google's Modern Android Development (MAD) standard (Navigation Compose 2.8+)
- Remove AddEditMoodColorScreen route (now a dialog)

**Subtasks:**

**Part A: Add Dependencies**
- [ ] Add Kotlin Serialization plugin to `build.gradle.kts` (project level):
  ```kotlin
  plugins {
      id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
  }
  ```
- [ ] Add kotlinx-serialization dependency to `build.gradle.kts` (app level):
  ```kotlin
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
  ```

**Part B: Create Type-Safe Routes**
- [ ] Create `core/presentation/navigation/NavigationRoutes.kt`:
  ```kotlin
  @Serializable
  object SignInRoute

  @Serializable
  object EntriesRoute

  @Serializable
  data class AddEditEntryRoute(
      val entryId: Int = -1,
      val showBackButton: Boolean = false
  )

  @Serializable
  object MoodColorManagerRoute  // For Task 2.7

  @Serializable
  object NotificationTestRoute
  ```

**Part C: Update Navigation Graph**
- [ ] Update `TheDayToApp.kt` to use type-safe composable routes
- [ ] Remove string-based route parsing
- [ ] Use `backStackEntry.toRoute<T>()` for argument retrieval
- [ ] Remove AddEditMoodColorScreen composable (now a dialog)

**Part D: Update Navigation Calls**
- [ ] Update all `navController.navigate(Screen.*.route)` calls to use route objects
- [ ] Update in EntriesScreen, AddEditEntryScreen, SignInScreen
- [ ] Update back navigation to use type-safe routes

**Part E: Clean Up**
- [ ] Delete `core/presentation/Screen.kt` (replaced by NavigationRoutes)
- [ ] Remove all `Screen.*` references
- [ ] Test all navigation flows work correctly
- [ ] Verify navigation graph is correct

**Files to Create:**
- `core/presentation/navigation/NavigationRoutes.kt`

**Files to Delete:**
- `core/presentation/Screen.kt`

**Files to Modify:**
- `build.gradle.kts` (project level - add serialization plugin)
- `build.gradle.kts` (app level - add kotlinx-serialization dependency)
- `core/presentation/TheDayToApp.kt` (update NavHost)
- `feature_daily_entry/presentation/entries/EntriesScreen.kt` (navigation calls)
- `feature_daily_entry/presentation/add_edit_daily_entry/AddEditEntryScreen.kt` (navigation calls)
- `feature_sign_in/presentation/SignInScreen.kt` (navigation calls)

**Example Migration:**
```kotlin
// ❌ OLD (String-Based)
sealed class Screen(val route: String) {
    data object EntriesScreen : Screen("entries_screen")
}

composable(route = Screen.AddEditEntryScreen.route + "?entryId={entryId}") { ... }
navController.navigate("${Screen.AddEditEntryScreen.route}?entryId=$id")

// ✅ NEW (Type-Safe)
@Serializable
object EntriesRoute

@Serializable
data class AddEditEntryRoute(val entryId: Int = -1, val showBackButton: Boolean = false)

composable<AddEditEntryRoute> { backStackEntry ->
    val args = backStackEntry.toRoute<AddEditEntryRoute>()
    AddEditEntryScreen(entryId = args.entryId, showBackButton = args.showBackButton)
}

navController.navigate(AddEditEntryRoute(entryId = id, showBackButton = true))
```

**Benefits:**
- Type-safe: No string manipulation, compile-time checks
- Refactor-friendly: Rename detection works
- Cleaner: No manual argument parsing
- Modern: Follows Google's current best practices (2024+)

**References:**
- [Google Docs: Type Safety in Navigation Compose](https://developer.android.com/guide/navigation/design/type-safety)
- [Google Codelabs: Navigation Compose Type Safety](https://developer.android.com/codelabs/basic-android-kotlin-compose-navigation)

**Notes:**
- This is a significant upgrade to navigation architecture
- Aligns with Google's Modern Android Development standards
- Makes codebase more maintainable and less error-prone
- Required dependency: kotlinx-serialization-json 1.6.3+

---

## Phase 3: ViewModel State Consolidation (High Priority)

**Goal:** Modernize all ViewModels to Google's recommended patterns
**Estimated Time:** 4-5 hours
**Status:** [ ] Not Started

**Prerequisites:** Phase 2 must be complete (architecture overhaul)

**Scope:** Refactor 4 ViewModels:
1. AddEditEntryViewModel (consolidate 6 → 1 state)
2. EntriesViewModel (convert to StateFlow)
3. SignInViewModel (rename state only)
4. MoodColorManagerViewModel (already follows best practices from Phase 2)

### 📊 ViewModel Audit Summary (Completed 2025-10-23)

**Current State (After Phase 2):**
- **Total ViewModels:** 3 (AddEditMoodColorViewModel removed in Phase 2)
- **Following Best Practices:** 1 (SignInViewModel ✅)
- **Needs Refactoring:** 2

**Findings:**
1. ✅ **SignInViewModel** - PERFECT (uses single StateFlow, follows MAD guidelines)
2. 🔴 **AddEditEntryViewModel** - 6 separate state holders → needs consolidation
3. 🟡 **EntriesViewModel** - Uses single mutableStateOf → convert to StateFlow
4. ✅ **AddEditMoodColorViewModel** - REMOVED in Phase 2 (replaced with dialog)

**Actions Pattern:**
- All ViewModels now use `*Action` for user interactions (renamed in Phase 2)
- All use `*UiEvent` for one-time UI events
- SignInViewModel uses direct methods (acceptable for simple cases)

**Priority Order:**
1. Consolidate AddEditEntryViewModel (6 states → 1 StateFlow)
2. Convert EntriesViewModel to StateFlow
3. Create Root/Presenter pattern for all screens

### Task 3.1: Create Unified UiState Data Classes
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 30 minutes

**Subtasks:**
- [ ] Create `feature_daily_entry/presentation/add_edit_entry/state/AddEditEntryUiState.kt`
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

### Task 3.2: Refactor AddEditEntryViewModel (CRITICAL 🔴)
**Status:** [ ] Not Started
**Priority:** CRITICAL
**Estimated Time:** 1 hour

**Current Issues:**
- Has **6 separate state holders**: `_entryDate`, `_entryMood`, `_entryContent`, `_entryColor`, `_eventFlow`, `_state`
- Violates single source of truth principle
- State updates scattered across multiple properties
- Makes testing difficult and error-prone

**Subtasks:**
- [ ] Replace 6 separate state holders with single StateFlow:
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

### Task 3.3: Convert EntriesViewModel to StateFlow
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 30 minutes

**Current State:**
- Already uses single state holder: `_state = mutableStateOf(EntriesState())`
- Works correctly but uses `mutableStateOf` instead of `StateFlow`

**Subtasks:**
- [ ] Convert `mutableStateOf` to `MutableStateFlow`
- [ ] Add `recentlyDeletedEntry` to EntriesUiState
- [ ] Update screen to use `collectAsStateWithLifecycle()`
- [ ] Test entries display still works

**Files to Modify:**
- `feature_daily_entry/presentation/entries/EntriesViewModel.kt`
- `feature_daily_entry/presentation/entries/EntriesScreen.kt`

---

### Task 3.4: Implement Root/Presenter Pattern for All Screens
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 2 hours

**Goal:** Split all screens into Root (state management) + Presenter (pure UI)

**Subtasks:**
- [ ] **AddEditEntryScreen**
  - [ ] Create `AddEditEntryScreenRoot.kt` (handles ViewModel, state collection, navigation)
  - [ ] Rename existing to `AddEditEntryScreen.kt` (private, pure UI)
  - [ ] Pass state + callbacks as parameters

- [ ] **EntriesScreen**
  - [ ] Create `EntriesScreenRoot.kt`
  - [ ] Rename existing to `EntriesScreen.kt` (private, pure UI)

- [ ] **SignInScreen**
  - [ ] Create `SignInScreenRoot.kt`
  - [ ] Rename existing to `SignInScreen.kt` (private, pure UI)

**Pattern:**
```kotlin
// Root - State management
@Composable
fun AddEditEntryScreenRoot(
    viewModel: AddEditEntryViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvents.collect { event ->
            when (event) {
                is AddEditEntryUiEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    AddEditEntryScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

// Presenter - Pure UI
@Composable
private fun AddEditEntryScreen(
    state: AddEditEntryUiState,
    onAction: (AddEditEntryAction) -> Unit,
    modifier: Modifier = Modifier
) {
    // Pure UI implementation
}
```

---

### Task 3.5: Update All Screens to Use collectAsStateWithLifecycle()
**Status:** [ ] Not Started
**Priority:** High
**Estimated Time:** 30 minutes

**Subtasks:**
- [ ] AddEditEntryScreen (handled in Root/Presenter)
- [ ] EntriesScreen (handled in Root/Presenter)
- [ ] SignInScreen (handled in Root/Presenter)
- [ ] Verify lifecycle-aware state collection works correctly

---

## Phase 4: Quality & Polish (Medium Priority)

**Goal:** Add error handling, clean up code, add tests
**Estimated Time:** 3-4 hours

### Task 4.1: Implement Error Handling
**Status:** [ ] Not Started
**Priority:** Medium
**Estimated Time:** 1-2 hours

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

### Task 4.2: Code Cleanup
**Status:** [ ] Not Started
**Priority:** Medium
**Estimated Time:** 1 hour

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

### Task 4.3: Add Unit Tests
**Status:** [ ] Not Started
**Priority:** Medium
**Estimated Time:** 2 hours

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

## Phase 5: Release Preparation (Low Priority)

**Goal:** Polish for public GitHub release
**Estimated Time:** 2-3 hours

### Task 5.1: Update README
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 1 hour

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

### Task 5.2: Add LICENSE
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 5 minutes

**Subtasks:**
- [ ] Choose license (MIT recommended like My-Bookshelf)
- [ ] Create LICENSE file
- [ ] Add copyright year and name
- [ ] Reference in README

**Files to Create:**
- `LICENSE`

---

### Task 5.3: Privacy & Documentation
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 30 minutes

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

### Task 5.4: Notification Improvements
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 1 hour

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

### Task 5.5: Final Polish
**Status:** [ ] Not Started
**Priority:** Low
**Estimated Time:** 1 hour

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

### Session 2 (2025-10-22) - Phase 1 Complete! ✅
**Tasks Completed:**
- ✅ Task 1.1: Add Timber Logging
- ✅ Task 1.2: Remove Hilt Dependencies
- ✅ Task 1.3: Standardize Dependency Injection
- ✅ Task 2.6: Modernize Google Sign-In to Credential Manager API (Bonus!)
- ✅ Fixed BuildConfig generation issue
- ✅ Resolved KSP compatibility (updated to 2.2.20-2.0.4)
- ✅ Updated Gradle wrapper to 8.14.3
- ✅ Updated .gitignore for proper IDE file handling
- ✅ Created PreferencesRepository interface with implementation
- ✅ Reorganized feature_sign_in package structure

**Issues Encountered:**
- BuildConfig.DEBUG unresolved: BuildConfig generation was disabled by default in AGP 8.0+
  - Solution: Added `buildConfig = true` to buildFeatures block
- KSP version incompatibility: KSP 2.0.21 incompatible with Kotlin 2.2.20
  - Solution: Updated to KSP 2.2.20-2.0.4
- BeginSignInRequest deprecation forcing immediate Google Sign-In modernization
  - Solution: Migrated to Credential Manager API (ahead of schedule)
- Clean build KSP cache issues
  - Solution: Resolved after proper sync in Android Studio

**Next Session Plan:**
- Begin Phase 2: Architecture Improvements
- Start with Task 2.1: Create UiState Data Classes
- Focus on ViewModel state consolidation

---

### Session 3 (2025-10-22) - ViewModel Audit Complete ✅
**Tasks Completed:**
- ✅ Conducted comprehensive ViewModel audit using Explore tool
- ✅ Analyzed all 4 ViewModels for architecture patterns
- ✅ Identified critical issues:
  - AddEditEntryViewModel: 6 separate state holders (critical)
  - AddEditMoodColorViewModel: 5 separate state holders (critical)
  - AddEditMoodColorScreen: Uses TWO ViewModels (anti-pattern)
  - EntriesViewModel: Uses mutableStateOf (optional improvement)
  - SignInViewModel: Perfect implementation ✅
- ✅ Updated MODERNIZATION_ROADMAP.md with detailed ViewModel Audit Summary
- ✅ Updated Phase 2 tasks with specific findings and solutions
- ✅ Added CLAUDE.md Root/Presenter pattern documentation (from My-Bookshelf)

**Architectural Decisions:**
- **Keep Actions Pattern:** 3/4 ViewModels use it - consistency over uniformity
- **StateFlow over mutableStateOf:** Google's recommended pattern for ViewModels
- **Single ViewModel per Screen:** Fix AddEditMoodColorScreen dual ViewModel issue

**Issues Encountered:**
- None - this was a planning/documentation session

**Next Session Plan:**
- Begin Phase 2 implementation
- Start with Task 2.1: Create UiState Data Classes
- Then tackle critical refactoring tasks (2.2, 2.3, 2.4)

---

### Session 4 (2025-10-23) - Architecture Overhaul Identified 🏗️
**Critical Discovery:**
- Realized jumping into ViewModel refactoring would be premature
- Identified fundamental architecture issues requiring resolution first
- User clarified MoodColor should be a **dialog**, not full screen
- User confirmed future requirement: standalone mood color management screen

**Major Architectural Insights:**
1. **MoodColorScreen → Dialog**: Currently full screen, should be modal dialog
2. **Dual ViewModel Anti-Pattern**: AddEditMoodColorScreen injects TWO ViewModels
3. **Naming Inconsistency**: Mix of `*State`, `*Event` patterns across features
4. **Feature Boundaries**: MoodColor should stay separate (bounded context) but use dialog pattern
5. **Future-Proofing**: Architecture must support standalone mood color management

**Tasks Completed:**
- ✅ Comprehensive architecture review with user
- ✅ Clarified MoodColor usage patterns (dialog now, standalone screen later)
- ✅ Defined target architecture following Google MAD best practices
- ✅ Created new **Phase 2: Architecture Overhaul** (9 tasks)
- ✅ Reorganized roadmap phases:
  - Phase 1: Foundation ✅ (Complete)
  - Phase 2: Architecture Overhaul (NEW - 9 tasks)
  - Phase 3: ViewModel State Consolidation (reorganized, 5 tasks)
  - Phase 4: Quality & Polish (3 tasks)
  - Phase 5: Release Preparation (5 tasks)
- ✅ Updated MODERNIZATION_ROADMAP.md with comprehensive architecture plan

**Key Architectural Decisions:**
1. **MoodColor as Separate Feature** - Keep as bounded context (has its own data layer)
2. **Dialog Pattern** - Convert AddEditMoodColorScreen → MoodColorPickerDialog component
3. **Callback Pattern** - Dialog uses callbacks, no ViewModel injection
4. **Naming Standardization**:
   - All state: `*UiState`
   - User interactions: `*Action`
   - One-time UI events: `*UiEvent`
5. **Root/Presenter Pattern** - All screens split into Root (state) + Presenter (UI)
6. **Future MoodColorManagerScreen** - Plan documented, defer to post-Phase 3

**Roadmap Summary:**
- **Phase 2** (Architecture): 9 tasks, 4-6 hours estimated
  - Convert screen → dialog
  - Standardize naming (Event→Action, State→UiState)
  - Rename display_daily_entries → entries
  - Create state/ directories
  - Remove AddEditMoodColorViewModel
  - Update DI and navigation

- **Phase 3** (ViewModels): 5 tasks, 3-4 hours estimated
  - Create unified UiState classes
  - Refactor AddEditEntryViewModel (6→1 state)
  - Convert EntriesViewModel to StateFlow
  - Implement Root/Presenter pattern for all screens

**Issues Encountered:**
- None - pure planning and architecture design session

**Next Session Plan:**
- Begin Phase 2: Architecture Overhaul
- Start with Task 2.1: Convert AddEditMoodColorScreen → MoodColorPickerDialog
- This solves dual ViewModel anti-pattern immediately

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
- ~~Google Sign-In update~~ ✅ Completed in Phase 1
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

**Last Updated:** 2025-10-23
**Current Status:** Phase 1 Complete ✅ → Phase 2 Ready (Architecture Overhaul)