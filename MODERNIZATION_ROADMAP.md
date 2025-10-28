# The Day To - Modernization Roadmap

**Target:** Bring this app up to portfolio quality matching [My-Bookshelf](https://github.com/Zlurgg/My-Bookshelf)

**Standards:** Google's Modern Android Development (MAD) + SOLID/CLEAN/DRY/SRP principles

See [CLAUDE.md](./CLAUDE.md) for detailed coding standards and architectural guidelines.

---

## Progress Overview

**Current Phase:** Phase 2 COMPLETE ✅ (Type-Safe Navigation & Business Logic) → Moving to Code Quality
**Overall Progress:** Phase 1: 3/3 ✅ | Phase 2: COMPLETED ✅ | Phase 3: Partial | Overall: ~70% complete
**Last Updated:** 2025-10-26

**NOTE:** Phase 2 was completed via comprehensive refactoring:
- Session 5: Structure overhaul (Clean Architecture, package modernization)
- Session 6: Type-safe navigation, business logic separation, ViewModel modernization

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
**Status:** [✅] COMPLETE (Session 6, 2025-10-26)
**Priority:** High
**Estimated Time:** 2-3 hours
**Actual Time:** ~2 hours

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
- [✅] Add Kotlin Serialization plugin to `build.gradle.kts` (project level):
  ```kotlin
  plugins {
      id("org.jetbrains.kotlin.plugin.serialization") version "2.2.20"
  }
  ```
- [✅] Add kotlinx-serialization dependency to `build.gradle.kts` (app level):
  ```kotlin
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
  ```

**Part B: Create Type-Safe Routes**
- [✅] Create `core/ui/navigation/NavigationRoutes.kt`:
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
- [✅] Update `TheDayToApp.kt` to use type-safe composable routes
- [✅] Remove string-based route parsing
- [✅] Use `backStackEntry.toRoute<T>()` for argument retrieval
- [✅] Clean navigation graph (no AddEditMoodColorScreen - deferred)

**Part D: Update Navigation Calls**
- [✅] Update all `navController.navigate(Screen.*.route)` calls to use route objects
- [✅] Update in OverviewScreen, EditorScreen, SignInScreen
- [✅] Update back navigation to use type-safe routes

**Part E: Clean Up**
- [✅] Delete `core/ui/Screen.kt` (replaced by NavigationRoutes)
- [✅] Remove all `Screen.*` references
- [✅] Test all navigation flows work correctly
- [✅] Verify navigation graph is correct

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

composable(route = Screen.AddEditEntryScreen.route + "?entryId={entryId}") {  }
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

### Session 5 (2025-10-24) - Phase 2 Complete via Structure Overhaul ✅

**Major Accomplishment:**
Instead of incremental Phase 2 tasks, completed a comprehensive architecture refactoring using Android Studio's refactoring tools.

**Tasks Completed:**
- ✅ **Implemented Clean Architecture Data/Domain Separation**
  - Created `EntryEntity` and `MoodColorEntity` in `data/model/`
  - Created `EntryMapper` and `MoodColorMapper` in `data/mapper/`
  - Removed `@Entity` from domain models (`Entry`, `MoodColor`)
  - Updated DAOs to work with entities
  - Updated repositories to use mappers (entity ↔ domain conversion)
  - Updated database to register entities

- ✅ **Package Structure Modernization**
  - Renamed `feature_daily_entry` → `journal`
  - Renamed `feature_mood_color` → `journal` (consolidated)
  - Renamed `feature_sign_in` → `auth`
  - Renamed `use_case` → `usecases`
  - Renamed `presentation` → `ui`
  - Renamed UI screens: `add_edit` → `editor`, `entries` → `overview`
  - Fixed all snake_case packages to lowercase

- ✅ **Fixed Architectural Violations**
  - Moved `PreferencesRepositoryImpl` from domain to `core.data.repository`
  - Moved `GoogleAuthUiClient` to `auth.data.service`
  - Moved theme to `core.ui.theme`
  - Reorganized notifications to `core.service.notifications`
  - Fixed duplicate path `core.data.data` → `core.data`
  - Moved constants from domain to data layer

- ✅ **Naming Standardization**
  - All packages now use proper lowercase conventions
  - Consistent entity naming (EntryEntity vs Entry)
  - Clean separation of concerns

**Methodology:**
- Used Android Studio's "Refactor → Move" and "Refactor → Rename" tools
- Leveraged IDE's automatic import updates
- Manual fixes for remaining edge cases
- Incremental testing throughout refactoring

**Result:**
- ✅ App compiles successfully
- ✅ App builds and runs
- ✅ User flow tested and working
- ✅ Clean Architecture properly implemented
- ✅ Package structure modernized
- ✅ All documentation updated (CLAUDE.md)

**Phase 2 Status:**
While the originally planned incremental tasks (2.1-2.9) weren't followed exactly, the **architectural goals were achieved** through this comprehensive refactoring:
- ✅ Clean architecture boundaries established
- ✅ Proper data/domain separation
- ✅ Modern package naming
- ✅ Eliminated architectural violations
- ⚠️ MoodColor dialog pattern - deferred (current implementation working)
- ⚠️ Type-safe navigation - deferred for future enhancement

**Issues Encountered:**
- Build errors during refactoring due to outdated imports (resolved with IDE refactoring)
- Package declaration mismatches (resolved by fixing in Android Studio)
- All errors successfully resolved

**Next Session Plan:**
- Begin Phase 3: ViewModel State Consolidation
- Verify all ViewModels follow StateFlow pattern
- Implement Root/Presenter pattern for remaining screens
- Consider creating MoodColorPickerDialog if needed

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

---

### Session 6 (2025-10-26) - Type-Safe Navigation & Business Logic Refactoring ✅

**Major Accomplishment:**
Completed comprehensive modernization following Google's 2025 Android best practices - type-safe navigation, proper business logic separation, and architectural cleanup.

**Tasks Completed:**

**1. Type-Safe Navigation Migration ✅**
- ✅ Added Kotlin Serialization plugin to build system
  - Added `kotlin-serialization` plugin to `libs.versions.toml`
  - Applied plugin in root and app `build.gradle.kts`
  - Added `kotlinx-serialization-json` dependency (v1.7.3)
- ✅ Created `NavigationRoutes.kt` with `@Serializable` route objects:
  - `SignInRoute` (object)
  - `OverviewRoute` (object)
  - `EditorRoute(entryId: Int?, showBackButton: Boolean)` (data class)
  - `MoodColorRoute`, `NotificationTestRoute` (placeholder objects)
- ✅ Updated all navigation calls to use type-safe routes
- ✅ Deleted old `Screen.kt` sealed class
- ✅ Updated `TheDayToApp.kt` to use type-safe `composable<T>` pattern

**2. SignInViewModel Modernization ✅**
- ✅ Created `SignInUiEvent.kt` sealed interface
  - `NavigateToOverview` - Navigate after successful sign-in
  - `ShowSnackbar(message)` - Display error messages
- ✅ Injected `GoogleAuthUiClient` and `PreferencesRepository` via Koin
- ✅ Added `signIn(activityContext)` method - handles auth flow with error handling
- ✅ Added `checkSignInStatus()` method - auto-navigate if already signed in
- ✅ Moved all auth business logic from composable to ViewModel
- ✅ Updated `signInModule` in Koin DI

**3. SignInScreen Root/Presenter Pattern ✅**
- ✅ Created `SignInScreenRoot` composable (handles ViewModel + events)
- ✅ Converted existing to `SignInScreen` private presenter (pure UI)
- ✅ Replaced Toast with Snackbar via UiEvents
- ✅ Auto-checks sign-in status on launch (via `checkSignInStatus()`)
- ✅ Proper event handling with `LaunchedEffect`

**4. OverviewViewModel Sign-Out Logic ✅**
- ✅ Added `SignOut` action to `OverviewAction`
- ✅ Added `NavigateToSignIn` event to `OverviewUiEvent`
- ✅ Injected `GoogleAuthUiClient` and `PreferencesRepository`
- ✅ Implemented debounced loading for sign-out (150ms threshold)
- ✅ Proper error handling with Snackbar events
- ✅ Updates sign-in state in SharedPreferences
- ✅ Updated `overviewModule` in Koin DI

**5. OverviewScreen Event Handling ✅**
- ✅ Added `NavigateToSignIn` event handling in Root composable
- ✅ Removed `onSignOut` callback parameter
- ✅ Sign-out button now triggers `OverviewAction.SignOut`
- ✅ Business logic fully moved to ViewModel

**6. TheDayToApp Complete Refactoring ✅**
- ✅ Removed all business logic (previously 163 lines → now 83 lines, 49% reduction)
- ✅ Removed manual repository instantiation (`TheDayToPrefRepository`)
- ✅ Removed Toast usage (replaced with Snackbar in ViewModels)
- ✅ Removed Context usage for business logic
- ✅ Removed `googleAuthUiClient` parameter (now injected in ViewModels)
- ✅ Implemented type-safe navigation with Kotlin Serialization
- ✅ Proper back stack management with `popUpTo`
- ✅ Now a pure navigation graph (clean architecture)

**7. MainActivity Cleanup ✅**
- ✅ Removed `GoogleAuthUiClient` injection
- ✅ Removed unused `by inject()` import
- ✅ Simplified `TheDayToApp()` call (no parameters)

**8. PreferencesRepository Enhancement ✅**
- ✅ Added `SIGNED_IN_STATE` constant to `Constants.kt`
- ✅ Added `setSignedInState(Boolean)` to interface
- ✅ Added `getSignedInState(): Boolean` to interface
- ✅ Implemented in `PreferencesRepositoryImpl`
- ✅ Proper sign-in state management throughout app

**9. Navigation Updates ✅**
- ✅ Updated EditorScreen navigation to use `EditorRoute`
- ✅ Updated OverviewScreen navigation to use `EditorRoute`
- ✅ All screens now use type-safe navigation
- ✅ Compile-time safety for all navigation calls

**Architectural Improvements:**

**Before This Session:**
- ❌ String-based navigation ("overview_screen")
- ❌ Business logic in TheDayToApp composable
- ❌ Manual repository instantiation
- ❌ Toast for user feedback
- ❌ GoogleAuthUiClient passed through UI layer
- ❌ Sign-in state checked in composables
- ❌ No clear separation of concerns

**After This Session:**
- ✅ Type-safe navigation with Kotlin Serialization
- ✅ All business logic in ViewModels
- ✅ Full dependency injection via Koin
- ✅ Snackbar with proper event handling
- ✅ GoogleAuthUiClient injected into ViewModels
- ✅ Sign-in state managed via repository pattern
- ✅ Clear Root/Presenter pattern throughout
- ✅ Proper State vs Events separation

**Key Patterns Applied:**
1. **Type-Safe Navigation** - Kotlin Serialization with `@Serializable` routes
2. **State vs Events** - State = persistent, Events = one-time actions
3. **Root/Presenter** - Root handles ViewModel/events, Presenter is pure UI
4. **Debounced Loading** - 150ms threshold prevents jarring flashes
5. **Repository Pattern** - Sign-in state managed via PreferencesRepository

**Files Created:**
- `core/ui/navigation/NavigationRoutes.kt`
- `auth/ui/state/SignInUiEvent.kt`
- `journal/ui/overview/state/OverviewUiEvent.kt` (was missing)

**Files Modified:**
- `gradle/libs.versions.toml` (Kotlin Serialization dependencies)
- `build.gradle.kts` (root - serialization plugin)
- `app/build.gradle.kts` (serialization plugin + dependency)
- `auth/ui/SignInViewModel.kt` (complete refactoring)
- `auth/ui/SignInScreen.kt` (Root/Presenter pattern)
- `journal/ui/overview/OverviewViewModel.kt` (sign-out logic)
- `journal/ui/overview/OverviewScreen.kt` (event handling)
- `journal/ui/editor/EditorScreen.kt` (type-safe navigation)
- `core/ui/TheDayToApp.kt` (complete refactoring)
- `MainActivity.kt` (simplified)
- `core/domain/repository/PreferencesRepository.kt` (sign-in state methods)
- `core/data/repository/PreferencesRepositoryImpl.kt` (implementation)
- `core/data/util/Constants.kt` (SIGNED_IN_STATE)
- `di/ViewModelModules.kt` (updated DI for SignInViewModel, OverviewViewModel)

**Files Deleted:**
- `core/ui/Screen.kt` (replaced by NavigationRoutes)

**Issues Encountered:**
- Missing `setSignedInState()` and `getSignedInState()` methods in PreferencesRepository
  - Solution: Added interface methods and implementation
- All other changes applied cleanly

**Phase 2 Tasks Status (Aligned with Original Plan):**
- ✅ Task 2.9: Migrate to Type-Safe Navigation - **COMPLETE**
- ✅ Bonus: Moved all business logic from TheDayToApp to ViewModels
- ✅ Bonus: Proper sign-in/sign-out flow via ViewModels
- ⚠️ Task 2.1-2.8: Deferred (MoodColor dialog pattern) - current implementation working

**Result:**
- ✅ App compiles successfully
- ✅ Navigation fully type-safe with compile-time checking
- ✅ All business logic in ViewModels
- ✅ Proper dependency injection throughout
- ✅ Clean architecture with clear boundaries
- ✅ Ready for Phase 3 ViewModel refinement

**Next Session Plan:**
- Address high-priority code review findings:
  1. Add error handling to all viewModelScope.launch blocks
  2. Remove `!!` from NotificationWorker
  3. Change `commit()` to `apply()` in PreferencesRepositoryImpl
  4. Remove commented code
- Consider Phase 3 ViewModel consolidation
- Plan for testing implementation

---

---

### Session 7 (2025-10-27) - Material Design 3 Enhancements ✨

**Focus:** UI/UX improvements with Material Design 3, architectural cleanup, and proper separation of concerns

**Tasks Completed:**

**1. Material Design 3 Theme Implementation ✅**
- ✅ Disabled dynamic colors (Material You) by default in `Theme.kt`
  - Added `useDynamicColor` parameter defaulting to `false`
  - Ensures custom warm color scheme is applied
- ✅ Created custom warm color palette for mood tracking app
  - Light theme: Soft purple primary (#7B6FA1), warm off-white background (#FFFBF7)
  - Dark theme: Light lavender (#D5BAFF), warm dark brown (#1A1614)
  - Calming, nature-inspired colors appropriate for mood journaling
- ✅ Implemented complete Material 3 typography scale in `Type.kt`
  - Display, Headline, Title, Body, Label styles
  - All 15 typography variants defined with proper sizing and spacing

**2. Calendar View Improvements ✅**
- ✅ Fixed day-of-week alignment (lines 146-155 in OverviewScreen.kt)
  - Calculated `firstDayOfWeek` using `date.withDayOfMonth(1).dayOfWeek.value`
  - Added empty cells before 1st day: `emptyCellsAtStart = firstDayOfWeek - 1`
  - Grid now properly aligns days with M T W T F S S headers
- ✅ Reduced spacing between calendar components
  - Removed Box padding around calendar pager
  - Reduced calendar grid top padding from 4.dp to 0.dp
  - Reduced DayOfWeekHeader vertical padding to top=0.dp, bottom=4.dp
  - Tighter, more cohesive calendar layout

**3. Dynamic Greeting Feature ✅**
- ✅ Added time-based greeting to OverviewScreen
  - "Good morning" (5-11), "Good afternoon" (12-16), "Good evening" (17-20), "Good night" (21-4)
  - Added `greeting: String` to `OverviewUiState`
  - Implemented `updateGreeting()` in `OverviewViewModel`
  - Proper MVVM - business logic in ViewModel, not UI

**4. Month/Year Picker Dialog Refinements ✅**
- ✅ Optimized text sizing and spacing
  - Reduced year text to `labelMedium` typography
  - Reduced year grid height from 120.dp to 100.dp
  - Reduced month grid height to 220.dp (user-adjusted)
  - Reduced component spacing from 16.dp to 12.dp, and 8.dp to 6.dp
  - Dialog now fits better without excessive scrolling

**5. Editor Screen UX Improvements ✅**
- ✅ Removed date picker component
  - Replaced with simple read-only date display
  - Aligns with app's daily tracking pattern (one entry per day)
  - Cleaner, less cluttered UI
- ✅ Fixed content box height
  - Changed from `.fillMaxHeight()` to fixed `.height(200.dp)`
  - Prevents FAB (save button) from overlapping content

**6. Mood Color Dropdown Enhancements ✅**
- ✅ Fixed auto-population of newly created mood colors
  - Added `onMoodSelected(mood, colorHex)` call after `onSaveMoodColor`
  - Users no longer need to reopen dropdown to select new mood
- ✅ Improved dropdown UI (MoodItem.kt)
  - Color indicators: 12.dp squares → 24.dp circles with `CircleShape`
  - Better layout with `Arrangement.SpaceBetween`
  - Proper spacing with `Spacer` components (12.dp, 8.dp)
  - Delete icon uses `MaterialTheme.colorScheme.error` tint
  - Enhanced "Add new mood color" button:
    - Changed from `IconButton` to full `DropdownMenuItem`
    - Added text label with icon
    - Centered layout with primary color theming

**7. Business Logic Architectural Cleanup ✅**
- ✅ Identified and fixed business logic in UI layer (EditorScreen.kt line 166-168)
  - **Violation:** UI was comparing `entryDate` with `LocalDate.now()` to choose hint
  - **Fix:** Moved hint selection to `EditorViewModel.updateMoodHint()`
  - **Result:** Clean separation - ViewModel computes hint, UI just displays it
- ✅ Updated `EditorUiState` structure
  - Removed separate `todayHint` and `previousDayHint` fields
  - Added single dynamic `moodHint` field computed in ViewModel
  - Simplified state management
- ✅ Audited all UI components for business logic violations
  - MoodItem.kt: ✅ Pure presenter
  - DayOfWeekHeader.kt: ✅ Pure presenter
  - ContentItem.kt: ✅ Pure presenter
  - MonthYearPicker.kt: ✅ Acceptable UI state (form state, not business logic)
  - OverviewScreen.kt: ✅ Acceptable (calendar layout calculations are view logic)

**Architectural Principles Applied:**
1. **MVVM Compliance** - Business logic in ViewModels, UI is pure presentation
2. **Single Source of Truth** - Computed state lives in ViewModel, not UI
3. **Material Design 3** - Proper theming, typography, and color usage
4. **Performance** - State computed once in ViewModel, not on every recomposition
5. **UX Best Practices** - Auto-population, proper spacing, clear visual hierarchy

**Files Modified:**
- `core/ui/theme/Theme.kt` (dynamic color control)
- `core/ui/theme/Color.kt` (complete warm color scheme)
- `core/ui/theme/Type.kt` (Material 3 typography scale)
- `journal/ui/overview/OverviewScreen.kt` (calendar alignment, spacing, greeting)
- `journal/ui/overview/OverviewViewModel.kt` (greeting logic)
- `journal/ui/overview/state/OverviewUiState.kt` (greeting field)
- `journal/ui/overview/components/DayOfWeekHeader.kt` (spacing)
- `journal/ui/overview/components/MonthYearPicker.kt` (sizing)
- `journal/ui/editor/EditorScreen.kt` (date display, business logic fix)
- `journal/ui/editor/EditorViewModel.kt` (hint logic)
- `journal/ui/editor/state/EditorUiState.kt` (hint field)
- `journal/ui/editor/components/MoodItem.kt` (auto-population, UI improvements)
- `journal/ui/editor/components/ContentItem.kt` (fixed height)

**Issues Encountered & Fixed:**
- Custom theme colors not applying → disabled dynamic colors
- Calendar days misaligned with headers → added empty cell calculation
- Business logic in UI layer → moved to ViewModel
- Mood color not auto-selecting → added callback chaining
- All issues resolved successfully

**Result:**
- ✅ App has cohesive, calming Material Design 3 theme
- ✅ Calendar properly aligned and spaced
- ✅ Improved UX with auto-population and dynamic greeting
- ✅ All business logic violations fixed
- ✅ Clean architecture maintained throughout
- ✅ App builds and runs successfully

**Next Session Plan:**
- Continue with code quality improvements:
  - Add error handling to viewModelScope.launch blocks
  - Remove `!!` operators
  - Clean up commented code
- Consider Phase 3 ViewModel consolidation tasks
- Plan for unit testing implementation

---

### Session 8 (2025-10-28) - UseCase Architecture & Sign-Out Dialog Refactoring ✅

**Focus:** Business logic extraction into UseCases, bounded context violation fixes, and proper Unidirectional Data Flow

**Tasks Completed:**

**1. UseCase Architecture Refactoring ✅**
- ✅ Renamed UseCase packages to mirror UI structure
  - `journal/domain/usecases/entry` → `journal/domain/usecases/overview`
  - `journal/domain/usecases/moodcolor` → `journal/domain/usecases/editor`
  - Aligns UseCases with the ViewModels that use them
  - Cleaner one-to-one mapping

- ✅ Eliminated UseCase crossover between ViewModels
  - **Issue:** Both OverviewViewModel and EditorViewModel used `EntryUseCases`
  - **Fix:**
    - Moved `GetEntryUseCase` and `AddEntryUseCase` to `EditorUseCases`
    - Created `RestoreEntryUseCase` for overview's undo functionality
    - Result: Clean separation - no shared UseCases between ViewModels

**2. Business Logic Extraction (7 New UseCases Created) ✅**
- ✅ **SignOutUseCase** (auth/domain/usecases)
  - Encapsulates sign-out logic (GoogleAuthUiClient + AuthStateRepository)
  - Removes auth dependency from OverviewViewModel

- ✅ **SignInUseCase** (auth/domain/usecases)
  - Handles sign-in flow + saves auth state
  - Returns SignInResult with error handling

- ✅ **CheckSignInStatusUseCase** (auth/domain/usecases)
  - Checks if user is signed in (local state + Google)
  - Combines both data sources

- ✅ **CheckTodayEntryUseCase** (auth/domain/usecases)
  - Determines post-login navigation (Overview vs Editor)
  - Returns today's entry timestamp if exists

- ✅ **SetupNotificationUseCase** (journal/domain/usecases/overview)
  - Wraps notification setup logic

- ✅ **CheckNotificationPermissionUseCase** (journal/domain/usecases/overview)
  - Checks notification permission status

- ✅ **RestoreEntryUseCase** (journal/domain/usecases/overview)
  - Dedicated UseCase for undo functionality

- ✅ **DateUtils** (core/domain/util)
  - Created centralized date logic
  - `getTodayStartEpoch()` - removes duplication across ViewModels

**3. ViewModel Cleanup - Repository Dependencies Removed ✅**
- ✅ **OverviewViewModel**
  - Removed: GoogleAuthUiClient, AuthStateRepository, NotificationRepository
  - Now depends only on: OverviewUseCases + standalone SignOutUseCase

- ✅ **EditorViewModel**
  - Removed: OverviewUseCases dependency (was crossover)
  - Now depends only on: EditorUseCases
  - Uses DateUtils instead of inline date calculation

- ✅ **SignInViewModel**
  - Removed: GoogleAuthUiClient, AuthStateRepository
  - Now depends only on: SignInUseCases
  - All business logic encapsulated in UseCases

**4. Sign-Out Dialog with Unidirectional Data Flow ✅**
- ✅ **Bounded Context Violation Fix**
  - **Issue:** OverviewViewModel (journal) depending on SignOutUseCase (auth)
  - **User's Solution:** Create SignOutDialog in auth package with confirmation (standard UX)
  - **Result:** Journal domain no longer depends on auth domain

- ✅ **Created SignOutDialog Component** (auth/ui/components)
  - Stateless confirmation dialog
  - No business logic, pure UI component
  - Material 3 themed with error-colored confirm button

- ✅ **Updated OverviewAction**
  - Renamed `SignOut` → `RequestSignOut`
  - Clear intent: request to show dialog

- ✅ **Updated OverviewUiEvent**
  - Added `ShowSignOutDialog` event

- ✅ **Updated OverviewViewModel**
  - Removed SignOutUseCase dependency
  - Emits `ShowSignOutDialog` event instead of performing sign-out
  - Clean architecture: journal domain doesn't know about auth operations

- ✅ **Updated OverviewScreenRoot**
  - Handles `ShowSignOutDialog` event
  - Calls `onShowSignOutDialog()` callback
  - Passes event up to navigation layer

- ✅ **Updated Navigation Layer (TheDayToApp.kt)**
  - Injects `SignOutUseCase` via Koin
  - Manages dialog state (`showSignOutDialog`)
  - Shows `SignOutDialog` when triggered
  - Handles sign-out business logic and navigation
  - Proper Unidirectional Data Flow pattern

- ✅ **Updated DI (ViewModelModules.kt)**
  - Removed `signOutUseCase` from `overviewModule`

**5. SignInScreen UI/UX Improvements ✅**
- ✅ **Component Separation**
  - Created `WelcomeHeader.kt` - Welcome text with animations
  - Created `SignInButton.kt` - Material 3 elevated card button
  - Created `SignInFooter.kt` - "Continue with Google" text

- ✅ **Material 3 Styling**
  - Elevated card button with proper color scheme
  - Staggered entrance animations (fade + slide)
  - Typography: `displaySmall`, `displayLarge`, `titleMedium`
  - Matches app's overall aesthetic

- ✅ **Removed Unused State**
  - `SignInState` was not being used anywhere
  - Removed parameter from SignInScreen

- ✅ **Better Error Messages** (GoogleAuthUiClient.kt)
  - "No Google account found. Please add a Google account in Settings → Accounts."
  - "Configuration error. Please contact support."
  - "Network error. Please check your connection and try again."
  - Fixed Timber usage: `Timber.e()` instead of `Timber.Forest.e()`

**6. Compose Previews Added ✅**
- ✅ **Created SampleEntries.kt** (journal/ui/overview/util)
  - 3 realistic sample entries with different moods/colors/dates
  - Reusable for both previews and testing

- ✅ **OverviewScreen Previews**
  - `OverviewScreenPreview` (Light + Dark) - with 3 sample entries
  - `OverviewScreenEmptyPreview` (Light + Dark) - empty state

- ✅ **EditorScreen Previews**
  - `EditorScreenNewEntryPreview` (Light + Dark) - new entry creation
  - `EditorScreenEditEntryPreview` (Light + Dark) - editing existing entry
  - `EditorScreenLoadingPreview` (Light + Dark) - loading state during save

**Architectural Improvements:**

**Unidirectional Data Flow Pattern:**
```
User clicks "Sign Out" button
    ↓
OverviewAction.RequestSignOut
    ↓
OverviewViewModel emits OverviewUiEvent.ShowSignOutDialog
    ↓
OverviewScreenRoot receives event → onShowSignOutDialog()
    ↓
Navigation layer (TheDayToApp) manages dialog state
    ↓
SignOutDialog displays (stateless, no ViewModel)
    ↓
User confirms → Navigation layer calls SignOutUseCase → Navigate to SignIn
```

**Before This Session:**
- ❌ ViewModels directly depended on repositories and data services
- ❌ Bounded context violation (journal → auth)
- ❌ UseCase crossover between ViewModels
- ❌ Business logic in navigation layer
- ❌ No sign-out confirmation dialog

**After This Session:**
- ✅ ViewModels depend ONLY on UseCases (Clean Architecture)
- ✅ No bounded context violations
- ✅ Clean UseCase separation (one set per ViewModel)
- ✅ Business logic in ViewModels and UseCases only
- ✅ Proper UDF with confirmation dialog
- ✅ Comprehensive Compose previews

**Key Patterns Applied:**
1. **UseCase Pattern** - Single responsibility, encapsulated business logic
2. **Bounded Context Separation** - Auth and journal features independent
3. **Unidirectional Data Flow** - Events up, data down
4. **Stateless Dialog** - No ViewModel needed, pure UI component
5. **Repository Pattern** - Date utils centralized, no duplication
6. **Preview Patterns** - Light/dark modes, multiple states

**Files Created:**
- `auth/domain/usecases/SignOutUseCase.kt`
- `auth/domain/usecases/SignInUseCase.kt`
- `auth/domain/usecases/CheckSignInStatusUseCase.kt`
- `auth/domain/usecases/CheckTodayEntryUseCase.kt`
- `auth/domain/usecases/SignInUseCases.kt` (aggregator)
- `journal/domain/usecases/overview/SetupNotificationUseCase.kt`
- `journal/domain/usecases/overview/CheckNotificationPermissionUseCase.kt`
- `journal/domain/usecases/overview/RestoreEntryUseCase.kt`
- `core/domain/util/DateUtils.kt`
- `auth/ui/components/SignOutDialog.kt`
- `auth/ui/components/WelcomeHeader.kt`
- `auth/ui/components/SignInButton.kt`
- `auth/ui/components/SignInFooter.kt`
- `journal/ui/overview/util/SampleEntries.kt`

**Files Modified:**
- `journal/ui/overview/OverviewViewModel.kt` (removed repo dependencies)
- `journal/ui/overview/OverviewScreen.kt` (UDF pattern, preview)
- `journal/ui/overview/state/OverviewAction.kt` (RequestSignOut)
- `journal/ui/overview/state/OverviewUiEvent.kt` (ShowSignOutDialog)
- `journal/ui/editor/EditorViewModel.kt` (removed crossover)
- `journal/ui/editor/EditorScreen.kt` (preview)
- `auth/ui/SignInViewModel.kt` (uses SignInUseCases)
- `auth/ui/SignInScreen.kt` (components, removed unused state)
- `auth/data/service/GoogleAuthUiClient.kt` (better error messages)
- `core/ui/TheDayToApp.kt` (dialog management)
- `di/AppModule.kt` (new UseCases)
- `di/ViewModelModules.kt` (updated dependencies)

**Issues Encountered & Fixed:**
- None - All changes applied cleanly and built successfully

**Phase 4 Tasks Completed:**
- ✅ Extracted business logic into UseCases
- ✅ Fixed bounded context violations
- ✅ Proper Unidirectional Data Flow
- ✅ Material 3 component improvements
- ✅ Comprehensive preview coverage

**Result:**
- ✅ App builds successfully
- ✅ Clean Architecture properly enforced
- ✅ No bounded context violations
- ✅ ViewModels depend only on domain layer
- ✅ Proper separation of concerns throughout
- ✅ Preview system in place for rapid UI development

**Next Session Plan:**
- Continue with Phase 4 code quality:
  - Add error handling to viewModelScope.launch blocks
  - Remove `!!` operators (NotificationWorker)
  - Change `commit()` to `apply()` in PreferencesRepositoryImpl
  - Remove commented code throughout codebase
- Consider Phase 4.3: Add Unit Tests
- Plan for Phase 5: Release Preparation

---

**Last Updated:** 2025-10-28
**Current Status:** Phase 2 COMPLETE ✅ → Material Design 3 Enhancements COMPLETE ✅ → Code Quality Focus (UseCases ✅, Previews ✅)