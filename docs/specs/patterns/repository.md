# Repository Pattern

Patterns for implementing data access through repositories.

## Interface in Domain

Repository interfaces live in the domain layer and define the contract:

```kotlin
// In journal/domain/repository/
interface EntryRepository {
    suspend fun getEntries(): Result<List<Entry>, DataError.Local>
    suspend fun getEntryById(id: Int): Result<Entry?, DataError.Local>
    suspend fun getEntryByDate(date: LocalDate): Result<Entry?, DataError.Local>
    suspend fun saveEntry(entry: Entry): Result<Unit, DataError.Local>
    suspend fun deleteEntry(id: Int): Result<Unit, DataError.Local>
    fun observeEntries(): Flow<List<Entry>>
}
```

### Guidelines

- Interfaces in `domain/repository/`
- Use domain models, not entities
- Return `Result<T, DataError>` for fallible operations
- Use `Flow<T>` for reactive/observable data
- Keep methods focused (SRP)

## Implementation in Data

Repository implementations live in the data layer:

```kotlin
// In journal/data/repository/
class EntryRepositoryImpl(
    private val dao: EntryDao
) : EntryRepository {

    override suspend fun getEntries(): Result<List<Entry>, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getAll().map { it.toDomain() }
        }
    }

    override suspend fun getEntryById(id: Int): Result<Entry?, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.getById(id)?.toDomain()
        }
    }

    override suspend fun saveEntry(entry: Entry): Result<Unit, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.upsert(entry.toEntity())
        }
    }

    override suspend fun deleteEntry(id: Int): Result<Unit, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.deleteById(id)
        }
    }

    override fun observeEntries(): Flow<List<Entry>> {
        return dao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    companion object {
        private const val TAG = "EntryRepository"
    }
}
```

## Error Handling with ErrorMapper

Use `ErrorMapper.safeSuspendCall()` to wrap database operations:

```kotlin
object ErrorMapper {
    suspend fun <T> safeSuspendCall(
        tag: String,
        block: suspend () -> T
    ): Result<T, DataError.Local> {
        return try {
            Result.Success(block())
        } catch (e: CancellationException) {
            throw e  // Never catch cancellation
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Database operation failed")
            Result.Error(mapExceptionToDataError(e))
        }
    }

    fun mapExceptionToDataError(e: Exception): DataError.Local {
        return when (e) {
            is SQLiteException -> DataError.Local.DatabaseError
            else -> DataError.Local.Unknown(e.message ?: "Unknown error")
        }
    }
}
```

## Entity to Domain Mapping

Keep mappers as extension functions on entities:

```kotlin
// In journal/data/mapper/EntryMapper.kt
fun EntryEntity.toDomain(): Entry {
    return Entry(
        id = id,
        date = LocalDate.ofEpochDay(dateEpochDay),
        mood = mood,
        note = note
    )
}

fun Entry.toEntity(): EntryEntity {
    return EntryEntity(
        id = id,
        dateEpochDay = date.toEpochDay(),
        mood = mood,
        note = note
    )
}
```

## Flow-based Reactive Data

For data that changes over time, expose Flow:

```kotlin
interface MoodColorRepository {
    fun observeMoodColors(): Flow<List<MoodColor>>
    suspend fun saveMoodColor(moodColor: MoodColor): Result<Unit, DataError.Local>
}

class MoodColorRepositoryImpl(
    private val dao: MoodColorDao
) : MoodColorRepository {

    override fun observeMoodColors(): Flow<List<MoodColor>> {
        return dao.observeAll()
            .map { entities -> entities.map { it.toDomain() } }
            .distinctUntilChanged()
    }

    override suspend fun saveMoodColor(moodColor: MoodColor): Result<Unit, DataError.Local> {
        return ErrorMapper.safeSuspendCall(TAG) {
            dao.upsert(moodColor.toEntity())
        }
    }
}
```

## DI Registration (Koin)

```kotlin
// In JournalModule.kt
val journalModule = module {
    // DAOs
    single { get<TheDayToDatabase>().entryDao() }
    single { get<TheDayToDatabase>().moodColorDao() }

    // Repositories
    single<EntryRepository> { EntryRepositoryImpl(get()) }
    single<MoodColorRepository> { MoodColorRepositoryImpl(get()) }
}
```

## Testing with Fakes

Create fake implementations for testing:

```kotlin
// In test/.../fake/FakeEntryRepository.kt
class FakeEntryRepository : EntryRepository {

    val entries = MutableStateFlow<List<Entry>>(emptyList())
    var shouldReturnError = false

    override suspend fun getEntries(): Result<List<Entry>, DataError.Local> {
        return if (shouldReturnError) {
            Result.Error(DataError.Local.DatabaseError)
        } else {
            Result.Success(entries.value)
        }
    }

    override suspend fun getEntryById(id: Int): Result<Entry?, DataError.Local> {
        return if (shouldReturnError) {
            Result.Error(DataError.Local.DatabaseError)
        } else {
            Result.Success(entries.value.find { it.id == id })
        }
    }

    override suspend fun saveEntry(entry: Entry): Result<Unit, DataError.Local> {
        if (shouldReturnError) {
            return Result.Error(DataError.Local.DatabaseError)
        }
        entries.update { current ->
            val existing = current.indexOfFirst { it.id == entry.id }
            if (existing >= 0) {
                current.toMutableList().apply { set(existing, entry) }
            } else {
                current + entry
            }
        }
        return Result.Success(Unit)
    }

    override suspend fun deleteEntry(id: Int): Result<Unit, DataError.Local> {
        if (shouldReturnError) {
            return Result.Error(DataError.Local.DatabaseError)
        }
        entries.update { it.filter { entry -> entry.id != id } }
        return Result.Success(Unit)
    }

    override fun observeEntries(): Flow<List<Entry>> = entries
}
```

### Key Fake Guidelines

- Use `MutableStateFlow` for reactive behavior
- Add `shouldReturnError` flag for error testing
- Keep in sync with interface changes
- Share fakes across test classes
