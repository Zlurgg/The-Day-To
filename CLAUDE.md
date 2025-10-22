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

### Target Quality Standard
This project is being modernized to match the quality of **[My-Bookshelf](https://github.com/Zlurgg/My-Bookshelf)** as a portfolio piece for GitHub release.

### Development Standards
This project follows **Google's official Modern Android Development (MAD)** recommendations:
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide)
- [Jetpack Compose Best Practices](https://developer.android.com/jetpack/compose/architecture)
- [Material Design 3 Guidelines](https://m3.material.io/)
- [Guide to App Architecture](https://developer.android.com/topic/architecture/recommendations)

---

## Architecture & Design Principles

### Architecture Pattern
**Google's Recommended Architecture: Unidirectional Data Flow (UDF) + Layered Architecture**

Following [Android's Guide to App Architecture](https://developer.android.com/topic/architecture):

```
UI Layer (presentation/)          <- Compose UI + ViewModels (State Holders)
    ├── [Feature]Screen.kt        <- Composable UI
    ├── [Feature]ViewModel.kt     <- UI state holder
    ├── state/
    │   └── [Feature]UiState.kt   <- UI state data class
    └── components/               <- Reusable composables

Domain Layer (domain/)            <- Optional business logic layer
    ├── model/                    <- Business models
    ├── repository/               <- Repository interfaces
    └── use_case/                 <- Single-responsibility use cases

Data Layer (data/)               <- Data sources and repositories
    ├── repository/              <- Repository implementations
    │   └── [Entity]RepositoryImpl.kt
    └── data_source/             <- Local/remote data sources
        ├── local/
        │   └── [Entity]Dao.kt   <- Room DAOs
        └── remote/              <- API services (if needed)
```

**Key Principles:**
- **Unidirectional Data Flow**: Data flows down, events flow up
- **Single Source of Truth**: Each piece of data has one source
- **Separation of Concerns**: UI, domain, and data layers are independent
- **Immutability**: Use immutable data classes for state

### SOLID Principles

1. **Single Responsibility Principle (SRP)**
   - Each class/function has ONE clear purpose
   - ViewModels manage UI state only
   - Use Cases handle single business operations
   - Repositories abstract data sources only

2. **Open/Closed Principle**
   - Open for extension, closed for modification
   - Use interfaces for repositories
   - Use sealed classes for state/events

3. **Liskov Substitution Principle**
   - Implementations should be interchangeable
   - Repository implementations must honor interface contracts

4. **Interface Segregation Principle**
   - Keep interfaces focused and minimal
   - Don't force implementations to depend on unused methods

5. **Dependency Inversion Principle**
   - Depend on abstractions (interfaces), not concrete implementations
   - Use dependency injection throughout

### Additional Principles

**DRY (Don't Repeat Yourself)**
- Extract common logic into shared functions/classes
- Use extension functions for repeated operations
- Create reusable Composable components

**CLEAN Code**
- Meaningful variable/function names
- Functions should be small and focused
- Proper error handling throughout
- Comprehensive documentation

---

## Tech Stack

### Core Framework
- **Language**: Kotlin 2.2.20
- **UI**: Jetpack Compose with Material 3
- **Architecture**: MVVM + Clean Architecture
- **Navigation**: Jetpack Navigation Compose

### Dependency Injection
- **Framework**: Koin 4.1.1
- **Pattern**: Constructor injection
- **Rule**: ALL dependencies must be injected (no manual instantiation)

### Data Persistence
- **Database**: Room 2.8.2
- **Preferences**: SharedPreferences via repository pattern
- **Strategy**: Offline-first, local storage

### Background Work
- **Framework**: WorkManager 2.10.5
- **Use Case**: Daily notification scheduling

### Authentication
- **Provider**: Google Sign-In + Firebase Auth
- **Status**: Currently deprecated - needs update to Google Identity Services

### Networking (Future)
- **HTTP Client**: Retrofit 3.0.0 + OkHttp 5.2.1
- **Serialization**: Moshi (preferred) or Gson

### Logging
- **Framework**: Timber
- **Usage**: Replace Log.d/Log.e with Timber throughout app
- **Setup**: Initialize in Application class, plant DebugTree for debug builds

### Testing
- **Unit Tests**: JUnit 4
- **Instrumentation**: AndroidX Test
- **Mocking**: MockK (to be added)
- **Coverage Target**: 70%+ for ViewModels and Use Cases

---

## Coding Standards

### Kotlin Best Practices

Follow [Kotlin Style Guide](https://developer.android.com/kotlin/style-guide) and [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html):

1. **Naming Conventions**
   - Classes/Objects: PascalCase (e.g., `DailyEntryViewModel`)
   - Functions/Variables: camelCase (e.g., `getUserEntry`)
   - Constants: UPPER_SNAKE_CASE (e.g., `MAX_RETRY_COUNT`)
   - Composables: PascalCase (e.g., `AddEditEntryScreen`)

2. **Logging**
   - Use Timber for all logging
   - Use appropriate log levels: `Timber.d()`, `Timber.e()`, `Timber.w()`
   - Include meaningful context in log messages
   - Never use `println()` or `Log.*` directly
   ```kotlin
   // ✅ CORRECT
   Timber.d("Loading entry for date: $date")
   Timber.e(exception, "Failed to save entry")

   // ❌ WRONG
   println("Loading entry")
   Log.d("TAG", "Loading entry")
   ```

3. **Null Safety**
   - Avoid `!!` operator - use safe calls `?.` or `let`/`run`
   - Use `requireNotNull()` with descriptive messages when appropriate
   - Prefer nullable types over default values when absence is meaningful
   - Use Kotlin's null-safe operators: `?.`, `?:`, `?.let {}`

4. **Coroutines & Flow**
   - Use `viewModelScope` in ViewModels
   - Prefer `Flow` over `LiveData`
   - Use `collectAsStateWithLifecycle()` in Composables
   - Handle errors with `catch` operators

5. **Sealed Classes & Interfaces**
   - Use sealed classes for restricted type hierarchies
   - Use for UI state variants: `sealed interface UiState`
   - Use for one-time events: `sealed interface UiEvent`
   - Use for Result types: `sealed interface Result<out T>`
   ```kotlin
   // ✅ Recommended - sealed interface over sealed class
   sealed interface UiState {
       data object Loading : UiState
       data class Success(val data: List<Entry>) : UiState
       data class Error(val message: String) : UiState
   }
   ```

6. **Data Classes & Immutability**
   - Use data classes for models, state objects, DTOs
   - Prefer immutability (`val` over `var`)
   - Use `.copy()` for state updates
   - Make collections immutable by default (List vs MutableList)

7. **Extension Functions**
   - Extract common operations (e.g., `Long.toDateString()`)
   - Keep focused and well-named

### ViewModel State Management

**GOOGLE'S RECOMMENDED PATTERN: StateFlow with Immutable UI State**

Per [Android ViewModel documentation](https://developer.android.com/topic/libraries/architecture/viewmodel):

```kotlin
// ✅ CORRECT - Google's recommended pattern
data class EntryUiState(
    val entryDate: Long = System.currentTimeMillis(),
    val entryMood: String = "",
    val entryContent: String = "",
    val entryColor: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

class MyViewModel(
    private val repository: EntryRepository
) : ViewModel() {
    // Private mutable state
    private val _uiState = MutableStateFlow(EntryUiState())
    // Public immutable state
    val uiState: StateFlow<EntryUiState> = _uiState.asStateFlow()

    fun updateMood(mood: String) {
        _uiState.update { it.copy(entryMood = mood) }
    }
}

// In Composable
@Composable
fun MyScreen(viewModel: MyViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    // Use uiState...
}

// ❌ WRONG - Multiple mutable states (not recommended by Google)
class MyViewModel : ViewModel() {
    var entryDate by mutableStateOf(0L)  // Don't do this
    var entryMood by mutableStateOf("")  // Don't do this
    var entryContent by mutableStateOf("")  // Don't do this
}
```

**Key Points:**
- Use `MutableStateFlow` privately, expose `StateFlow` publicly
- Use `.asStateFlow()` to make it read-only
- Use `.update {}` for state changes (handles concurrency safely)
- Single source of truth for UI state

### Dependency Injection Rules

1. **No Manual Instantiation**
   ```kotlin
   // ❌ WRONG
   class MainActivity {
       private val authClient by lazy { GoogleAuthUiClient(this) }
   }

   // ✅ CORRECT
   class MainActivity {
       private val authClient: GoogleAuthUiClient by inject()
   }
   ```

2. **Constructor Injection**
   ```kotlin
   // ✅ CORRECT
   class MyViewModel(
       private val repository: MyRepository,
       private val useCase: MyUseCase
   ) : ViewModel()
   ```

3. **Repository Injection**
   ```kotlin
   // ❌ WRONG
   class MyViewModel(context: Context) : ViewModel() {
       private val prefRepo = TheDayToPrefRepository(context)
   }

   // ✅ CORRECT
   class MyViewModel(
       private val prefRepository: PreferencesRepository
   ) : ViewModel()
   ```

### Error Handling

1. **Use Result/Resource Wrapper**
   ```kotlin
   sealed class Resource<T> {
       data class Success<T>(val data: T) : Resource<T>()
       data class Error<T>(val message: String) : Resource<T>()
       class Loading<T> : Resource<T>()
   }
   ```

2. **Repository Layer**
   - Catch exceptions and convert to Result/Resource
   - Don't let raw exceptions propagate to ViewModels

3. **ViewModel Layer**
   - Handle all states (success, error, loading)
   - Update UI state accordingly

4. **UI Layer**
   - Display appropriate error messages
   - Provide retry mechanisms

### Compose Best Practices

Following [Jetpack Compose Best Practices](https://developer.android.com/jetpack/compose/architecture):

1. **State Hoisting**
   - Hoist state to the appropriate level (usually ViewModel)
   - Pass state down, events up (unidirectional data flow)
   - Keep Composables stateless when possible
   ```kotlin
   // ✅ CORRECT - State hoisting
   @Composable
   fun MyScreen(viewModel: MyViewModel = viewModel()) {
       val uiState by viewModel.uiState.collectAsStateWithLifecycle()
       MyContent(
           data = uiState.data,
           onEvent = viewModel::onEvent
       )
   }
   ```

2. **Reusable Components**
   - Extract common UI patterns into separate Composables
   - Use modifier parameters for flexibility
   - Follow single responsibility principle

3. **Side Effects**
   - Use `LaunchedEffect` for one-time events
   - Use `DisposableEffect` for cleanup
   - Use `rememberCoroutineScope` for event-based coroutines
   - Never launch coroutines directly in Composable body

4. **Performance**
   - Use `remember` for expensive computations
   - Use `derivedStateOf` for computed state
   - Use `key` parameter in LazyColumn/LazyRow
   - Avoid unnecessary recompositions

5. **Lifecycle Awareness**
   - Use `collectAsStateWithLifecycle()` instead of `collectAsState()`
   - Respects lifecycle, stops collection when app is backgrounded

6. **Preview Annotations**
   - Add `@Preview` for all major Composables
   - Include light and dark theme previews
   - Use `@PreviewParameter` for different states
   ```kotlin
   @Preview(name = "Light Mode", showBackground = true)
   @Preview(name = "Dark Mode", uiMode = Configuration.UI_MODE_NIGHT_YES)
   @Composable
   fun MyScreenPreview() {
       TheDayToTheme {
           MyScreen()
       }
   }
   ```

7. **Theming**
   - Use Material 3 theming system
   - Access theme colors via `MaterialTheme.colorScheme`
   - Access typography via `MaterialTheme.typography`
   - Support dynamic color (Material You)

---

## Database Guidelines

### Room Best Practices

Following [Android Room documentation](https://developer.android.com/training/data-storage/room):

1. **Versioning**
   - Increment version on schema changes
   - **Note**: No migrations needed for pre-release - clean deployment
   ```kotlin
   @Database(
       entities = [DailyEntry::class, MoodColor::class],
       version = 1,  // Increment when schema changes (no migration needed pre-release)
       exportSchema = false  // Can keep false until public release
   ) {}
   ```

2. **Migrations** (Post-Release Only)
   - Not required until app is released to users
   - Can make breaking schema changes freely during development
   - Will need migration strategy for post-release updates

3. **Async Operations**
   - All DAO methods must be `suspend` or return `Flow`
   - No blocking calls on main thread
   - Room automatically runs `suspend` functions off the main thread

4. **Queries**
   - Use `Flow<List<T>>` for observable queries (Google's recommendation)
   - Use `suspend fun` for one-shot operations
   - Avoid `LiveData` - prefer Flow for modern architecture
   ```kotlin
   // ✅ CORRECT - Google's recommended pattern
   @Dao
   interface DailyEntryDao {
       @Query("SELECT * FROM daily_entry ORDER BY dateStamp DESC")
       fun getAll(): Flow<List<DailyEntry>>  // Observable

       @Insert(onConflict = OnConflictStrategy.REPLACE)
       suspend fun insert(entry: DailyEntry)  // One-shot
   }
   ```

5. **Type Converters**
   - Use for complex types (Date, List, etc.)
   - Keep converters simple and pure

6. **Relationships**
   - Use `@Relation` for one-to-many relationships
   - Use `@Embedded` for object composition
   - Consider denormalization for better performance

---

## Known Issues to Fix

### High Priority

1. **Add Timber Logging**
   - Add Timber dependency to build.gradle.kts
   - Initialize in Application class
   - Replace any Log.d/Log.e calls with Timber

2. **Remove Hilt Dependencies**
   - Clean up build.gradle.kts
   - Remove unused Hilt annotations/imports
   - Standardize on Koin only

3. **Update Google Sign-In**
   - Replace deprecated Firebase auth flow
   - Use Google Identity Services
   - Update to Credential Manager API

4. **Standardize DI**
   - Inject GoogleAuthUiClient
   - Inject PreferencesRepository everywhere
   - Remove manual instantiation in MainActivity

5. **Consolidate ViewModel State**
   - Replace multiple `mutableStateOf` with single `StateFlow<UiState>`
   - Create proper UiState data classes
   - Implement in all ViewModels

### Medium Priority

6. **Error Handling**
   - Create Resource/Result sealed class
   - Add error handling in repositories
   - Display errors in UI

7. **Code Cleanup**
   - Remove all commented code
   - Extract magic numbers to constants
   - Improve naming consistency

8. **Testing**
   - Add ViewModel unit tests
   - Add Use Case tests
   - Add Repository tests (with fakes)

### Low Priority

9. **Documentation**
   - Update README to match My-Bookshelf quality
   - Add KDoc comments for public APIs
   - Create architecture diagram

10. **Notification Improvements**
    - Remove network constraint from WorkManager
    - Add user-configurable notification time
    - Improve notification content

---

## File Organization

### Feature Module Structure
Each feature should follow this structure:

```
feature_[name]/
├── data/
│   ├── data_source/
│   │   └── [Entity]Dao.kt
│   └── repository/
│       └── [Entity]RepositoryImpl.kt
├── domain/
│   ├── model/
│   │   └── [Entity].kt
│   ├── repository/
│   │   └── [Entity]Repository.kt
│   └── use_case/
│       ├── Get[Entity]UseCase.kt
│       ├── Add[Entity]UseCase.kt
│       ├── Update[Entity]UseCase.kt
│       ├── Delete[Entity]UseCase.kt
│       └── [Entity]UseCases.kt (aggregator)
└── presentation/
    ├── [Feature]Screen.kt
    ├── [Feature]ViewModel.kt
    ├── state/
    │   └── [Feature]UiState.kt
    └── components/
        └── [Reusable]Component.kt
```

### Core Module
```
core/
├── data/
│   └── TheDayToDatabase.kt
├── di/
│   ├── AppModule.kt
│   └── ViewModelModules.kt
├── notification/
│   ├── NotificationWorker.kt
│   └── Notifications.kt
└── util/
    ├── Constants.kt
    ├── Extensions.kt
    └── Resource.kt
```

---

## Testing Requirements

### Unit Tests (Required)

1. **ViewModels**
   - Test all state transitions
   - Test error scenarios
   - Use fake repositories

2. **Use Cases**
   - Test business logic
   - Test validation
   - Mock repositories

3. **Repositories**
   - Test data mapping
   - Test error handling
   - Use in-memory database or fakes

### Test Coverage Goals
- ViewModels: 80%+
- Use Cases: 90%+
- Repositories: 70%+

---

## Things to Avoid (Anti-Patterns)

Based on [Android's Common Mistakes](https://developer.android.com/topic/architecture/recommendations#common-mistakes):

1. ❌ Don't use `!!` operator (Kotlin anti-pattern)
2. ❌ Don't manually instantiate dependencies (violates DI principles)
3. ❌ Don't use multiple mutable states in ViewModels (use single StateFlow)
4. ❌ Don't pass Context to ViewModels (violates separation of concerns)
5. ❌ Don't pass ViewModels to Composables (pass state and callbacks instead)
6. ❌ Don't use LiveData in new code (prefer StateFlow/Flow)
7. ❌ Don't let exceptions propagate raw from repository (use Result wrapper)
8. ❌ Don't use magic numbers/strings (create constants)
9. ❌ Don't leave commented code (use version control)
10. ❌ Don't use `viewModelScope.launch` without error handling
11. ❌ Don't make database calls on main thread
12. ❌ Don't skip database migrations (post-release)
13. ❌ Don't put business logic in Composables (belongs in ViewModel/UseCase)
14. ❌ Don't use GlobalScope (use structured concurrency)
15. ❌ Don't collect Flow in Composable body (use side effects)

---

## Git Workflow

### Commit Messages
- Use conventional commits: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`
- Be descriptive but concise
- Reference issues when applicable

### Branch Strategy
- `master` - production-ready code
- `develop` - integration branch
- `feature/*` - feature branches
- `fix/*` - bug fixes
- `refactor/*` - code improvements

---

## Release Checklist

Before publishing to GitHub:

- [ ] All tests passing
- [ ] No commented code
- [ ] Comprehensive README with screenshots
- [ ] LICENSE file added (MIT recommended)
- [ ] .gitignore properly configured
- [ ] No hardcoded secrets/API keys
- [ ] Privacy policy statement
- [ ] Architecture documentation
- [ ] Code coverage >70%
- [ ] ProGuard/R8 rules configured
- [ ] App icons and branding complete
- [ ] Version code/name updated
- [ ] Release notes prepared

---

## References

### Official Google Documentation
- **Modern Android Development (MAD)**: [Overview](https://developer.android.com/modern-android-development)
- **Architecture**: [Guide to App Architecture](https://developer.android.com/topic/architecture)
- **Architecture Recommendations**: [Best Practices](https://developer.android.com/topic/architecture/recommendations)
- **Kotlin Style Guide**: [Official Guide](https://developer.android.com/kotlin/style-guide)
- **Jetpack Compose**: [Architecture](https://developer.android.com/jetpack/compose/architecture)
- **ViewModel**: [Overview & Best Practices](https://developer.android.com/topic/libraries/architecture/viewmodel)
- **StateFlow & SharedFlow**: [Documentation](https://developer.android.com/kotlin/flow/stateflow-and-sharedflow)
- **Room Database**: [Guide](https://developer.android.com/training/data-storage/room)
- **Dependency Injection**: [Manual DI Guide](https://developer.android.com/training/dependency-injection/manual)
- **Material Design 3**: [Guidelines](https://m3.material.io/)
- **Testing**: [Android Testing Guide](https://developer.android.com/training/testing)

### Project-Specific
- **Target Standard**: [My-Bookshelf](https://github.com/Zlurgg/My-Bookshelf)
- **Google's Now in Android**: [Sample App](https://github.com/android/nowinandroid) - Reference architecture

---

## Notes

This is a living document. Update as the project evolves and new patterns emerge.

Last Updated: 2025-10-22