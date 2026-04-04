# Plan: Unified Date/Time Handling (TimeProvider)

## Summary

Introduce a `TimeProvider` abstraction for time access (testable) and pure extension functions for storage conversion (stateless math). This separates concerns properly and eliminates scattered timezone handling.

**v2 Changes:** Addressed staff engineer review - split time access from storage conversion, fixed FakeDateTimeProvider bug, moved formatting to UI layer, added atomic convenience methods.

---

## Current State Analysis

### Files Affected
| File | Current Pattern | Issues |
|------|-----------------|--------|
| `DateUtils.kt` | `LocalDate.now()`, `ZoneOffset.UTC` | Direct system calls, untestable |
| `DateConvertors.kt` | `ZoneId.systemDefault()` | Different timezone than storage |
| `CalendarUtils.kt` | `ZoneOffset.UTC` throughout | Consistent, but scattered |
| `CheckTodayEntryUseCase.kt` | `DateUtils.getTodayStartEpoch()` | Indirect system call |
| `EditorViewModel.kt` | `DateUtils.getTodayStartEpoch()` | Indirect system call |
| `OverviewViewModel.kt` | `DateUtils.getTodayStartEpoch()` | Indirect system call |
| `NotificationRepositoryImpl.kt` | `ZoneId.systemDefault()` | System timezone for scheduling |
| `EntryRepositoryImpl.kt` | Epoch math with UTC | Correct, but verbose |

### Core Problem
The app stores dates as UTC epoch seconds but displays using system timezone. This works correctly **as long as the user doesn't change timezones**. The real question is: what should a journal entry's date represent?

**Decision (from prior discussion):** A journal entry's date represents "the day I experienced" in the user's local time. Entries should NOT shift when viewed from a different timezone. "Today" is always the local date.

---

## Design

### Separation of Concerns

| Concern | Solution | Layer | Needs Faking? |
|---------|----------|-------|---------------|
| Current time access | `TimeProvider` interface | Domain | Yes |
| Storage format conversion | Extension functions | Data | No (pure math) |
| Date formatting for display | `DateFormatter` class | UI | No (uses TimeProvider) |
| Calendar comparisons | `CalendarHelper` class | UI | No (uses TimeProvider) |

### TimeProvider Interface (Domain Layer)

```kotlin
// core/domain/util/TimeProvider.kt
interface TimeProvider {
    /** Current local date (user's timezone) */
    fun today(): LocalDate

    /** Current local time (user's timezone) */
    fun now(): LocalDateTime

    /** Current instant (absolute point in time) */
    fun instant(): Instant

    /**
     * Atomic capture of today's date as storage epoch.
     * Prevents midnight boundary races between today() and conversion.
     */
    fun todayStorageEpoch(): Long
}
```

**Note:** `zone()` removed - it was exposed but unused in conversions. If needed later for notification scheduling, it can be added to a separate `TimezoneProvider` interface.

### Storage Conversion (Data Layer - Pure Functions)

```kotlin
// core/data/util/DateStorageExt.kt

/**
 * Converts LocalDate to storage epoch (UTC midnight).
 * This is the canonical storage format for journal entry dates.
 *
 * Pure function - no timezone dependency, deterministic output.
 */
fun LocalDate.toStorageEpoch(): Long =
    atStartOfDay().toEpochSecond(ZoneOffset.UTC)

/**
 * Converts storage epoch back to LocalDate.
 * Assumes epoch represents UTC midnight (matches toStorageEpoch format).
 *
 * Pure function - no timezone dependency, deterministic output.
 */
fun Long.toLocalDate(): LocalDate =
    Instant.ofEpochSecond(this)
        .atOffset(ZoneOffset.UTC)
        .toLocalDate()
```

**Why extension functions?**
- Pure math, no state needed
- No injection required
- Tests don't need to fake conversions
- Simpler call sites: `date.toStorageEpoch()` vs `provider.toStorageEpoch(date)`

### SystemTimeProvider Implementation (Data Layer)

```kotlin
// core/data/util/SystemTimeProvider.kt
class SystemTimeProvider : TimeProvider {

    override fun today(): LocalDate = LocalDate.now()

    override fun now(): LocalDateTime = LocalDateTime.now()

    override fun instant(): Instant = Instant.now()

    override fun todayStorageEpoch(): Long = today().toStorageEpoch()
}
```

### FakeTimeProvider (Test)

