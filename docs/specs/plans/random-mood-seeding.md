# Random Mood Seeding Feature

## Problem

Users who don't want to think about naming moods settle for the 7 defaults and never expand their palette. The journal data quality suffers because the same 7 words can't capture the nuance of daily emotional life. People are lazy — they need a low-friction way to discover moods they didn't know they'd use.

## Solution

Add a dice button to the Mood Color Management screen that seeds 5-10 random mood+color pairs from a curated list, skipping any that already exist.

---

## UX Flow

1. User opens Mood Color Management screen
2. Dice icon visible in TopAppBar actions (right side, before the "X / 50" counter if shown)
3. User taps dice icon
4. Confirmation dialog appears:
   - Centered dice icon (enlarged)
   - Text: "Add random mood colors?"
   - Subtext: "A handful of new moods will be added to your palette"
   - Confirm button + Cancel button
5. On confirm:
   - Dialog closes
   - Dice button disabled while seeding (concurrency guard via `isSeedingInProgress`)
   - ViewModel picks 5-10 random moods from the curated pool, skipping existing names
   - Each is saved via `SaveMoodColorUseCase` (gets validation, limit enforcement, sync for free)
   - Room flow re-emits; list updates automatically — the new moods animating in
     via `animateItem()` is the visual feedback
   - Dice button re-enables when seeding completes
6. If the 50-cap would be hit: saves as many as fit, stops at `LimitReached`
7. If ALL curated moods already exist: nothing happens (dice roll was empty), no error shown

---

## Curated Mood Pool

### Design Principles

- **Color psychology grounded** — each color should feel like it "fits" the mood
- **No overlap with defaults** — the 7 seeds (Happy, Sad, In Love, Calm, Excited, Anxious, Grateful) are excluded
- **Diverse emotional range** — positive, negative, neutral, social, reflective, energetic, and subtle emotions
- **Short names** — 1-2 words max for comfortable dropdown display
- **No duplicate hex codes** — every mood has a unique color so the calendar stays legible
- **~58 moods** — enough for 5+ dice taps before most are duplicates

### Data Structure

