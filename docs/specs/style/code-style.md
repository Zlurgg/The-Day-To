# Code Style & Conventions

Code style rules, naming conventions, and testing standards.

## Naming Conventions

| Type | Pattern | Example |
|------|---------|---------|
| UseCase | `VerbNounUseCase` | `GetEntryUseCase`, `SaveMoodColorUseCase` |
| UseCases Aggregator | `FeatureUseCases` | `JournalUseCases`, `AuthUseCases` |
| ViewModel | `FeatureViewModel` | `CalendarViewModel`, `SignInViewModel` |
| Repository Interface | `FeatureRepository` | `EntryRepository`, `MoodColorRepository` |
| Repository Impl | `FeatureRepositoryImpl` | `EntryRepositoryImpl` |
| State | `FeatureState` | `CalendarState`, `EditorState` |
| Action | `FeatureAction` | `CalendarAction`, `EditorAction` |
| Screen (Root) | `FeatureScreenRoot` | `CalendarScreenRoot` |
| Screen (Pure UI) | `FeatureScreen` | `CalendarScreen` |
| Room Entity | `FeatureEntity` | `EntryEntity`, `MoodColorEntity` |
| Room DAO | `FeatureDao` | `EntryDao`, `MoodColorDao` |
| Koin Module | `FeatureModule` | `JournalModule`, `AuthModule` |

## Logging Pattern

Use Timber with TAG constants and varargs formatting (not string interpolation):

```kotlin
class CalendarViewModel(...) : ViewModel() {

    private fun loadEntries() {
        Timber.tag(TAG).d("Loading entries for month: %s", currentMonth)
        // NOT: Timber.d("Loading entries for month: $currentMonth")
    }

    companion object {
        private const val TAG = "CalendarViewModel"
    }
}
```

### Guidelines

- Always define `private const val TAG` in companion object
- Use `Timber.tag(TAG).d/w/e(...)` for all log calls
- Use format specifiers (`%s`, `%d`) instead of string interpolation
- This enables log filtering by class and improves performance

### When to Log

| Level | When | Example |
|-------|------|---------|
| `d` (debug) | Development info, operation start | `"Starting sync"` |
| `w` (warning) | Recoverable issues | `"Retry attempt %d"` |
| `e` (error) | Failures with exception | `"Database error", exception` |

**DO:** Log errors, critical operations, use varargs formatting, use tags
**DON'T:** Log happy-path, PII, use string interpolation, log in loops

## Detekt Rules

All code must pass detekt checks. Run `./gradlew detekt` before committing.

| Rule | Limit | How to Fix |
|------|-------|------------|
| **LongParameterList** | Max 6 parameters | Wrap related params in a data class |
| **LongMethod** | Max 60 lines | Extract helper functions |
| **ReturnCount** | Max 3 (guards excluded) | Use `when` or extract helpers |
| **MaxLineLength** | 120 characters | Wrap long lines |
| **MagicNumber** | Named constants only | Extract to companion object |

## Reducing Parameter Count

When a function has more than 6 parameters, create wrapper data classes:

```kotlin
// BAD: 8 parameters
@Composable
fun EntryCard(
    entry: Entry,
    moodColor: MoodColor,
    isSelected: Boolean,
    onEntryClick: (Entry) -> Unit,
    onEditClick: (Entry) -> Unit,
    onDeleteClick: (Entry) -> Unit,
    modifier: Modifier
)

// GOOD: Grouped into state + actions
data class EntryCardState(
    val isSelected: Boolean
)

data class EntryCardActions(
    val onEntryClick: (Entry) -> Unit,
    val onEditClick: (Entry) -> Unit,
    val onDeleteClick: (Entry) -> Unit
)

@Composable
fun EntryCard(
    entry: Entry,
    moodColor: MoodColor,
    state: EntryCardState,
    actions: EntryCardActions,
    modifier: Modifier
)
```

## Import Ordering

Imports must be sorted lexicographically with aliases at the end:

```kotlin
// CORRECT order:
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import uk.co.zlurgg.thedayto.journal.domain.model.MoodColor
import uk.co.zlurgg.thedayto.journal.ui.calendar.CalendarState
import java.time.LocalDate as Date  // aliases at END

// WRONG - alias in middle:
import uk.co.zlurgg.thedayto.journal.domain.model.Entry
import java.time.LocalDate as Date  // ERROR: alias not at end
import uk.co.zlurgg.thedayto.journal.ui.calendar.CalendarState
```

## Testing Standards

### Test Naming

```kotlin
// Pattern: `action - condition - expected result`
@Test
fun `getEntry - when entry exists - returns entry`()

@Test
fun `saveEntry - when database error - returns DatabaseError`()
```

### Test File Locations

| Test Type | Location | Purpose |
|-----------|----------|---------|
| Unit tests | `app/src/test/.../` | ViewModel, UseCase, pure logic |
| Integration tests | `app/src/androidTest/.../` | Room, real dependencies |
| Fake implementations | `app/src/test/.../fake/` | Shared test doubles |

### Test Organization

```
app/src/test/java/uk/co/zlurgg/thedayto/
├── journal/
│   ├── domain/usecase/           # UseCase tests
│   ├── ui/calendar/              # ViewModel tests
│   └── fake/                     # Feature-specific fakes
├── auth/
│   ├── domain/usecase/
│   └── ui/
└── core/
    └── fake/                     # Shared fakes
```

### Shared Fake Implementations

Use shared fakes to avoid duplication:

| Fake | Location | Purpose |
|------|----------|---------|
| `FakeEntryRepository` | `journal/fake/` | Entry tests |
| `FakeMoodColorRepository` | `journal/fake/` | MoodColor tests |
| `FakeAuthRepository` | `auth/fake/` | Auth tests |

### Test Class Guidelines

- Keep test classes focused (<300 lines)
- Use `@Before` for shared setup
- One assertion concept per test
- Use Turbine for Flow testing
- Use `advanceUntilIdle()` after coroutine actions

```kotlin
@Test
fun `loadEntries - emits loading then success`() = runTest {
    viewModel.state.test {
        assertThat(awaitItem().isLoading).isFalse()

        viewModel.onAction(CalendarAction.LoadEntries)

        assertThat(awaitItem().isLoading).isTrue()
        assertThat(awaitItem().entries).isNotEmpty()
    }
}
```

## Commit Style

Conventional commits with scope:

```
type(scope): description

feat(journal): add mood color picker
fix(auth): handle null user on sign out
refactor(core): extract error mapping to ErrorMapper
test(calendar): add ViewModel state tests
docs: update architecture documentation
build: migrate to AGP 9.1
```

Types: `feat`, `fix`, `refactor`, `test`, `docs`, `build`, `chore`