```kotlin
// test/.../testutil/FakeTimeProvider.kt

/**
 * Test fake for TimeProvider.
 *
 * NOT thread-safe - intended for single-threaded test execution.
 * For parallel tests, create separate instances per test.
 */
class FakeTimeProvider(
    private var fixedDate: LocalDate = LocalDate.of(2024, 1, 15),
    private var fixedTime: LocalTime = LocalTime.of(10, 30),
    private var fixedZone: ZoneId = ZoneId.of("UTC")
) : TimeProvider {

    override fun today(): LocalDate = fixedDate

    override fun now(): LocalDateTime = LocalDateTime.of(fixedDate, fixedTime)

    override fun instant(): Instant = now().atZone(fixedZone).toInstant()

    override fun todayStorageEpoch(): Long = fixedDate.toStorageEpoch()

    // Test helpers
    fun advanceDays(days: Int) {
        fixedDate = fixedDate.plusDays(days.toLong())
    }

    fun setDate(date: LocalDate) {
        fixedDate = date
    }

    fun setTime(time: LocalTime) {
        fixedTime = time
    }

    fun setZone(zone: ZoneId) {
        fixedZone = zone
    }
}
```

### DateFormatter (UI Layer)

```kotlin
// journal/ui/util/DateFormatter.kt

/**
 * Stateless date formatting utilities.
 * Uses pure extension functions - no dependencies needed.
 */
object DateFormatter {

    fun formatDate(epochSeconds: Long): String {
        val date = epochSeconds.toLocalDate()
        return date.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
    }

    fun formatDay(epochSeconds: Long): Int {
        return epochSeconds.toLocalDate().dayOfMonth
    }

    fun formatMonth(epochSeconds: Long): String {
        return epochSeconds.toLocalDate().month.getDisplayName(
            TextStyle.FULL,
            Locale.getDefault()
        )
    }

    fun formatMonthValue(epochSeconds: Long): Int {
        return epochSeconds.toLocalDate().monthValue
    }

    fun formatYear(epochSeconds: Long): Int {
        return epochSeconds.toLocalDate().year
    }
}
```

### CalendarHelper (UI Layer)

```kotlin
// journal/ui/util/CalendarHelper.kt
class CalendarHelper(private val timeProvider: TimeProvider) {

    fun isToday(epochSeconds: Long): Boolean {
        return epochSeconds.toLocalDate() == timeProvider.today()
    }

    fun isPast(epochSeconds: Long): Boolean {
        return epochSeconds.toLocalDate() < timeProvider.today()
    }

    fun isFuture(epochSeconds: Long): Boolean {
        return epochSeconds.toLocalDate() > timeProvider.today()
    }

    fun isInMonth(epochSeconds: Long, year: Int, month: Int): Boolean {
        val date = epochSeconds.toLocalDate()
        return date.year == year && date.monthValue == month
    }
}
```

---

## Migration Strategy

### Phase 1: Add TimeProvider + Extensions (Non-Breaking)

1. Create `TimeProvider` interface in `core/domain/util/`
2. Create `SystemTimeProvider` implementation in `core/data/util/`
3. Create `DateStorageExt.kt` extension functions in `core/data/util/`
4. Create `FakeTimeProvider` in `test/.../testutil/`
5. Register in `CoreModule.kt` as singleton
6. **No existing code changes yet**

### Phase 2: Migrate DateUtils.kt

```kotlin
// Before
object DateUtils {
    fun getTodayStartEpoch(): Long {
        return LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
    }
}

// After - deprecate and delegate
object DateUtils {
    @Deprecated(
        message = "Use TimeProvider.todayStorageEpoch()",
        replaceWith = ReplaceWith("timeProvider.todayStorageEpoch()")
    )
    fun getTodayStartEpoch(): Long {
        return LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)
    }
}
```

### Phase 3: Migrate UseCases and ViewModels

Inject `TimeProvider` and replace `DateUtils` calls:

```kotlin
// Before
class CheckTodayEntryUseCase(private val entryRepository: EntryRepository) {
    suspend operator fun invoke(): Boolean {
        val todayEpoch = DateUtils.getTodayStartEpoch()
        return entryRepository.getEntryByDateStamp(todayEpoch) != null
    }
}

// After
class CheckTodayEntryUseCase(
    private val entryRepository: EntryRepository,
    private val timeProvider: TimeProvider
) {
    suspend operator fun invoke(): Boolean {
        return entryRepository.getEntryByDateStamp(timeProvider.todayStorageEpoch()) != null
    }
}
```

### Phase 4: Create UI Helpers

1. Create `DateFormatter` object in `journal/ui/util/` (stateless, no DI needed)
2. Create `CalendarHelper` class in `journal/ui/util/`
3. Register `CalendarHelper` in `JournalModule.kt` (needs TimeProvider)
4. Migrate `DateConvertors.kt` functions to `DateFormatter`
5. Migrate `CalendarUtils.kt` functions to `CalendarHelper`