```kotlin
// journal/domain/model/CuratedMoods.kt

object CuratedMoods {
    data class Seed(val mood: String, val color: String)

    val ALL: List<Seed> = listOf(
        // ---- Positive / Warm ----
        Seed("Joyful", "FFD54F"),         // Warm gold
        Seed("Content", "A5D6A7"),        // Soft sage
        Seed("Hopeful", "FFF176"),        // Pale yellow
        Seed("Playful", "FF8A65"),        // Coral
        Seed("Proud", "CE93D8"),          // Soft purple
        Seed("Cheerful", "FFB74D"),       // Light orange
        Seed("Amused", "FFD740"),         // Bright amber
        Seed("Inspired", "7E57C2"),       // Deep violet
        Seed("Optimistic", "81D4FA"),     // Sky blue
        Seed("Relieved", "80CBC4"),       // Light teal

        // ---- Energetic / Intense ----
        Seed("Passionate", "EF5350"),     // Warm red
        Seed("Determined", "FF7043"),     // Burnt orange
        Seed("Confident", "42A5F5"),      // Bold blue
        Seed("Energised", "66BB6A"),      // Fresh green
        Seed("Adventurous", "FFAB40"),    // Adventurous amber
        Seed("Empowered", "AB47BC"),      // Strong purple
        Seed("Fired Up", "F44336"),       // Fire red
        Seed("Motivated", "4CAF50"),      // Green
        Seed("Fearless", "FF5722"),       // Deep orange

        // ---- Reflective / Neutral ----
        Seed("Thoughtful", "90A4AE"),     // Blue-grey
        Seed("Curious", "4DD0E1"),        // Cyan
        Seed("Nostalgic", "D4A574"),      // Warm amber
        Seed("Mellow", "C5E1A5"),         // Soft lime
        Seed("Pensive", "78909C"),        // Steel blue
        Seed("Contemplative", "B0BEC5"),  // Silver
        Seed("Daydreamy", "B39DDB"),      // Lavender
        Seed("Peaceful", "81C784"),       // Gentle green
        Seed("Wistful", "BCAAA4"),        // Warm taupe
        Seed("Serene", "4FC3F7"),         // Light azure

        // ---- Mixed / Unsettled ----
        Seed("Restless", "E07C4F"),       // Rusty orange
        Seed("Conflicted", "FFA270"),     // Peach
        Seed("Numb", "CFD8DC"),           // Pale grey-blue
        Seed("Uncertain", "AED581"),      // Yellow-green

        // ---- Social / Connected ----
        Seed("Loved", "F48FB1"),          // Rose pink
        Seed("Affectionate", "F06292"),   // Deep pink
        Seed("Empathetic", "9FA8DA"),     // Periwinkle
        Seed("Thankful", "009688"),       // Deep teal
        Seed("Connected", "26C6DA"),      // Bright cyan
        Seed("Compassionate", "BA68C8"),  // Orchid

        // ---- Low Energy / Quiet ----
        Seed("Tired", "9E9E9E"),          // Medium grey
        Seed("Lazy", "BDBDBD"),           // Light grey
        Seed("Bored", "B8C4CA"),          // Soft blue-grey
        Seed("Drained", "6B7B8D"),        // Dark slate
        Seed("Sleepy", "7986CB"),         // Muted indigo
        Seed("Lethargic", "A1887F"),      // Warm brown

        // ---- Negative / Difficult ----
        Seed("Frustrated", "E57373"),     // Muted red
        Seed("Overwhelmed", "7B1FA2"),    // Dark purple
        Seed("Irritated", "D4845A"),      // Burnt sienna
        Seed("Lonely", "5C6BC0"),         // Indigo
        Seed("Jealous", "689F38"),        // Olive green
        Seed("Embarrassed", "E91E63"),    // Bright pink
        Seed("Guilty", "8D6E63"),         // Brown
        Seed("Vulnerable", "D1A3E0"),     // Lilac
        Seed("Melancholy", "3F51B5"),     // Deep indigo
        Seed("Worried", "9575CD"),        // Medium purple
        Seed("Insecure", "C9A882"),       // Sandy tan
        Seed("Angry", "D32F2F"),          // Strong red
        Seed("Scared", "512DA8"),         // Dark violet
        Seed("Disappointed", "7E8C8D"),   // Sage grey
        Seed("Heartbroken", "880E4F"),    // Deep rose
        Seed("Stressed", "E040FB"),       // Bright magenta
    )
}
```

58 curated moods. All hex codes are unique. Colors within related emotional families
share a hue range (e.g. purples for introspection, reds for intensity) but are
distinguishable on the calendar.

**Changes from v1 draft based on review:**
- Fixed 5 duplicate hex collisions (Playful/Irritated, Proud/Vulnerable, Pensive/Drained, Lethargic/Insecure, Contemplative/Bored)
- Replaced "Fierce" → "Fired Up", "Bold" → "Empowered", "Generous" → "Connected" (emotional states, not traits)
- Added "Restless", "Conflicted", "Numb", "Uncertain" (neutral/mixed states)

---

## Architecture

### New Files

| File | Layer | Purpose |
|------|-------|---------|
| `journal/domain/model/CuratedMoods.kt` | Domain | The curated mood+color list |
| `journal/domain/usecases/moodcolormanagement/SeedRandomMoodColorsUseCase.kt` | Domain | Business logic: pick random, skip duplicates, save |
| `journal/ui/moodcolormanagement/components/SeedRandomMoodColorsDialog.kt` | UI | Confirmation dialog with dice icon |

### Modified Files

| File | Change |
|------|--------|
| `MoodColorDao.kt` | Add `getActiveMoodNames()` query |
| `MoodColorRepository.kt` | Add `getActiveMoodNames()` interface method |
| `MoodColorRepositoryImpl.kt` | Implement `getActiveMoodNames()` |
| `MoodColorManagementUseCases.kt` | Add `seedRandomMoodColors` field |
| `MoodColorManagementAction.kt` | Add `RequestSeedRandom`, `ConfirmSeedRandom`, `DismissSeedRandomDialog` |
| `MoodColorManagementUiState.kt` | Add `showSeedRandomDialog: Boolean`, `isSeedingInProgress: Boolean` |
| `MoodColorManagementViewModel.kt` | Handle new actions, call use case, manage seeding state |
| `MoodColorManagementScreen.kt` | Add dice `IconButton` in TopAppBar actions, show dialog |
| `MoodColorEvent.kt` | (No new event needed — deferred dice animation, list animateItem is the feedback) |
| `JournalModule.kt` | Wire `SeedRandomMoodColorsUseCase` DI |
| `FakeMoodColorRepository.kt` | Add `getActiveMoodNames()` for tests |
| `strings.xml` | Add dialog strings |

