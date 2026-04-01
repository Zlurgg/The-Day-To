# UseCase Pattern

Patterns for implementing business logic through UseCases.

## UseCase Pattern

Concrete classes with `operator fun invoke()` for callable syntax:

```kotlin
class GetEntryByIdUseCase(
    private val repository: EntryRepository
) {
    suspend operator fun invoke(id: Int): Result<Entry?, DataError.Local> {
        return repository.getEntryById(id)
    }
}

// Usage: useCase(id) instead of useCase.execute(id)
```

### Guidelines

- Naming: `VerbNoun` + `UseCase` (e.g., `GetEntryUseCase`, `SaveMoodColorUseCase`)
- One UseCase per business operation
- Can depend on repositories, domain services, or other UseCases
- Must be main-safe; use `withContext(dispatcher)` for background work
- Inject dependencies for testability rather than using static calls

## UseCases Aggregator Pattern

When a ViewModel requires multiple UseCases, group them in a data class:

```kotlin
// In domain/usecase/
data class JournalUseCases(
    val getEntries: GetEntriesUseCase,
    val getEntryById: GetEntryByIdUseCase,
    val saveEntry: SaveEntryUseCase,
    val deleteEntry: DeleteEntryUseCase,
    val getMoodColors: GetMoodColorsUseCase
)

data class AuthUseCases(
    val signIn: SignInUseCase,
    val signOut: SignOutUseCase,
    val getCurrentUser: GetCurrentUserUseCase
)

// ViewModel constructor
class CalendarViewModel(
    private val journalUseCases: JournalUseCases
) : ViewModel() {
    // Usage: journalUseCases.getEntries(), journalUseCases.saveEntry(entry)
}
```

### DI Registration (Koin)

```kotlin
// In JournalModule.kt
factory { JournalUseCases(get(), get(), get(), get(), get()) }

// Or with named parameters for clarity
factory {
    JournalUseCases(
        getEntries = get(),
        getEntryById = get(),
        saveEntry = get(),
        deleteEntry = get(),
        getMoodColors = get()
    )
}
```

## Complex UseCase Example

UseCases can orchestrate multiple repositories and contain business logic:

```kotlin
class SaveEntryUseCase(
    private val entryRepository: EntryRepository,
    private val moodColorRepository: MoodColorRepository
) {
    suspend operator fun invoke(entry: Entry): Result<Unit, DataError.Local> {
        // Business validation
        if (entry.mood.isBlank()) {
            return Result.Error(DataError.Local.ValidationError)
        }

        // Check mood color exists
        val moodColor = moodColorRepository.getMoodColorByName(entry.mood)
        if (moodColor is Result.Error) {
            return Result.Error(DataError.Local.NotFound)
        }

        // Save entry
        return entryRepository.saveEntry(entry)
    }
}
```

## UseCase with Flow

For reactive data, UseCases can return Flow:

```kotlin
class ObserveEntriesUseCase(
    private val repository: EntryRepository
) {
    operator fun invoke(): Flow<List<Entry>> {
        return repository.observeEntries()
    }
}

// In ViewModel
init {
    viewModelScope.launch {
        journalUseCases.observeEntries()
            .collect { entries ->
                _state.update { it.copy(entries = entries) }
            }
    }
}
```

## Testing UseCases

```kotlin
class GetEntryByIdUseCaseTest {

    private lateinit var useCase: GetEntryByIdUseCase
    private lateinit var fakeRepository: FakeEntryRepository

    @Before
    fun setup() {
        fakeRepository = FakeEntryRepository()
        useCase = GetEntryByIdUseCase(fakeRepository)
    }

    @Test
    fun `invoke - when entry exists - returns entry`() = runTest {
        val entry = Entry(id = 1, mood = "Happy", date = LocalDate.now())
        fakeRepository.entries.value = listOf(entry)

        val result = useCase(1)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isEqualTo(entry)
    }

    @Test
    fun `invoke - when entry not found - returns null`() = runTest {
        fakeRepository.entries.value = emptyList()

        val result = useCase(999)

        assertThat(result).isInstanceOf(Result.Success::class.java)
        assertThat((result as Result.Success).data).isNull()
    }
}
```