### Phase 5: Deprecate Old Utilities

```kotlin
// DateConvertors.kt - deprecate all functions
@Deprecated("Use DateFormatter.formatDate()")
fun Long.datestampToFormattedDate(): String = ...

// CalendarUtils.kt - deprecate all functions
@Deprecated("Use CalendarHelper.isToday()")
fun isToday(epochSeconds: Long): Boolean = ...
```

### Phase 6: Cleanup

1. Remove deprecated functions from `DateConvertors.kt` and `CalendarUtils.kt`
2. Delete `DateUtils.kt`
3. Update any remaining direct `LocalDate.now()` calls

---

## Files Modified/Created

### New Files
- `app/src/main/java/uk/co/zlurgg/thedayto/core/domain/util/TimeProvider.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/core/data/util/SystemTimeProvider.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/core/data/util/DateStorageExt.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/util/DateFormatter.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/util/CalendarHelper.kt`
- `app/src/test/java/uk/co/zlurgg/thedayto/testutil/FakeTimeProvider.kt`

### Modified Files
- `app/src/main/java/uk/co/zlurgg/thedayto/core/di/CoreModule.kt` (add TimeProvider)
- `app/src/main/java/uk/co/zlurgg/thedayto/journal/di/JournalModule.kt` (add CalendarHelper - DateFormatter is object, no DI)
- `app/src/main/java/uk/co/zlurgg/thedayto/auth/domain/usecases/CheckTodayEntryUseCase.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/editor/EditorViewModel.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/overview/OverviewViewModel.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/journal/data/repository/EntryRepositoryImpl.kt`
- Various UI components using DateConvertors/CalendarUtils

### Deleted Files (Phase 6)
- `app/src/main/java/uk/co/zlurgg/thedayto/core/domain/util/DateUtils.kt`
- `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/util/DateConvertors.kt` (contents migrated)
- `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/overview/util/CalendarUtils.kt` (contents migrated)

---

## Edge Cases Addressed

### Midnight Boundary Race (Fixed)

**Problem:** Calling `today()` then `toStorageEpoch()` separately risks date shift at midnight.

**Solution:** `todayStorageEpoch()` atomically captures both:

```kotlin
override fun todayStorageEpoch(): Long = today().toStorageEpoch()
```

Single call = single point in time = no race.

### FakeTimeProvider.instant() Timezone Bug (Fixed)

**Problem:** Original hardcoded UTC regardless of `fixedZone`.

**Solution:**
```kotlin
// Before (broken)
override fun instant(): Instant = now().toInstant(ZoneOffset.UTC)

// After (respects fixedZone)
override fun instant(): Instant = now().atZone(fixedZone).toInstant()
```

### Thread Safety (Documented)

`FakeTimeProvider` is not thread-safe. Documented in class KDoc:

```kotlin
/**
 * NOT thread-safe - intended for single-threaded test execution.
 * For parallel tests, create separate instances per test.
 */
```

---

## Edge Cases Not Addressed

1. **Historical timezone changes**: If a country changes its timezone rules, old entries won't retroactively adjust. Acceptable for journal app.

2. **Sub-day precision**: Entries only store date, not time. Two entries at 11:59 PM and 12:01 AM are separate days.

3. **Device clock manipulation**: Entries use device time, no server validation.

4. **International Date Line crossing**: User might experience "two Mondays" when flying. Each entry is local date when created.

---

## Assumptions That Might Not Hold

| Assumption | Risk | Mitigation |
|------------|------|------------|
| User's device clock is accurate | Low | None needed for journal app |
| `LocalDate.now()` is reliable | Very low | Standard Android API |
| UTC epoch is stable storage | Very low | Unix standard since 1970 |
| Users don't frequently change timezones | Medium | Document behavior, accept edge case |
| Tests are single-threaded | Medium | Document FakeTimeProvider limitation |

---

## Simpler Alternatives Considered

### Alternative 1: Keep Everything in One Interface
**Rejected.** Storage conversion is pure math - wrapping in interface is over-engineering.

### Alternative 2: Just Use java.time.Clock
**Partially adopted.** Could use `Clock` internally in `SystemTimeProvider`, but domain interface is more expressive for our use case.

### Alternative 3: Do Nothing
**Rejected.** Testability alone justifies the change.

---

## Clean Architecture Compliance

### Dependency Rule
- `TimeProvider` interface in `domain/util/` (abstraction)
- `SystemTimeProvider` implementation in `data/util/`
- Domain depends on abstraction, not concrete

