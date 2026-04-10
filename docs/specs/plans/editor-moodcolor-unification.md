# Revised Plan: Editor MoodColor Unification

## Design Decisions (Confirmed)

- **Remove delete from Editor**: Prevents accidental taps, rarely used
- **Remove navigation link**: Simplifies flow, avoids entry state complexity
- **Unify row appearance**: Editor dropdown matches Management list visually

---

## Revised Dropdown Structure

```
┌─────────────────────────────┐
│ ⭐ Happy      [🟡 edit]     │  ← MoodColorRow (compact)
│ ☆ Calm       [🔵 edit]     │
│ ☆ Excited    [🔴 edit]     │
└─────────────────────────────┘
        [🎨]  ← Color wheel button (adds new)
```

---

## Key Change: Single Shared Row Component

Delete Phase 2 ("Create MoodColorDropdownRow"). Instead, modify existing `MoodColorRow`:

```kotlin
@Composable
fun MoodColorRow(
    moodColor: MoodColor,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    entryCount: Int? = null,        // null = hidden (Editor context)
    compact: Boolean = false,       // true = dropdown sizing
)
```

**Usage:**

| Context | Call |
|---------|------|
| Management | `MoodColorRow(mc, onFav, onEdit, entryCount = 5)` |
| Editor dropdown | `MoodColorRow(mc, onFav, onEdit, compact = true)` |

**Benefits:**
- True DRY - one component, two contexts
- Visual consistency guaranteed
- Single place to update styling

---

## Revised Phase Summary

| Phase | Original | Revised |
|-------|----------|---------|
| 1 | Move shared dialogs | ✓ No change |
| 2 | Create MoodColorDropdownRow | **Delete** - use MoodColorRow with params |
| 3 | Refactor MoodItem.kt | Update to use `MoodColorRow(compact = true)` |
| 4 | Update EditorUseCases | ✓ No change |
| 5 | Update MoodColorActionDelegate | ✓ No change |
| 6 | Update EditorUiState & Actions | Remove DeleteMoodColor action only |
| 7 | Wire up in EditorScreen | Simplify - only onToggleFavorite needed |

---

## Revised File Changes

### New Files
- ~~`shared/moodcolor/MoodColorDropdownRow.kt`~~ **None**

### Moved Files (unchanged)
- `EditMoodColorDialog.kt` → `shared/moodcolor/`
- `MoodColorPickerDialog.kt` → `shared/moodcolor/AddMoodColorDialog.kt`
- `ColorWheelAddButton.kt` → `shared/moodcolor/ColorWheelButton.kt`

### Modified Files
- `shared/moodcolor/MoodColorRow.kt` - Add entryCount, compact params
- `editor/components/MoodItem.kt` - Use MoodColorRow, remove delete callback
- `editor/MoodColorActionDelegate.kt` - Use new use cases, add pendingFavorites
- `editor/EditorViewModel.kt` - Wire favorite action
- `editor/EditorScreen.kt` - Remove onDeleteMoodColor, add onToggleFavorite
- `editor/state/EditorAction.kt` - Add ToggleMoodColorFavorite, remove DeleteMoodColor
- `domain/usecases/editor/EditorUseCases.kt` - Swap use cases
- `domain/usecases/shared/moodcolor/SaveMoodColorUseCase.kt` - Add name trimming
- `di/JournalModule.kt` - Update DI wiring
- `res/values/strings.xml` - Add accessibility strings

### Deleted Code
- Delete button + callback chain in `MoodItem.kt`
- `DeleteMoodColor` action from Editor
- "Add custom mood color" dropdown item text

---

## Decisions Made

### 1. Favorite Toggle: Race Condition Handling

**Decision: `pendingFavorites` map**

Mirror Management screen pattern for consistency:

```kotlin
// In MoodColorActionDelegate
private val pendingFavorites = mutableMapOf<Int, Boolean>()

fun handleToggleFavorite(moodColor: MoodColor) {
    val id = moodColor.id ?: return
    val targetState = pendingFavorites[id] ?: !moodColor.isFavorite
    pendingFavorites[id] = targetState

    viewModelScope.launch {
        editorUseCases.setMoodColorFavorite(id, targetState)
        pendingFavorites.remove(id)
    }
}
```

### 2. Name Sanitization

**Decision: Option A - `SaveMoodColorUseCase`**

Keep normalization logic together:

