# Pre-Release Polish Plan

## Context

The branch is feature-complete and all tests/detekt pass. These are the remaining rough edges identified during the final review before promoting from closed testing to production.

---

## Item 1: Typed error model for Entry errors

### Problem

Six English strings are hardcoded in `EditorViewModel.kt` and one in `MoodColorActionDelegate.kt` instead of being string resources. This breaks localisation and violates the pattern established by `MoodColorError` + `MoodColorErrorFormatter`.

**EditorViewModel.kt:**

| Line | String | Context |
|------|--------|---------|
| 150 | `"Entry not found. It may have been deleted."` | Load error banner |
| 160 | `"Failed to load entry: ${e.message}"` | Load error banner |
| 319 | `"Failed to load entry for this date"` | Snackbar event |
| 354 | `"Please select or create a mood"` | Inline mood validation |
| 382 | `"Couldn't save entry"` | Snackbar event (fallback for `e.message`) |
| 416 | `"Cannot retry: no entry ID"` | Load error banner |

**MoodColorActionDelegate.kt:**

| Line | String | Context |
|------|--------|---------|
| 130 | `"\"${result.data.mood}\" updated"` | Success snackbar (interpolated) |

### Approach

Follow the `MoodColorError` pattern but **fix the layer violation**: sealed interface in domain, formatter in the **UI layer** (not domain).

1. Create `EntryError` sealed interface in domain
2. Create `EntryErrorFormatter` in **`journal/ui/editor/formatter/`** (not domain — it depends on `Context`)
3. Store typed errors in `EditorUiState`
4. Format in the Composable

Also move `MoodColorErrorFormatter` from domain to UI as part of this change to fix the existing layer violation. Both formatters are presentation concerns that depend on `android.content.Context`.

### Files

| File | Change |
|------|--------|
| `journal/domain/model/EntryError.kt` | **New** — sealed interface |
| `journal/ui/editor/formatter/EntryErrorFormatter.kt` | **New** — `format(context, error)` |
| `journal/ui/shared/moodcolor/MoodColorErrorFormatter.kt` | **Moved** from `journal/domain/model/` |
| `journal/ui/editor/state/EditorUiState.kt` | `loadError: String?` → `EntryError?`, `moodError: String?` → `EntryError?` |
| `journal/ui/editor/state/EditorUiEvent.kt` | Add `ShowEntryError(error)` variant |
| `journal/ui/editor/EditorViewModel.kt` | Replace 6 hardcoded strings with typed errors |
| `journal/ui/editor/MoodColorActionDelegate.kt` | Replace interpolated snackbar with string resource |
| `journal/ui/editor/EditorScreen.kt` | Format typed errors, update `MoodColorErrorFormatter` import |
| `journal/ui/moodcolormanagement/MoodColorManagementScreen.kt` | Update `MoodColorErrorFormatter` import |
| `res/values/strings.xml` | Add 7 new string resources |
| `EditorViewModelTest.kt` | Assert on typed errors instead of strings |

### Design

```kotlin
// journal/domain/model/EntryError.kt
sealed interface EntryError : uk.co.zlurgg.thedayto.core.domain.error.Error {
    data object NotFound : EntryError
    data object LoadFailed : EntryError
    data object DateLoadFailed : EntryError
    data object NoMoodSelected : EntryError
    data object SaveFailed : EntryError
    data object RetryFailed : EntryError
}
```

All variants are `data object` — no exception messages carried through the domain.
`e.message` is already logged at the catch site via `Timber.e` before the error
is created. The domain type doesn't need to transport it.

```kotlin
// journal/ui/editor/formatter/EntryErrorFormatter.kt
object EntryErrorFormatter {
    fun format(context: Context, error: EntryError): String = when (error) {
        EntryError.NotFound -> context.getString(R.string.error_entry_not_found)
        EntryError.LoadFailed -> context.getString(R.string.error_entry_load_failed)
        EntryError.DateLoadFailed -> context.getString(R.string.error_entry_date_load_failed)
        EntryError.NoMoodSelected -> context.getString(R.string.error_no_mood_selected)
        EntryError.SaveFailed -> context.getString(R.string.error_entry_save_failed)
        EntryError.RetryFailed -> context.getString(R.string.error_retry_failed)
    }
}
```