---

## Detailed Design

### `SeedRandomMoodColorsUseCase`

```kotlin
class SeedRandomMoodColorsUseCase(
    private val saveMoodColor: SaveMoodColorUseCase,
    private val repository: MoodColorRepository,
    private val timeProvider: TimeProvider,
) {
    companion object {
        private const val MIN_SEED_COUNT = 5
        private const val MAX_SEED_COUNT = 10
    }

    suspend operator fun invoke(): Result<Int, MoodColorError> {
        // 1. Get all existing mood names (normalized to lowercase)
        val existingNames = repository.getActiveMoodNames()

        // 2. Filter curated list to only moods not already present
        val available = CuratedMoods.ALL.filter { seed ->
            seed.mood.trim().lowercase() !in existingNames
        }

        if (available.isEmpty()) return Result.Success(0)

        // 3. Pick random count, capped at available size
        val count = Random.nextInt(MIN_SEED_COUNT, MAX_SEED_COUNT + 1)
            .coerceAtMost(available.size)

        // 4. Shuffle and take
        val selected = available.shuffled().take(count)

        // 5. Save each via SaveMoodColorUseCase
        var successCount = 0
        for (seed in selected) {
            val moodColor = MoodColor(
                mood = seed.mood,
                color = seed.color,
                dateStamp = timeProvider.instant().toEpochMilli(),
            )
            when (val result = saveMoodColor(moodColor)) {
                is Result.Success -> successCount++
                is Result.Error -> when (result.error) {
                    MoodColorError.LimitReached -> break  // Hit 50 cap, stop
                    else -> continue  // Skip this one (duplicate race, DB glitch), try next
                }
            }
        }

        return Result.Success(successCount)
    }
}
```

**Key design decisions:**
- `TimeProvider` injected for testability (matching `SeedDefaultMoodColorsUseCase` pattern)
- `break` only on `LimitReached`; `continue` on all other errors (DuplicateName from a race, DatabaseError, etc.)
- Uses `getActiveMoodNames()` for efficient bulk duplicate check (one query instead of N)

### DAO + Repository

```kotlin
// MoodColorDao.kt — new query
@Query("SELECT moodNormalized FROM mood_color WHERE isDeleted = 0")
suspend fun getActiveMoodNames(): List<String>

// MoodColorRepository.kt — new interface method
suspend fun getActiveMoodNames(): Set<String>

// MoodColorRepositoryImpl.kt — implementation
// Intentionally bare (no ErrorMapper.safeSuspendCall / Result wrapper).
// This is a non-critical read used only by the random seeder — if the DAO
// throws, the exception propagates to the ViewModel's coroutine, which is
// wrapped in a finally { isSeedingInProgress = false } so the UI recovers
// gracefully. Adding a Result wrapper here would add boilerplate for no
// user-visible gain.
override suspend fun getActiveMoodNames(): Set<String> {
    return dao.getActiveMoodNames().toSet()
}
```

**Note on soft-deleted moods:** `getActiveMoodNames()` queries `isDeleted = 0`, so a
soft-deleted "Joyful" passes the filter. `SaveMoodColorUseCase` will then *restore* it
(with its original sync fields) rather than inserting fresh. This is correct behavior —
the user may be surprised to see a deleted mood reappear, but the dice is explicitly
"add moods" so restoring a soft-deleted one is a reasonable interpretation. Documented
here for awareness.

### ViewModel Changes