```kotlin
suspend operator fun invoke(moodColor: MoodColor): Result<MoodColor, MoodColorError> {
    val normalizedColor = MoodColorValidation.normalizeHexColor(moodColor.color)
    val normalizedMood = moodColor.mood.trim()
    val normalizedMoodColor = moodColor.copy(
        mood = normalizedMood,
        color = normalizedColor
    )
    // ... rest unchanged
}
```

### 3. Upper Bound on Mood Colors

**Decision: Defer**

- Typical users have <20 mood colors
- Dropdown already has `heightIn(max = 300.dp)` with scroll
- Add limit later if performance issue reported
- Avoids new error type and UI for edge case

### 4. MoodColor Construction in Delegate

**Explicit patterns:**

```kotlin
// For NEW mood color (from add dialog)
fun handleSaveMoodColor(mood: String, colorHex: String) {
    viewModelScope.launch {
        val moodColor = MoodColor(
            mood = mood,
            color = colorHex,
            dateStamp = System.currentTimeMillis()
        )
        when (val result = editorUseCases.saveMoodColor(moodColor)) {
            is Result.Success -> { /* update state, auto-select */ }
            is Result.Error -> { /* show error via MoodColorErrorFormatter */ }
        }
    }
}

// For EDIT (from edit dialog) - receives existing MoodColor
fun handleUpdateMoodColor(moodColor: MoodColor, newMood: String, newColorHex: String) {
    viewModelScope.launch {
        val updated = moodColor.copy(mood = newMood, color = newColorHex)
        when (val result = editorUseCases.saveMoodColor(updated)) {
            is Result.Success -> { /* update state */ }
            is Result.Error -> { /* show error */ }
        }
    }
}
```

---

## Accessibility

Add to `MoodColorRow`:

```kotlin
Icon(
    imageVector = if (moodColor.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
    contentDescription = if (moodColor.isFavorite) {
        stringResource(R.string.remove_from_favorites_desc, moodColor.mood)
    } else {
        stringResource(R.string.add_to_favorites_desc, moodColor.mood)
    },
)
```

Add strings:
```xml
<string name="add_to_favorites_desc">Add %1$s to favorites</string>
<string name="remove_from_favorites_desc">Remove %1$s from favorites</string>
```

---

## Test Migration

| Moved File | Action |
|------------|--------|
| `EditMoodColorDialog.kt` | Update imports in any UI tests |
| `MoodColorPickerDialog.kt` | Rename references to `AddMoodColorDialog` |
| `ColorWheelAddButton.kt` | Rename to `ColorWheelButton` |
| `MoodColorRow.kt` | Add tests for new params (`entryCount = null`, `compact = true`) |

---

## Testing Strategy

### Unit Tests
- `MoodColorActionDelegate`: Test `handleSaveMoodColor`, `handleUpdateMoodColor`, `handleToggleFavorite`
- `SaveMoodColorUseCase`: Test name trimming

### Component Tests
- `MoodColorRow`: Test all param combinations (compact, entryCount)

### Manual Tests
- [ ] Add mood color in Editor → verify validation works
- [ ] Edit mood color → verify color normalization (8→6 char)
- [ ] Edit mood color → verify name trimming
- [ ] Toggle favorite in Editor → verify persists after reload
- [ ] Toggle favorite rapidly → verify no race condition
- [ ] Verify dropdown sorted by favorites
- [ ] Verify delete NOT available in Editor dropdown
- [ ] Accessibility: TalkBack navigation through dropdown

---

## Implementation Order

1. **Phase 1**: Move shared dialogs (low risk)
2. **Modify MoodColorRow**: Add `entryCount`, `compact` params
3. **Phase 3**: Refactor `MoodItem.kt` to use shared row
4. **Phase 4**: Update `EditorUseCases` and DI
5. **Phase 5**: Update `MoodColorActionDelegate` with new use cases + pendingFavorites
6. **Phase 6**: Update `EditorAction` (add favorite, remove delete)
7. **Phase 7**: Wire up in `EditorScreen`
8. **Cleanup**: Remove dead code, update tests

---

## Summary of Changes from Original Plan

| Section | Change |
|---------|--------|
| Phase 2 | Delete entirely |
| Phase 3 | Use `MoodColorRow(compact = true)` instead of new component |
| Phase 6 | Remove navigation action, keep only favorite toggle |
| Phase 7 | Simplify - no navigation callback |
| New Files | None (was 1) |
| MoodColorRow.kt | Add optional params instead of new component |
| Edge Cases | Add: name sanitization, favorite race condition pattern |
| Testing | Add: test migration list, accessibility tests |