```kotlin
// MoodColorActionDelegate.kt:130 — interpolated success string
// Before: uiEvents.emit(EditorUiEvent.ShowSnackbar("\"${result.data.mood}\" updated"))
// After:  use a string resource with placeholder
uiEvents.emit(EditorUiEvent.ShowSnackbar(
    context.getString(R.string.mood_color_updated_format, result.data.mood)
))
```

Wait — the delegate doesn't have a Context. Two options:
- **Option A:** Emit a typed event like `ShowMoodColorUpdated(moodName)` and format in the screen.
- **Option B:** Pass the format via a string resource ID + arg through the snackbar event.

**Decision: Option A.** Add `ShowMoodColorUpdated(val moodName: String)` to `EditorUiEvent`.
The screen formats it: `context.getString(R.string.mood_color_updated_format, event.moodName)`.
This keeps the delegate Context-free.

### Layer violation fix: MoodColorErrorFormatter

Move from `journal/domain/model/MoodColorErrorFormatter.kt` → `journal/ui/shared/moodcolor/MoodColorErrorFormatter.kt`.

The sealed interface `MoodColorError` stays in domain (correct). The formatter
that depends on `Context` moves to UI (correct). Update imports in:
- `EditorScreen.kt`
- `MoodColorManagementScreen.kt`

### Edge cases

- The `handleSaveEntry` catch block (line 379-382) catches `InvalidEntryException`
  and falls back to `e.message ?: "Couldn't save entry"`. After the refactor, the
  ViewModel emits `EntryError.SaveFailed` and the screen formats it via
  `EntryErrorFormatter`. The exception message is still logged via `Timber.e(e, ...)`
  on the line above — no information lost.
- Existing tests assert string equality (e.g. `assertEquals("Please select or create
  a mood", state.moodError)`). These will assert on the typed error instead:
  `assertEquals(EntryError.NoMoodSelected, state.moodError)`. Simpler assertions.

### Assumptions

- Six strings in EditorViewModel + one interpolated string in MoodColorActionDelegate
  are ALL the hardcoded user-facing strings in the editor module. **Verified via grep.**
  Remaining quoted strings are `Timber.d/w/e` log messages (developer-facing).

---

## Item 2: Management empty state mentions ✨ button

### Problem

Empty state says "Tap + to create your first mood color" — doesn't mention the ✨
sparkles button for random seeding.

### Approach

Update the subtitle string. Add XML comment to keep it in sync with the icon choice.

### Files

| File | Change |
|------|--------|
| `res/values/strings.xml` | Update `tap_plus_to_add` |

### Design

```xml
<!-- Keep in sync with MoodColorManagementScreen's seed icon (AutoAwesome) -->
<string name="tap_plus_to_add">Tap + to create one, or ✨ for a random selection</string>
```

### Edge cases

- ✨ button is disabled at 50 moods. Empty state requires 0 moods. Mutually exclusive
  — the ✨ reference is always valid when the empty state is visible.
- ✨ emoji (U+2728) renders on all API 26+ devices.

---

## Item 3: Editor date row accessibility

### Problem

The date Row is `Modifier.clickable { ... }` without semantic grouping. TalkBack reads
the children individually instead of as one tappable element.

### Approach

Add `semantics(mergeDescendants = true)` and `onClickLabel` to the date Row.

Also audit other clickable elements in EditorScreen. The only other `Modifier.clickable`
is the `ManageMoodColorsCard` which already uses a `JournalCard` (Card has built-in
semantics) with a `clickable` modifier and `contentDescription` on the arrow icon.
**No other gaps found.**

### Files

| File | Change |
|------|--------|
| `journal/ui/editor/EditorScreen.kt` | Add semantics to date Row |
| `res/values/strings.xml` | Add `change_date_action` string |

### Design

```kotlin
Row(
    modifier = Modifier
        .semantics(mergeDescendants = true) { }
        .clickable(onClickLabel = stringResource(R.string.change_date_action)) {
            onAction(EditorAction.ToggleDatePicker)
        },
    ...
)
```

TalkBack result: "15th January 2024, Double tap to Change date"

---

## Item 4: ManageMoodColorsCard subtitle