```kotlin
// State additions:
data class MoodColorManagementUiState(
    // ... existing fields ...
    val showSeedRandomDialog: Boolean = false,
    val isSeedingInProgress: Boolean = false,
)

// Action additions:
sealed interface MoodColorManagementAction {
    // ... existing actions ...
    data object RequestSeedRandom : MoodColorManagementAction
    data object ConfirmSeedRandom : MoodColorManagementAction
    data object DismissSeedRandomDialog : MoodColorManagementAction
}

// Handler:
private fun showSeedRandomDialog() {
    _state.update { it.copy(showSeedRandomDialog = true) }
}

private fun dismissSeedRandomDialog() {
    _state.update { it.copy(showSeedRandomDialog = false) }
}

private fun seedRandomMoodColors() {
    _state.update { it.copy(showSeedRandomDialog = false, isSeedingInProgress = true) }
    viewModelScope.launch {
        try {
            when (val result = useCases.seedRandomMoodColors()) {
                is Result.Success -> {
                    if (result.data > 0) syncScheduler.requestImmediateSync()
                }
                is Result.Error -> { /* absorbed — non-critical action */ }
            }
        } finally {
            // Always re-enable the button, even if the coroutine dies
            // from an uncaught exception (e.g. getActiveMoodNames() DB failure).
            _state.update { it.copy(isSeedingInProgress = false) }
        }
    }
}
```

**Concurrency guard:** `isSeedingInProgress` is set `true` before launch and `false` in
the coroutine's finally-equivalent position. The dice `IconButton` checks this flag and
disables while seeding is in progress, preventing double-tap races.

### Screen Changes

**TopAppBar actions — dice button placement:**
```
┌──────────────────────────────────────────────┐
│ ←  Manage Mood Colors     🔀  [40 / 50]     │
│                          dice   counter      │
└──────────────────────────────────────────────┘
```

**Icon:** `Icons.Default.Shuffle` — available in the default Material Icons set
(no `material-icons-extended` dependency needed). Communicates "randomize" clearly.
`Icons.Default.Casino` would be ideal but requires the extended icon set which isn't
a project dependency.

**Dice button disabled when:**
- `state.isSeedingInProgress` is true (seeding in flight)
- `state.moodColors.size >= SaveMoodColorUseCase.MAX_MOOD_COLORS` (at 50 cap)

```kotlin
// In TopAppBar actions slot:
IconButton(
    onClick = { onAction(MoodColorManagementAction.RequestSeedRandom) },
    enabled = !state.isSeedingInProgress &&
        state.moodColors.size < SaveMoodColorUseCase.MAX_MOOD_COLORS,
) {
    Icon(
        imageVector = Icons.Default.Shuffle,
        contentDescription = stringResource(R.string.seed_random_mood_colors),
    )
}
```

**Animation:** Deferred. The list items appearing via the existing `animateItem()`
modifier on the `LazyColumn` is sufficient feedback. A TopAppBar dice animation can
be added as a follow-up if desired.

### Confirmation Dialog

```
┌──────────────────────────────────────┐
│                                      │
│              🔀  (large)             │
│                                      │
│     Add random mood colors?          │
│                                      │
│   A handful of new moods will be     │
│   added to your palette              │
│                                      │
│         [Cancel]    [Add]            │
└──────────────────────────────────────┘
```

Follows the existing `DeleteMoodColorConfirmDialog` pattern: `AlertDialog` with
title, message, confirm + dismiss buttons. The icon is a large centered `Shuffle`
icon above the title text.

**Dialog text is deliberately vague** ("A handful of new moods") rather than
promising "5-10" — because if only 3 curated moods are left in the pool, the
actual count will be lower. This avoids setting expectations the feature can't meet.

---

## Edge Cases

| Case | Behavior |
|------|----------|
| All 58 curated moods already exist | `invoke()` returns `Success(0)`, no visible change |
| 50-cap hit mid-batch | Saves as many as fit, breaks on `LimitReached`, returns partial count |
| Non-LimitReached error mid-batch | Skips that mood (`continue`), tries next. Handles DuplicateName race, transient DB errors |
| User taps dice while seeding | Button disabled via `isSeedingInProgress` flag |
| Soft-deleted curated mood exists | `SaveMoodColorUseCase.restoreIfSoftDeleted` restores it with original sync fields. Correct behavior, documented above |
| Offline | Saves locally, sync happens when connectivity returns (existing WorkManager pattern) |
| Config change mid-seeding | Coroutine survives on `viewModelScope`. `isSeedingInProgress` resets when it completes. Dialog is dismissed before launch so no dialog state to lose |

---

## Strings

```xml
<string name="seed_random_mood_colors">Add random mood colors</string>
<string name="seed_random_dialog_title">Add random mood colors?</string>
<string name="seed_random_dialog_message">A handful of new moods will be added to your palette</string>
<string name="seed_random_confirm">Add</string>
```