### Single Responsibility
| Class | Single Responsibility |
|-------|----------------------|
| `TimeProvider` | Current time access |
| `DateStorageExt` | Storage format conversion (stateless) |
| `DateFormatter` | Date display formatting |
| `CalendarHelper` | Calendar comparison logic |

### Interface Segregation
- `TimeProvider` is minimal: 4 methods, all needed
- No "fat interface" with unused methods
- `zone()` removed since it was unused

### Layer Placement
| Component | Layer | Justification |
|-----------|-------|---------------|
| `TimeProvider` | Domain | Abstract time access, used by UseCases |
| `SystemTimeProvider` | Data | Concrete implementation |
| `DateStorageExt` | Data | Storage format knowledge |
| `DateFormatter` | UI | Presentation/display concern |
| `CalendarHelper` | UI | Calendar UI logic |

---

## DRY Analysis

### Before (Repetition)
```kotlin
// In DateUtils.kt
LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)

// In CalendarUtils.kt
LocalDate.now().atStartOfDay().toEpochSecond(ZoneOffset.UTC)

// In EditorViewModel.kt
DateUtils.getTodayStartEpoch()

// In CheckTodayEntryUseCase.kt
DateUtils.getTodayStartEpoch()
```

### After (Single Source)
```kotlin
// All callers use:
timeProvider.todayStorageEpoch()

// Conversion defined once:
fun LocalDate.toStorageEpoch(): Long = atStartOfDay().toEpochSecond(ZoneOffset.UTC)
```

**Improvement:**
- 4+ duplicated conversion patterns → 1 extension function
- Verbose `toStorageEpoch(today())` → atomic `todayStorageEpoch()`

---

## Testing Strategy

### Pure Function Tests (No Fakes Needed)

```kotlin
class DateStorageExtTest {
    @Test
    fun `toStorageEpoch converts LocalDate to UTC midnight epoch`() {
        val date = LocalDate.of(2024, 1, 1)
        assertEquals(1704067200L, date.toStorageEpoch())
    }

    @Test
    fun `toLocalDate converts epoch back to LocalDate`() {
        assertEquals(
            LocalDate.of(2024, 1, 1),
            1704067200L.toLocalDate()
        )
    }

    @Test
    fun `roundtrip preserves date`() {
        val original = LocalDate.of(2024, 6, 15)
        assertEquals(original, original.toStorageEpoch().toLocalDate())
    }
}
```

### TimeProvider Tests (Use Fake)

```kotlin
class CheckTodayEntryUseCaseTest {
    @Test
    fun `returns true when entry exists for today`() = runTest {
        val fakeTime = FakeTimeProvider(fixedDate = LocalDate.of(2024, 6, 15))
        val fakeRepo = FakeEntryRepository()
        fakeRepo.addEntry(Entry(dateStamp = fakeTime.todayStorageEpoch()))

        val useCase = CheckTodayEntryUseCase(fakeRepo, fakeTime)

        assertTrue(useCase())
    }
}
```

---

## Implementation Order

```
Phase 1: Add TimeProvider + Extensions    [~45 min]
    ↓
Phase 2: Migrate DateUtils.kt             [~15 min]
    ↓
Phase 3: Migrate UseCases/ViewModels      [~1 hour]
    ↓
Phase 4: Create UI Helpers                [~45 min]
    ↓
Phase 5: Deprecate Old Utilities          [~15 min]
    ↓
Phase 6: Cleanup & Delete                 [~15 min]
```

---

## Verification Checklist

- [ ] All existing tests pass after migration
- [ ] TimeProvider injected in all time-dependent classes
- [ ] No direct `LocalDate.now()` calls outside SystemTimeProvider
- [ ] No direct `ZoneOffset.UTC` conversion logic outside DateStorageExt
- [ ] FakeTimeProvider used in all time-dependent tests
- [ ] DateFormatter and CalendarHelper in UI layer
- [ ] DateUtils.kt deleted
- [ ] Old DateConvertors/CalendarUtils deprecated or removed

---

## Dependency on Notification Refactor

This plan should be completed **BEFORE** the notification refactor because:

1. Notification worker needs consistent "today" calculation via `TimeProvider`
2. TimezoneChangeReceiver should use `TimeProvider.todayStorageEpoch()`

**Note on timezone for notifications:**
`NotificationRepositoryImpl` uses `ZoneId.systemDefault()` for scheduling - this is correct and intentional. Notification scheduling must use the actual device timezone to fire at the right local time. This is not abstracted because:
- It's a system-level concern, not domain logic
- Faking it in tests could mask real scheduling bugs
- The `TimezoneChangeReceiver` handles timezone changes by rescheduling

If notification scheduling tests need timezone control, a separate `TimezoneProvider` can be added to the notification module specifically. That's out of scope for this plan.