### Problem

The card shows "Mood Colors →" with no hint about what the destination is.

### Approach

Add a `bodySmall` subtitle: "Manage your palette".

### Files

| File | Change |
|------|--------|
| `journal/ui/editor/components/ManageMoodColorsCard.kt` | Add subtitle Text |
| `res/values/strings.xml` | Add `mood_colors_card_subtitle` |

### Design

Wrap the title Text in a Column and add the subtitle below:

```kotlin
Column(modifier = Modifier.weight(1f)) {
    Text(
        text = stringResource(R.string.mood_colors_card_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
    )
    Text(
        text = stringResource(R.string.mood_colors_card_subtitle),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}
```

Adds ~16dp to the card height (64dp total, still under MonthStatistics' ~80dp).

---

## Strings

```xml
<!-- Entry errors -->
<string name="error_entry_not_found">Entry not found. It may have been deleted.</string>
<string name="error_entry_load_failed">Failed to load entry. Please try again.</string>
<string name="error_entry_date_load_failed">Failed to load entry for this date</string>
<string name="error_no_mood_selected">Please select or create a mood</string>
<string name="error_entry_save_failed">Couldn\'t save entry. Please try again.</string>
<string name="error_retry_failed">Cannot retry. Please go back and try again.</string>

<!-- Mood color update success -->
<string name="mood_color_updated_format">\"%1$s\" updated</string>

<!-- Accessibility -->
<string name="change_date_action">Change date</string>

<!-- ManageMoodColorsCard -->
<string name="mood_colors_card_subtitle">Manage your palette</string>
```

---

## Implementation Order

1. **Strings** — add all new string resources
2. **Item 2** — empty state string update (1 line)
3. **Item 4** — ManageMoodColorsCard subtitle (5 lines)
4. **Item 3** — date row accessibility (3 lines)
5. **Commit** items 2-4 as one focused commit
6. **Move `MoodColorErrorFormatter`** from domain to UI layer
7. **Item 1** — `EntryError` sealed interface + formatter + state migration +
   delegate update + test updates
8. **Commit** items 6+1 as one focused refactor commit

---

## Testing Strategy

### Item 1 (Entry error model)

| Test | What it verifies |
|------|-----------------|
| `EditorViewModelTest` — updated assertions | Typed errors: `assertEquals(EntryError.NoMoodSelected, state.moodError)` etc. |
| `EntryErrorFormatterTest` | **New** — parameterized test mapping all 6 variants to non-empty strings |
| Existing Compose UI tests (if present) | `EditorScreenTest` already exists in androidTest — verify it still compiles |

### Items 2-4 (UI polish)

| Test | What it verifies |
|------|-----------------|
| Manual | Empty state mentions ✨ |
| Manual | ManageMoodColorsCard shows subtitle |
| Manual | TalkBack reads date row correctly |

Note: the project has `EditorScreenTest.kt` in androidTest but it tests composable
rendering, not content strings. Adding `onNodeWithText("Manage your palette").assertExists()`
is trivial but would require updating the test's mock state. Not blocking — the
subtitle is a static string on a static composable.

---

## Decisions Log

| # | Decision | Rationale |
|---|----------|-----------|
| 1 | All `EntryError` variants are `data object`, not `data class` | No exception messages in the domain type. `e.message` is logged at the catch site via Timber. |
| 2 | `EntryErrorFormatter` in `journal/ui/editor/formatter/`, not domain | Depends on `android.content.Context` — presentation concern belongs in the UI layer. |
| 3 | Move `MoodColorErrorFormatter` to UI too | Fixes existing layer violation. Same rationale as #2. Two wrongs ≠ right. |
| 4 | Delegate's success snackbar uses `ShowMoodColorUpdated(moodName)` event | Keeps the delegate Context-free. Screen formats via string resource. |
| 5 | ✨ emoji string has XML comment for sync | If the icon changes, the comment reminds maintainers to update the empty state text. |
| 6 | Only the date Row needs a11y fix in EditorScreen | Audited: ManageMoodColorsCard already has Card semantics + icon contentDescription. No other bare clickable Rows. |
| 7 | Merged Item 5 into Item 1 | They were the same task restated. One item, one implementation. |