---

## Testing Strategy

### Unit Tests

| Test | What it verifies |
|------|-----------------|
| **SeedRandomMoodColorsUseCaseTest** | |
| Seeds 5-10 moods when pool has enough available | Count is in range, all saved |
| Skips existing moods by name (case-insensitive) | Only non-duplicates saved |
| Returns 0 when all curated moods exist | No error, just 0 |
| Stops on LimitReached, continues on other errors | Partial success, returns count |
| Handles empty pool gracefully | No crash, returns 0 |
| Uses TimeProvider for dateStamp | Verifiable via FakeTimeProvider |
| **MoodColorManagementViewModelTest** | |
| RequestSeedRandom opens dialog | `showSeedRandomDialog = true` |
| ConfirmSeedRandom calls use case, sets loading, syncs | `isSeedingInProgress` lifecycle |
| DismissSeedRandomDialog closes dialog | `showSeedRandomDialog = false` |
| Button disabled while seeding | State flag prevents re-entry |

### Manual Tests

- [ ] Tap dice → confirmation dialog appears with large shuffle icon
- [ ] Confirm → dialog closes, new moods appear in list (animated)
- [ ] Cancel → dialog closes, nothing changes
- [ ] Dice disabled at 50 moods
- [ ] Dice disabled while seeding in progress
- [ ] Multiple taps across sessions → diminishing returns (fewer new moods each time)
- [ ] Delete some moods, tap dice → new moods fill gaps

---

## Implementation Order

1. **`CuratedMoods.kt`** — the data (no dependencies)
2. **DAO + Repository** — `getActiveMoodNames()` query + interface + impl + fake
3. **`SeedRandomMoodColorsUseCase.kt`** — business logic + TimeProvider injection
4. **Unit tests for use case**
5. **Actions + State** — new action variants, `showSeedRandomDialog`, `isSeedingInProgress`
6. **`MoodColorManagementUseCases.kt`** — add `seedRandomMoodColors` field
7. **`JournalModule.kt`** — DI wiring
8. **`SeedRandomMoodColorsDialog.kt`** — the confirmation UI
9. **`MoodColorManagementScreen.kt`** — dice button + dialog wiring
10. **`MoodColorManagementViewModel.kt`** — handle actions
11. **ViewModel tests**
12. **Strings**

---

## Decisions Log

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | `CuratedMoods.Seed` data class, not `Pair` or `MoodColor` | Named, lightweight, no sync/id baggage |
| 2 | New DAO `getActiveMoodNames()` for bulk duplicate check | One query vs N `getMoodColorByName()` calls |
| 3 | Save through `SaveMoodColorUseCase` | Gets validation, 50-cap, normalization, sync for free |
| 4 | Dice disabled at 50 moods | Uses existing `MAX_MOOD_COLORS` constant |
| 5 | Defaults kept separate from curated | Different sync semantics (`syncId`, `updatedAt = 0L`) |
| 6 | `Icons.Default.Shuffle` not `Casino` | Casino needs `material-icons-extended` dependency |
| 7 | `TimeProvider` injected, not `System.currentTimeMillis()` | Matches seed use case pattern, testable |
| 8 | `break` only on `LimitReached`, `continue` otherwise | DuplicateName race or transient DB error shouldn't abort the whole batch |
| 9 | `isSeedingInProgress` state flag for concurrency | Prevents double-tap, simpler than Mutex |
| 10 | Dialog text says "handful" not "5-10" | If only 3 are available, "5-10" would be a lie |
| 11 | Dice animation deferred | `animateItem()` on the LazyColumn is sufficient feedback for v1 |
| 12 | Soft-delete restore is acceptable | Documented; dice is "add moods" so restoring a deleted one fits the intent |
| 13 | `finally` block resets `isSeedingInProgress` | Uncaught exceptions (e.g. DAO failure) must not permanently disable the button |
| 14 | `getActiveMoodNames()` skips `ErrorMapper` | Non-critical read; exception propagates to ViewModel's `finally` block. Comment documents the intentional deviation |
| 15 | Sync only when `result.data > 0` | Avoid pointless WorkManager enqueue when no moods were actually added |
