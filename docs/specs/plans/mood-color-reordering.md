# Mood Color Favorites + Usage Ordering

## Overview

Replace manual sort options with automatic favorites + usage-based ordering. Users mark favorites (pinned at top), remaining items sorted by usage count.

**Status:** Planned (Pre-release)
**Effort:** Medium
**Priority:** Enhancement

---

## PR Strategy

Split implementation into 3 focused PRs for easier review:

### PR 1: Bug Fix + Data Foundation
**Branch:** `fix/mood-color-dialog-reset`
**Scope:**
- Phase 0: EditMoodColorDialog bug fix
- Phase 1.1-1.5: Entity, DAO, domain model updates
- Phase 1.6: MoodColorSorting extension
- Phase 1.7-1.10: Mappers, repository, Firestore
- Phase 1.11: Seed updates
- Update `FakeMoodColorRepository` for tests

**Tests:** Unit tests for new repository methods
**Size:** ~15 files

### PR 2: Domain UseCases + Shared Components
**Branch:** `feat/mood-color-usecases`
**Scope:**
- Phase 2: All new UseCases
- Phase 3: UI State classes
- Phase 4: Shared UI components (AnimatedFavoriteIcon, EditableColorCircle, MoodColorRow)
- Constants extraction

**Tests:** UseCase unit tests
**Size:** ~12 files

### PR 3: Screen Integration + Cleanup
**Branch:** `feat/mood-color-favorites-ui`
**Scope:**
- Phase 5: Management screen updates
- Phase 6: Editor screen updates
- Phase 7: DI wiring
- Phase 8: Delete unused files
- Phase 9: Remaining tests

**Tests:** ViewModel tests, manual testing
**Size:** ~15 files, ~4 deletions

### Merge Order
```
main ← PR1 ← PR2 ← PR3
```

Each PR is independently testable and deployable.

---

## Pre-Release Database Strategy

**Important:** This project has no production users. Database changes do NOT require migrations.

- Keep database at **version 1**
- Add new columns directly to entities with default values
- Testers uninstall/reinstall to get new schema
- Migrations only needed after first production release

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                              │
├─────────────────────────┬───────────────────────────────────┤
│  ManagementViewModel    │       EditorViewModel             │
│  - owns delete queue    │       - no delete capability      │
│  - dialog state         │       - dialog state              │
│  - observes Flow        │       - observes same Flow        │
│                         │                                   │
│  Both observe GetSortedMoodColorsUseCase (Room Flow)        │
│  Room emits on any write → both screens update automatically│
├─────────────────────────────────────────────────────────────┤
│                      Domain Layer                            │
├─────────────────────────────────────────────────────────────┤
│  GetSortedMoodColorsUseCase    (combine + sort)             │
│  ValidateMoodColorUseCase      (name/color validation)      │
│  SaveMoodColorUseCase          (validate + insert/update)   │
│  DeleteMoodColorUseCase        (soft delete, return for undo)│
│  RestoreMoodColorUseCase       (undo delete)                │
│  SetMoodColorFavoriteUseCase   (set favorite flag)          │
├─────────────────────────────────────────────────────────────┤
│                       Data Layer                             │
└─────────────────────────────────────────────────────────────┘
```

**Key Design Decision:** Each ViewModel independently observes `GetSortedMoodColorsUseCase()` which returns a Room Flow. When any screen writes to the database, Room automatically emits new values to all observers. No shared singleton handler needed.

---

## Constants Extraction

Extract magic numbers to named constants for maintainability.

### Existing Constants (reuse)

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/util/InputValidation.kt`
```kotlin
const val MAX_MOOD_LENGTH = 50  // Already exists - reuse in ValidateMoodColorUseCase
```

### New Constants File

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/shared/moodcolor/MoodColorConstants.kt`

```kotlin
package uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Constants for mood color UI components.
 */
object MoodColorConstants {
    // Animation timing
    const val REORDER_DELAY_MS = 200L
    const val FAVORITE_ANIMATION_DURATION_MS = 200

    // Color circle sizes
    val COLOR_CIRCLE_SIZE_SMALL: Dp = 32.dp
    val COLOR_CIRCLE_SIZE_LARGE: Dp = 44.dp

    // Favorite icon animation
    const val ICON_SCALE_FAVORITE = 1f
    const val ICON_SCALE_UNFAVORITE = 0.8f

    // Edit icon contrast (for light/dark backgrounds)
    const val EDIT_ICON_ALPHA_ON_LIGHT = 0.6f
    const val EDIT_ICON_ALPHA_ON_DARK = 0.8f
    const val LUMINANCE_THRESHOLD = 0.5f

    // Border alpha
    const val BORDER_ALPHA = 0.3f

    // Icon button size (for dropdown items)
    val ICON_BUTTON_SIZE: Dp = 40.dp
}
```

### Validation Constants

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/util/MoodColorValidation.kt`

```kotlin
package uk.co.zlurgg.thedayto.journal.domain.util

/**
 * Validation constants for mood colors.
 */
object MoodColorValidation {
    /** Regex for valid 6-character hex color (no # prefix) */
    val HEX_COLOR_REGEX = Regex("^[A-Fa-f0-9]{6}$")
}
```

**Note:** `MAX_MOOD_LENGTH` stays in `InputValidation` since it's already used elsewhere.

---

## Phase 0: EditMoodColorDialog Bug Fix

### Problem

When editing a mood color, changing one property (mood name OR color) resets the other.

### Root Cause

In `EditMoodColorDialog.kt` line 139:
```kotlin
colorPickerController.selectByColor(initialColor, fromUser = false)
```

This runs on **every recomposition**, not just when the dialog opens.

### Fix

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/editor/components/EditMoodColorDialog.kt`

```kotlin
// Replace direct call with LaunchedEffect
// Use composite key to handle both existing and new mood colors
LaunchedEffect(moodColor.id, moodColor.color) {
    colorPickerController.selectByColor(initialColor, fromUser = false)
}
```

Using `moodColor.id` + `moodColor.color` as key ensures:
- Resets when editing a different mood color
- Resets when opening add dialog multiple times (color changes via `MoodColor.empty()`)

### Test Plan
1. Open editor, expand mood color section
2. Tap edit on any mood color
3. Change the mood name
4. Change the color using the picker
5. Tap save
6. Verify BOTH name and color are updated
7. **New:** Open add dialog, dismiss, open again - verify color picker resets

---

## Phase 1: Data Layer

### 1.1 Update MoodColorEntity

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/data/model/MoodColorEntity.kt`

Add `isFavorite` field (database stays at version 1):
```kotlin
data class MoodColorEntity(
    val mood: String,
    val moodNormalized: String,
    val color: String,
    val isDeleted: Boolean = false,
    val isFavorite: Boolean = false,  // NEW
    val dateStamp: Long,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
    val syncId: String? = null,
    val userId: String? = null,
    val updatedAt: Long? = null,
    val syncStatus: String = "LOCAL_ONLY"
)
```

**Note:** No index on `isFavorite` - sorting happens in Kotlin, not SQL.

### 1.2 Update MoodColorDao

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/data/dao/MoodColorDao.kt`

Add methods for favorite, restore, and getById:
```kotlin
@Query("SELECT * FROM mood_color WHERE id = :id")
suspend fun getMoodColorById(id: Int): MoodColorEntity?

@Query("UPDATE mood_color SET isFavorite = :isFavorite, updatedAt = :updatedAt WHERE id = :id")
suspend fun updateFavorite(id: Int, isFavorite: Boolean, updatedAt: Long)

@Query("UPDATE mood_color SET isDeleted = false, updatedAt = :updatedAt WHERE id = :id")
suspend fun restore(id: Int, updatedAt: Long)
```

**Note:** `getMoodColorByName` already exists in DAO and repository normalizes input before calling it.

### 1.3 Update EntryDao - Add Entry Count Query

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/data/dao/EntryDao.kt`

```kotlin
/**
 * Returns a map of mood color ID to entry count.
 * Efficient single query with GROUP BY instead of N+1 queries.
 */
@Query("""
    SELECT moodColorId, COUNT(*) as count
    FROM entry
    WHERE moodColorId IS NOT NULL
    GROUP BY moodColorId
""")
fun getMoodColorEntryCounts(): Flow<List<MoodColorEntryCount>>
```

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/data/model/MoodColorEntryCount.kt` (NEW)

```kotlin
data class MoodColorEntryCount(
    val moodColorId: Int,
    val count: Int
)
```

**Performance Note:** Ensure `entry` table has index on `moodColorId`:
```kotlin
@Entity(
    tableName = "entry",
    indices = [Index(value = ["moodColorId"])]  // Add if not present
)
```

### 1.4 Update MoodColor Domain Model

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/model/MoodColor.kt`

```kotlin
@Immutable
data class MoodColor(
    val mood: String,
    val color: String,
    val isDeleted: Boolean = false,
    val isFavorite: Boolean = false,  // NEW
    val dateStamp: Long,
    val id: Int? = null,
    val syncId: String? = null,
    val userId: String? = null,
    val updatedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY
) {
    companion object {
        /**
         * Creates an empty mood color for the add dialog.
         * Uses unique color each time to ensure LaunchedEffect re-runs.
         */
        fun empty() = MoodColor(
            mood = "",
            color = "CCCCCC",
            dateStamp = System.currentTimeMillis()  // Unique each call
        )
    }
}
```

### 1.5 Create MoodColorWithCount Domain Model

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/model/MoodColorWithCount.kt`

```kotlin
@Immutable
data class MoodColorWithCount(
    val moodColor: MoodColor,
    val entryCount: Int
)
```

### 1.6 Create Sorting Extension (DRY)

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/model/MoodColorSorting.kt`

```kotlin
/**
 * Single source of truth for mood color sorting.
 * Favorites first, then by entry count descending.
 */
fun List<MoodColorWithCount>.sortedByFavoriteAndUsage(): List<MoodColorWithCount> =
    sortedWith(
        compareByDescending<MoodColorWithCount> { it.moodColor.isFavorite }
            .thenByDescending { it.entryCount }
    )
```

### 1.7 Update Mappers

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/data/mapper/MoodColorMapper.kt`

Add `isFavorite` to both `toDomain()` and `toEntity()` functions.

### 1.8 Update Repository Interface

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/repository/MoodColorRepository.kt`

```kotlin
// EXISTING methods (no changes needed):
// - getMoodColorById(id: Int) ✓
// - getMoodColorByName(mood: String) ✓
// - deleteMoodColor(id: Int) ✓ (already does soft delete)

// ADD new methods:
suspend fun setFavorite(id: Int, isFavorite: Boolean): EmptyResult<DataError.Local>
suspend fun restore(id: Int): EmptyResult<DataError.Local>
```

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/repository/EntryRepository.kt`

```kotlin
// Add new method
fun getMoodColorEntryCounts(): Flow<Map<Int, Int>>
```

### 1.9 Update Repository Implementations

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/data/repository/MoodColorRepositoryImpl.kt`

```kotlin
// ADD new method implementations:

override suspend fun setFavorite(id: Int, isFavorite: Boolean): EmptyResult<DataError.Local> {
    return ErrorMapper.safeSuspendCall(TAG) {
        dao.updateFavorite(id, isFavorite, System.currentTimeMillis())
    }
}

override suspend fun restore(id: Int): EmptyResult<DataError.Local> {
    return ErrorMapper.safeSuspendCall(TAG) {
        dao.restore(id, System.currentTimeMillis())
    }
}
```

**Note:** `getMoodColorById`, `getMoodColorByName`, and `deleteMoodColor` already exist.

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/data/repository/EntryRepositoryImpl.kt`

```kotlin
override fun getMoodColorEntryCounts(): Flow<Map<Int, Int>> {
    return dao.getMoodColorEntryCounts()
        .map { counts -> counts.associate { it.moodColorId to it.count } }
}
```

### 1.10 Update Firestore Mapper

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/sync/data/mapper/FirestoreMapper.kt`

Upload mapping:
```kotlin
fun MoodColor.toFirestoreMap(): Map<String, Any?> = mapOf(
    // ... existing fields ...
    "isFavorite" to isFavorite,  // NEW
)
```

Download mapping:
```kotlin
fun DocumentSnapshot.toMoodColor(localId: Int? = null): MoodColor? {
    // ...
    isFavorite = getBoolean("isFavorite") ?: false,  // Defaults for old docs
    // ...
}
```

**Conflict Resolution:** Firestore uses last-write-wins with `updatedAt` timestamp. If two devices conflict:
- Most recent `updatedAt` wins during sync
- User sees their local state immediately (optimistic)
- Next sync resolves to server state

### 1.11 Update Default Seeds

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/moodcolor/SeedDefaultMoodColorsUseCase.kt`

Add explicit `isFavorite = false` to each seed for clarity.

---

## Phase 2: Domain UseCases

### 2.1 Create MoodColorError

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/model/MoodColorError.kt`

```kotlin
/**
 * Domain-level errors for mood color operations.
 * Does NOT expose data layer types (Clean Architecture).
 */
sealed interface MoodColorError : Error {
    data object BlankName : MoodColorError
    data object NameTooLong : MoodColorError
    data object InvalidColor : MoodColorError
    data object DuplicateName : MoodColorError
    data object NotFound : MoodColorError
    data object DatabaseError : MoodColorError  // Opaque - no inner cause exposed
}
```

### 2.2 Create ValidateMoodColorUseCase

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/moodcolor/ValidateMoodColorUseCase.kt`

```kotlin
class ValidateMoodColorUseCase(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(
        mood: String,
        color: String,
        excludeId: Int? = null
    ): Result<Unit, MoodColorError> {
        if (mood.isBlank()) {
            return Result.Error(MoodColorError.BlankName)
        }

        if (mood.length > InputValidation.MAX_MOOD_LENGTH) {
            return Result.Error(MoodColorError.NameTooLong)
        }

        if (!color.matches(MoodColorValidation.HEX_COLOR_REGEX)) {
            return Result.Error(MoodColorError.InvalidColor)
        }

        // getMoodColorByName already normalizes (trims + lowercase) internally
        val existing = repository.getMoodColorByName(mood)
        return when {
            existing is Result.Error -> Result.Error(MoodColorError.DatabaseError)
            existing is Result.Success && existing.data != null && existing.data.id != excludeId ->
                Result.Error(MoodColorError.DuplicateName)
            else -> Result.Success(Unit)
        }
    }
}
```

### 2.3 Create SaveMoodColorUseCase

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/moodcolor/SaveMoodColorUseCase.kt`

```kotlin
class SaveMoodColorUseCase(
    private val validate: ValidateMoodColorUseCase,
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(moodColor: MoodColor): Result<MoodColor, MoodColorError> {
        // Validate first
        val validation = validate(moodColor.mood, moodColor.color, moodColor.id)
        if (validation is Result.Error) {
            return Result.Error(validation.error)
        }

        // Insert or update
        val result = if (moodColor.id == null) {
            repository.insertMoodColor(moodColor)
        } else {
            repository.updateMoodColor(moodColor)
        }

        return when (result) {
            is Result.Success -> Result.Success(result.data)
            is Result.Error -> Result.Error(MoodColorError.DatabaseError)
        }
    }
}
```

### 2.4 Create DeleteMoodColorUseCase

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/moodcolor/DeleteMoodColorUseCase.kt`

```kotlin
class DeleteMoodColorUseCase(
    private val repository: MoodColorRepository
) {
    /**
     * Soft-deletes a mood color and returns it for undo support.
     */
    suspend operator fun invoke(id: Int): Result<MoodColor, MoodColorError> {
        // Get the item first (for undo)
        val getResult = repository.getMoodColorById(id)
        val moodColor = when (getResult) {
            is Result.Success -> getResult.data ?: return Result.Error(MoodColorError.NotFound)
            is Result.Error -> return Result.Error(MoodColorError.DatabaseError)
        }

        // Soft delete
        val deleteResult = repository.deleteMoodColor(id)
        if (deleteResult is Result.Error) {
            return Result.Error(MoodColorError.DatabaseError)
        }

        // Return deleted item for undo
        return Result.Success(moodColor)
    }
}
```

### 2.5 Create RestoreMoodColorUseCase

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/moodcolor/RestoreMoodColorUseCase.kt`

```kotlin
class RestoreMoodColorUseCase(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(id: Int): EmptyResult<MoodColorError> {
        return when (val result = repository.restore(id)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(MoodColorError.DatabaseError)
        }
    }
}
```

### 2.6 Create SetMoodColorFavoriteUseCase

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/moodcolor/SetMoodColorFavoriteUseCase.kt`

```kotlin
class SetMoodColorFavoriteUseCase(
    private val repository: MoodColorRepository
) {
    suspend operator fun invoke(id: Int, isFavorite: Boolean): EmptyResult<MoodColorError> {
        return when (val result = repository.setFavorite(id, isFavorite)) {
            is Result.Success -> Result.Success(Unit)
            is Result.Error -> Result.Error(MoodColorError.DatabaseError)
        }
    }
}
```

### 2.7 Create GetSortedMoodColorsUseCase

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/moodcolor/GetSortedMoodColorsUseCase.kt`

```kotlin
class GetSortedMoodColorsUseCase(
    private val moodColorRepository: MoodColorRepository,
    private val entryRepository: EntryRepository
) {
    /**
     * Returns mood colors sorted by: favorites first, then by usage count descending.
     * This is a Flow that automatically updates when data changes.
     */
    operator fun invoke(): Flow<List<MoodColorWithCount>> {
        return combine(
            moodColorRepository.getMoodColors(),
            entryRepository.getMoodColorEntryCounts()
        ) { moodColors, counts ->
            moodColors
                .map { mc -> MoodColorWithCount(mc, counts[mc.id] ?: 0) }
                .sortedByFavoriteAndUsage()  // Uses shared extension
        }
    }
}
```

### 2.8 Simplify GetMoodColorsUseCase

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/domain/usecases/shared/moodcolor/GetMoodColorsUseCase.kt`

Remove sort parameter - raw access only:
```kotlin
class GetMoodColorsUseCase(private val repository: MoodColorRepository) {
    operator fun invoke(): Flow<List<MoodColor>> = repository.getMoodColors()
}
```

---

## Phase 3: Shared UI Utilities

### 3.1 Create Optimistic Update Extensions

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/shared/moodcolor/OptimisticFavoriteExtensions.kt`

```kotlin
/**
 * Extension to apply optimistic favorite update.
 * Re-sorts list after update to maintain correct order.
 */
fun List<MoodColorWithCount>.withOptimisticFavorite(
    id: Int,
    newValue: Boolean
): List<MoodColorWithCount> = map { mc ->
    if (mc.moodColor.id == id) {
        mc.copy(moodColor = mc.moodColor.copy(isFavorite = newValue))
    } else mc
}.sortedByFavoriteAndUsage()

/**
 * Extension to revert optimistic update using tracked original value.
 */
fun List<MoodColorWithCount>.revertOptimisticFavorite(
    id: Int,
    originalValue: Boolean
): List<MoodColorWithCount> = map { mc ->
    if (mc.moodColor.id == id) {
        mc.copy(moodColor = mc.moodColor.copy(isFavorite = originalValue))
    } else mc
}.sortedByFavoriteAndUsage()
```

### 3.2 Create MoodColorEvent

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/shared/moodcolor/MoodColorEvent.kt`

```kotlin
sealed interface MoodColorEvent {
    data object ShowUndoSnackbar : MoodColorEvent
    data class ShowError(val error: MoodColorError) : MoodColorEvent
}
```

---

## Phase 4: Shared UI Components

### Animation Strategy

When favorites are toggled, items may jump positions in the list. To make this smooth:

1. **LazyColumn `animateItem()`** - Basic position animation
2. **Delayed reordering** - Wait for star animation to complete before reordering
3. **Visual feedback** - Star fills/unfills with scale animation

### 4.1 Create AnimatedFavoriteIcon

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/shared/moodcolor/AnimatedFavoriteIcon.kt`

```kotlin
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.FAVORITE_ANIMATION_DURATION_MS
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.ICON_SCALE_FAVORITE
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.ICON_SCALE_UNFAVORITE

@Composable
fun AnimatedFavoriteIcon(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isFavorite) ICON_SCALE_FAVORITE else ICON_SCALE_UNFAVORITE,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "favorite_scale"
    )

    val tint by animateColorAsState(
        targetValue = if (isFavorite)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = FAVORITE_ANIMATION_DURATION_MS),
        label = "favorite_tint"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(
            imageVector = if (isFavorite)
                Icons.Default.Star
            else
                Icons.Outlined.StarBorder,
            contentDescription = stringResource(
                if (isFavorite) R.string.remove_from_favorites
                else R.string.add_to_favorites
            ),
            tint = tint,
            modifier = Modifier.scale(scale)
        )
    }
}
```

### 4.2 Create EditableColorCircle

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/shared/moodcolor/EditableColorCircle.kt`

```kotlin
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.BORDER_ALPHA
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.COLOR_CIRCLE_SIZE_SMALL
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.EDIT_ICON_ALPHA_ON_DARK
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.EDIT_ICON_ALPHA_ON_LIGHT
import uk.co.zlurgg.thedayto.journal.ui.shared.moodcolor.MoodColorConstants.LUMINANCE_THRESHOLD

@Composable
fun EditableColorCircle(
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = COLOR_CIRCLE_SIZE_SMALL
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = BORDER_ALPHA), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = if (color.luminance() > LUMINANCE_THRESHOLD)
                Color.Black.copy(alpha = EDIT_ICON_ALPHA_ON_LIGHT)
            else
                Color.White.copy(alpha = EDIT_ICON_ALPHA_ON_DARK),
            modifier = Modifier.size(size * 0.5f)
        )
    }
}
```

### 4.3 Create MoodColorRow

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/shared/moodcolor/MoodColorRow.kt`

```kotlin
@Composable
fun MoodColorRow(
    moodColorWithCount: MoodColorWithCount,
    onToggleFavorite: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    showEntryCount: Boolean = false,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val moodColor = moodColorWithCount.moodColor
    val color = remember(moodColor.color) {
        try {
            Color("#${moodColor.color}".toColorInt())
        } catch (_: Exception) {
            Color.Gray
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = paddingMedium, vertical = paddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animated star toggle (favorite)
        AnimatedFavoriteIcon(
            isFavorite = moodColor.isFavorite,
            onClick = onToggleFavorite
        )

        // Mood name and optional entry count
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = moodColor.mood,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (showEntryCount) {
                Text(
                    text = pluralStringResource(
                        R.plurals.entry_count,
                        moodColorWithCount.entryCount,
                        moodColorWithCount.entryCount
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Optional trailing content (for variants)
        trailingContent?.invoke()

        Spacer(modifier = Modifier.width(paddingSmall))

        // Color circle with edit icon
        EditableColorCircle(
            color = color,
            onClick = onEdit,
            size = if (showEntryCount) COLOR_CIRCLE_SIZE_LARGE else COLOR_CIRCLE_SIZE_SMALL
        )
    }
}
```

---

## Phase 5: Management Screen Updates

### 5.1 Create MoodColorManagementState

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/moodcolormanagement/state/MoodColorManagementState.kt`

```kotlin
data class MoodColorManagementState(
    val moodColors: List<MoodColorWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val editingMoodColor: MoodColor? = null,
    val dialogError: MoodColorError? = null,
    val pendingDelete: MoodColor? = null,  // Most recent pending delete for snackbar
    val pendingFavorites: Map<Int, Boolean> = emptyMap()  // id -> original value
)
```

### 5.2 Create MoodColorManagementAction

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/moodcolormanagement/state/MoodColorManagementAction.kt`

```kotlin
sealed interface MoodColorManagementAction {
    data object AddMoodColor : MoodColorManagementAction
    data class EditMoodColor(val moodColor: MoodColor) : MoodColorManagementAction
    data class SaveMoodColor(val moodColor: MoodColor) : MoodColorManagementAction
    data class DeleteMoodColor(val moodColor: MoodColor) : MoodColorManagementAction
    data object UndoDelete : MoodColorManagementAction
    data object ClearPendingDelete : MoodColorManagementAction
    data class ToggleFavorite(val id: Int, val currentValue: Boolean) : MoodColorManagementAction
    data object DismissDialog : MoodColorManagementAction
    data object ClearError : MoodColorManagementAction
}
```

### 5.3 Update MoodColorManagementViewModel

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/moodcolormanagement/MoodColorManagementViewModel.kt`

```kotlin
class MoodColorManagementViewModel(
    private val getSortedMoodColors: GetSortedMoodColorsUseCase,
    private val saveMoodColor: SaveMoodColorUseCase,
    private val deleteMoodColor: DeleteMoodColorUseCase,
    private val restoreMoodColor: RestoreMoodColorUseCase,
    private val setFavorite: SetMoodColorFavoriteUseCase,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(MoodColorManagementState())
    val state: StateFlow<MoodColorManagementState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<MoodColorEvent>()
    val events: SharedFlow<MoodColorEvent> = _events.asSharedFlow()

    init {
        // Observe sorted mood colors - Room Flow auto-updates on any write
        getSortedMoodColors()
            .onStart { _state.update { it.copy(isLoading = true) } }
            .onEach { sorted ->
                _state.update { it.copy(moodColors = sorted, isLoading = false) }
            }
            .catch { /* Log error */ }
            .launchIn(viewModelScope)
    }

    fun onAction(action: MoodColorManagementAction) {
        when (action) {
            is MoodColorManagementAction.AddMoodColor -> showAddDialog()
            is MoodColorManagementAction.EditMoodColor -> showEditDialog(action.moodColor)
            is MoodColorManagementAction.SaveMoodColor -> save(action.moodColor)
            is MoodColorManagementAction.DeleteMoodColor -> delete(action.moodColor)
            is MoodColorManagementAction.UndoDelete -> undoDelete()
            is MoodColorManagementAction.ClearPendingDelete -> clearPendingDelete()
            is MoodColorManagementAction.ToggleFavorite ->
                toggleFavorite(action.id, action.currentValue)
            is MoodColorManagementAction.DismissDialog -> dismissDialog()
            is MoodColorManagementAction.ClearError -> clearError()
        }
    }

    private fun showAddDialog() {
        _state.update { it.copy(editingMoodColor = MoodColor.empty()) }
    }

    private fun showEditDialog(moodColor: MoodColor) {
        _state.update { it.copy(editingMoodColor = moodColor) }
    }

    private fun dismissDialog() {
        _state.update { it.copy(editingMoodColor = null, dialogError = null) }
    }

    private fun clearError() {
        _state.update { it.copy(dialogError = null) }
    }

    private fun save(moodColor: MoodColor) {
        viewModelScope.launch {
            when (val result = saveMoodColor(moodColor)) {
                is Result.Success -> {
                    _state.update { it.copy(editingMoodColor = null, dialogError = null) }
                    syncManager.requestImmediateSync()
                }
                is Result.Error -> {
                    _state.update { it.copy(dialogError = result.error) }
                }
            }
        }
    }

    private fun delete(moodColor: MoodColor) {
        val id = moodColor.id ?: return
        viewModelScope.launch {
            when (val result = deleteMoodColor(id)) {
                is Result.Success -> {
                    _state.update { it.copy(pendingDelete = result.data) }
                    _events.emit(MoodColorEvent.ShowUndoSnackbar)
                    syncManager.requestImmediateSync()
                }
                is Result.Error -> {
                    _events.emit(MoodColorEvent.ShowError(result.error))
                }
            }
        }
    }

    private fun undoDelete() {
        val toRestore = _state.value.pendingDelete ?: return
        val id = toRestore.id ?: return
        viewModelScope.launch {
            when (val result = restoreMoodColor(id)) {
                is Result.Success -> {
                    _state.update { it.copy(pendingDelete = null) }
                    syncManager.requestImmediateSync()
                }
                is Result.Error -> {
                    _events.emit(MoodColorEvent.ShowError(result.error))
                }
            }
        }
    }

    private fun clearPendingDelete() {
        _state.update { it.copy(pendingDelete = null) }
    }

    private fun toggleFavorite(id: Int, currentValue: Boolean) {
        val newValue = !currentValue

        // Store original value for potential rollback
        // Don't overwrite if already pending (preserves true original)
        _state.update { state ->
            state.copy(
                moodColors = state.moodColors.withOptimisticFavorite(id, newValue),
                pendingFavorites = if (id in state.pendingFavorites) {
                    state.pendingFavorites  // Keep true original
                } else {
                    state.pendingFavorites + (id to currentValue)
                }
            )
        }

        viewModelScope.launch {
            // Delay reordering for smooth animation
            delay(MoodColorConstants.REORDER_DELAY_MS)

            when (val result = setFavorite(id, newValue)) {
                is Result.Success -> {
                    _state.update { it.copy(
                        pendingFavorites = it.pendingFavorites - id
                    ) }
                    syncManager.requestImmediateSync()
                }
                is Result.Error -> {
                    // Revert using stored original value
                    val originalValue = _state.value.pendingFavorites[id] ?: currentValue
                    _state.update { state ->
                        state.copy(
                            moodColors = state.moodColors.revertOptimisticFavorite(id, originalValue),
                            pendingFavorites = state.pendingFavorites - id
                        )
                    }
                    _events.emit(MoodColorEvent.ShowError(result.error))
                }
            }
        }
    }
}
```

### 5.4 Update MoodColorManagementScreen

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/moodcolormanagement/MoodColorManagementScreen.kt`

**Remove:**
- `MoodColorSortSection` component
- Old `MoodColorCard` implementation
- Sort-related state observation

**Add swipe-to-delete with shared row:**

```kotlin
@Composable
fun MoodColorManagementScreen(
    viewModel: MoodColorManagementViewModel = koinViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is MoodColorEvent.ShowUndoSnackbar -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Mood color deleted",
                        actionLabel = "Undo",
                        duration = SnackbarDuration.Short
                    )
                    when (result) {
                        SnackbarResult.ActionPerformed ->
                            viewModel.onAction(MoodColorManagementAction.UndoDelete)
                        SnackbarResult.Dismissed ->
                            viewModel.onAction(MoodColorManagementAction.ClearPendingDelete)
                    }
                }
                is MoodColorEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = ErrorFormatter.format(event.error)
                    )
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { /* ... */ },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAction(MoodColorManagementAction.AddMoodColor) }
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_mood_color))
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(vertical = paddingMedium)
        ) {
            items(
                items = state.moodColors,
                key = { it.moodColor.id ?: 0 }
            ) { moodColorWithCount ->
                SwipeToDeleteMoodColorCard(
                    moodColorWithCount = moodColorWithCount,
                    onEdit = {
                        viewModel.onAction(MoodColorManagementAction.EditMoodColor(moodColorWithCount.moodColor))
                    },
                    onDelete = {
                        viewModel.onAction(MoodColorManagementAction.DeleteMoodColor(moodColorWithCount.moodColor))
                    },
                    onToggleFavorite = {
                        moodColorWithCount.moodColor.id?.let { id ->
                            viewModel.onAction(MoodColorManagementAction.ToggleFavorite(
                                id = id,
                                currentValue = moodColorWithCount.moodColor.isFavorite
                            ))
                        }
                    },
                    isDeleteEnabled = !state.isLoading,
                    pendingDelete = state.pendingDelete,
                    modifier = Modifier.animateItem()
                )
            }
        }
    }

    // Edit dialog
    state.editingMoodColor?.let { moodColor ->
        EditMoodColorDialog(
            moodColor = moodColor,
            onSave = { viewModel.onAction(MoodColorManagementAction.SaveMoodColor(it)) },
            onDismiss = { viewModel.onAction(MoodColorManagementAction.DismissDialog) },
            error = state.dialogError
        )
    }
}

@Composable
private fun SwipeToDeleteMoodColorCard(
    moodColorWithCount: MoodColorWithCount,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit,
    isDeleteEnabled: Boolean,
    pendingDelete: MoodColor?,
    modifier: Modifier = Modifier
) {
    val dismissState = rememberSwipeToDismissBoxState()

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onDelete()
        }
    }

    // Reset if undo
    LaunchedEffect(pendingDelete) {
        if (pendingDelete == null &&
            dismissState.currentValue == SwipeToDismissBoxValue.EndToStart
        ) {
            dismissState.reset()
        }
    }

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(end = paddingMedium)
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = isDeleteEnabled,
        modifier = modifier
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            MoodColorRow(
                moodColorWithCount = moodColorWithCount,
                onToggleFavorite = onToggleFavorite,
                onEdit = onEdit,
                showEntryCount = true
            )
        }
    }
}
```

---

## Phase 6: Editor Screen Updates

### 6.1 Create EditorMoodColorState

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/editor/state/EditorMoodColorState.kt`

```kotlin
data class EditorMoodColorState(
    val moodColors: List<MoodColorWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val editingMoodColor: MoodColor? = null,
    val dialogError: MoodColorError? = null,
    val pendingFavorites: Map<Int, Boolean> = emptyMap()
)
```

### 6.2 Create MoodColorAction (Editor-specific)

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/editor/state/MoodColorAction.kt`

```kotlin
sealed interface MoodColorAction {
    data class Edit(val moodColor: MoodColor) : MoodColorAction
    data class Save(val moodColor: MoodColor) : MoodColorAction
    data class ToggleFavorite(val id: Int, val currentValue: Boolean) : MoodColorAction
    data object DismissDialog : MoodColorAction
}
```

### 6.3 Update EditorViewModel

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/editor/EditorViewModel.kt`

Add mood color handling (similar pattern to Management, but no delete):

```kotlin
class EditorViewModel(
    // ... existing deps ...
    private val getSortedMoodColors: GetSortedMoodColorsUseCase,
    private val saveMoodColor: SaveMoodColorUseCase,
    private val setFavorite: SetMoodColorFavoriteUseCase,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _moodColorState = MutableStateFlow(EditorMoodColorState())
    val moodColorState: StateFlow<EditorMoodColorState> = _moodColorState.asStateFlow()

    private val _moodColorEvents = MutableSharedFlow<MoodColorEvent>()
    val moodColorEvents: SharedFlow<MoodColorEvent> = _moodColorEvents.asSharedFlow()

    init {
        // ... existing init ...

        // Observe mood colors - same Flow, independent collection
        getSortedMoodColors()
            .onStart { _moodColorState.update { it.copy(isLoading = true) } }
            .onEach { sorted ->
                _moodColorState.update { it.copy(moodColors = sorted, isLoading = false) }
            }
            .catch { /* Log error */ }
            .launchIn(viewModelScope)
    }

    fun onMoodColorAction(action: MoodColorAction) {
        when (action) {
            is MoodColorAction.Edit -> showEditDialog(action.moodColor)
            is MoodColorAction.Save -> saveMoodColorAction(action.moodColor)
            is MoodColorAction.ToggleFavorite ->
                toggleFavorite(action.id, action.currentValue)
            is MoodColorAction.DismissDialog -> dismissMoodColorDialog()
        }
    }

    private fun showEditDialog(moodColor: MoodColor) {
        _moodColorState.update { it.copy(editingMoodColor = moodColor) }
    }

    private fun dismissMoodColorDialog() {
        _moodColorState.update { it.copy(editingMoodColor = null, dialogError = null) }
    }

    private fun saveMoodColorAction(moodColor: MoodColor) {
        viewModelScope.launch {
            when (val result = saveMoodColor(moodColor)) {
                is Result.Success -> {
                    _moodColorState.update { it.copy(editingMoodColor = null, dialogError = null) }
                    syncManager.requestImmediateSync()
                }
                is Result.Error -> {
                    _moodColorState.update { it.copy(dialogError = result.error) }
                }
            }
        }
    }

    private fun toggleFavorite(id: Int, currentValue: Boolean) {
        val newValue = !currentValue

        // Store original value for potential rollback
        // Don't overwrite if already pending (preserves true original)
        _moodColorState.update { state ->
            state.copy(
                moodColors = state.moodColors.withOptimisticFavorite(id, newValue),
                pendingFavorites = if (id in state.pendingFavorites) {
                    state.pendingFavorites  // Keep true original
                } else {
                    state.pendingFavorites + (id to currentValue)
                }
            )
        }

        viewModelScope.launch {
            delay(MoodColorConstants.REORDER_DELAY_MS)

            when (val result = setFavorite(id, newValue)) {
                is Result.Success -> {
                    _moodColorState.update { it.copy(
                        pendingFavorites = it.pendingFavorites - id
                    ) }
                    syncManager.requestImmediateSync()
                }
                is Result.Error -> {
                    val originalValue = _moodColorState.value.pendingFavorites[id] ?: currentValue
                    _moodColorState.update { state ->
                        state.copy(
                            moodColors = state.moodColors.revertOptimisticFavorite(id, originalValue),
                            pendingFavorites = state.pendingFavorites - id
                        )
                    }
                    _moodColorEvents.emit(MoodColorEvent.ShowError(result.error))
                }
            }
        }
    }
}
```

### 6.4 Update MoodItem (Dropdown)

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/ui/editor/components/MoodItem.kt`

```kotlin
@Composable
fun MoodItem(
    moodColors: List<MoodColorWithCount>,
    selectedMoodColorId: Int?,
    onMoodSelected: (Int) -> Unit,
    onEdit: (MoodColor) -> Unit,
    onToggleFavorite: (Int, Boolean) -> Unit,  // id, currentValue
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // ... existing dropdown trigger ...

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        moodColors.forEach { moodColorWithCount ->
            val moodColor = moodColorWithCount.moodColor
            val color = remember(moodColor.color) {
                try {
                    Color("#${moodColor.color}".toColorInt())
                } catch (_: Exception) {
                    Color.Gray
                }
            }

            DropdownMenuItem(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    moodColor.id?.let { onMoodSelected(it) }
                    expanded = false
                },
                text = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Star toggle
                        IconButton(
                            onClick = {
                                moodColor.id?.let { id ->
                                    onToggleFavorite(id, moodColor.isFavorite)
                                }
                            },
                            modifier = Modifier.size(ICON_BUTTON_SIZE)
                        ) {
                            Icon(
                                imageVector = if (moodColor.isFavorite)
                                    Icons.Default.Star
                                else
                                    Icons.Outlined.StarBorder,
                                contentDescription = stringResource(
                                    if (moodColor.isFavorite) R.string.remove_from_favorites
                                    else R.string.add_to_favorites
                                ),
                                tint = if (moodColor.isFavorite)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Mood name
                        Text(
                            text = moodColor.mood,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Color circle with edit
                        EditableColorCircle(
                            color = color,
                            onClick = {
                                onEdit(moodColor)
                                expanded = false
                            },
                            size = COLOR_CIRCLE_SIZE_SMALL
                        )
                    }
                }
            )
        }
    }
}
```

---

## Phase 7: Dependency Injection

### 7.1 Update JournalModule

**File:** `app/src/main/java/uk/co/zlurgg/thedayto/journal/di/JournalModule.kt`

```kotlin
// Domain UseCases
factory { ValidateMoodColorUseCase(get()) }
factory { SaveMoodColorUseCase(get(), get()) }
factory { DeleteMoodColorUseCase(get()) }
factory { RestoreMoodColorUseCase(get()) }
factory { SetMoodColorFavoriteUseCase(get()) }
factory { GetSortedMoodColorsUseCase(get(), get()) }

// ViewModels - each gets its own UseCase instances
// No shared handler - Room Flow handles cross-screen updates
viewModel {
    MoodColorManagementViewModel(
        getSortedMoodColors = get(),
        saveMoodColor = get(),
        deleteMoodColor = get(),
        restoreMoodColor = get(),
        setFavorite = get(),
        syncManager = get()
    )
}

viewModel {
    EditorViewModel(
        // ... existing deps ...
        getSortedMoodColors = get(),
        saveMoodColor = get(),
        setFavorite = get(),
        syncManager = get()
    )
}
```

**Why no shared handler?**

Both ViewModels observe `GetSortedMoodColorsUseCase()` which returns a Room Flow. When either screen writes to the database:
1. Room detects the change
2. Room emits new values to ALL active Flow collectors
3. Both screens update automatically

This is simpler than a singleton and avoids:
- Lifecycle management issues
- Dialog state collision
- Scope ownership confusion

---

## Phase 8: Cleanup

### 8.1 Delete Unused Files

| File | Reason |
|------|--------|
| `MoodColorSortSection.kt` | Sort UI removed |
| `MoodColorOrder.kt` | Sort enum removed |
| `MoodColorActionDelegate.kt` | Replaced by ViewModel logic |
| `MoodColorManagementUiState.kt` | Replaced by new state |

### 8.2 Update String Resources

**File:** `app/src/main/res/values/strings.xml`

```xml
<string name="add_to_favorites">Add to favorites</string>
<string name="remove_from_favorites">Remove from favorites</string>
<string name="mood_color_deleted">Mood color deleted</string>
<string name="error_blank_name">Name cannot be empty</string>
<string name="error_name_too_long">Name must be 50 characters or less</string>
<string name="error_duplicate_name">A mood with this name already exists</string>
<string name="error_invalid_color">Invalid color format</string>
<string name="error_database">Something went wrong. Please try again.</string>

<plurals name="entry_count">
    <item quantity="one">%d entry</item>
    <item quantity="other">%d entries</item>
</plurals>
```

### 8.3 Remove MoodColorOrder References

Search and remove any remaining references to `MoodColorOrder` in:
- Actions
- States
- Tests

---

## Phase 9: Testing

### 9.1 New Unit Tests

| Test File | Coverage |
|-----------|----------|
| `ValidateMoodColorUseCaseTest.kt` | Blank name, duplicate, invalid color, max length |
| `SaveMoodColorUseCaseTest.kt` | Insert, update, validation failure |
| `DeleteMoodColorUseCaseTest.kt` | Success returns item, not found error |
| `RestoreMoodColorUseCaseTest.kt` | Restore success, error handling |
| `SetMoodColorFavoriteUseCaseTest.kt` | Set true/false, error handling |
| `GetSortedMoodColorsUseCaseTest.kt` | Sort order: favorites first, then by count |
| `MoodColorManagementViewModelTest.kt` | State transitions, events, optimistic updates with correct rollback |

### 9.2 Update Existing Tests

| Test File | Changes |
|-----------|---------|
| `FakeMoodColorRepository.kt` | Add `setFavorite`, `restore` |
| `FakeEntryRepository.kt` | Add `getMoodColorEntryCounts` |
| `GetMoodColorsUseCaseTest.kt` | Remove sort parameter tests |
| `SeedDefaultMoodColorsUseCaseTest.kt` | Assert `isFavorite = false` |

### 9.3 Delete Obsolete Tests

- Any tests for `MoodColorOrder`
- `MoodColorActionDelegate` tests (if any)

### 9.4 Manual Test Plan

**Core Functionality**
- [ ] Phase 0: Edit dialog allows changing both name and color
- [ ] Phase 0: Add dialog resets color picker when reopened
- [ ] Toggle favorite in management - star fills/unfills
- [ ] Toggle favorite in editor dropdown - same behavior
- [ ] Favorites appear at top of both lists
- [ ] Within favorites, higher usage sorts first
- [ ] Within non-favorites, higher usage sorts first
- [ ] Swipe-to-delete with undo in management
- [ ] No delete option in editor dropdown
- [ ] Tap color circle opens edit dialog
- [ ] Edit icon visible with proper contrast
- [ ] Validation errors show in dialog
- [ ] Duplicate name rejected
- [ ] Blank name rejected
- [ ] Name over 50 chars rejected
- [ ] Sync works for favorite changes
- [ ] New mood appears at bottom (not favorite, 0 entries)

**Cross-Screen Updates (Room Flow)**
- [ ] Add mood in management → appears in editor dropdown immediately
- [ ] Edit mood in management → changes visible in editor dropdown
- [ ] Delete mood in management → removed from editor dropdown
- [ ] Toggle favorite in management → editor dropdown reorders
- [ ] Toggle favorite in editor → management screen reorders (when navigating back)

**Animation**
- [ ] Star icon animates (scale bounce) when toggling favorite
- [ ] Star color transitions smoothly (not instant)
- [ ] List items animate to new positions when reordering
- [ ] ~200ms delay before reorder for smooth animation
- [ ] No jarring jumps when favoriting/unfavoriting

**Offline & Optimistic Updates**
- [ ] Toggle favorite responds instantly (no loading delay)
- [ ] Enable airplane mode → toggle favorite → UI updates immediately
- [ ] Disable airplane mode → change syncs to Firestore
- [ ] Rapid toggle (tap star 5x quickly) → settles on correct final state
- [ ] Simulate DB failure → UI reverts to ORIGINAL state, error snackbar shown

**Edge Case: Rapid Delete**
- [ ] Swipe delete A → immediately swipe delete B → tap Undo → B restored
- [ ] A remains deleted (expected - only most recent is undoable)

---

## Files Changed Summary

| Layer | File | Change |
|-------|------|--------|
| **Data** | `MoodColorEntity.kt` | Add `isFavorite` |
| **Data** | `MoodColorDao.kt` | Add `updateFavorite`, `restore` |
| **Data** | `MoodColorEntryCount.kt` | **NEW** |
| **Data** | `EntryDao.kt` | Add `getMoodColorEntryCounts` |
| **Data** | `MoodColorMapper.kt` | Map `isFavorite` |
| **Data** | `MoodColorRepositoryImpl.kt` | Implement new methods |
| **Data** | `EntryRepositoryImpl.kt` | Implement `getMoodColorEntryCounts` |
| **Domain** | `MoodColor.kt` | Add `isFavorite`, `empty()` factory |
| **Domain** | `MoodColorWithCount.kt` | **NEW** |
| **Domain** | `MoodColorSorting.kt` | **NEW** - DRY sorting extension |
| **Domain** | `MoodColorError.kt` | **NEW** - no data layer leakage |
| **Domain** | `MoodColorRepository.kt` | Add new method signatures |
| **Domain** | `EntryRepository.kt` | Add `getMoodColorEntryCounts` |
| **Domain** | `MoodColorValidation.kt` | **NEW** - validation constants |
| **Domain** | `ValidateMoodColorUseCase.kt` | **NEW** |
| **Domain** | `SaveMoodColorUseCase.kt` | **NEW** |
| **Domain** | `DeleteMoodColorUseCase.kt` | **NEW** |
| **Domain** | `RestoreMoodColorUseCase.kt` | **NEW** |
| **Domain** | `SetMoodColorFavoriteUseCase.kt` | **NEW** |
| **Domain** | `GetSortedMoodColorsUseCase.kt` | **NEW** |
| **Domain** | `GetMoodColorsUseCase.kt` | Remove sort parameter |
| **Domain** | `SeedDefaultMoodColorsUseCase.kt` | Add `isFavorite = false` |
| **UI Shared** | `MoodColorConstants.kt` | **NEW** - extracted magic numbers |
| **UI Shared** | `OptimisticFavoriteExtensions.kt` | **NEW** - optimistic update helpers |
| **UI Shared** | `MoodColorEvent.kt` | **NEW** |
| **UI Shared** | `AnimatedFavoriteIcon.kt` | **NEW** |
| **UI Shared** | `EditableColorCircle.kt` | **NEW** |
| **UI Shared** | `MoodColorRow.kt` | **NEW** |
| **UI Mgmt** | `MoodColorManagementState.kt` | **NEW** |
| **UI Mgmt** | `MoodColorManagementViewModel.kt` | Rewritten - no shared handler |
| **UI Mgmt** | `MoodColorManagementAction.kt` | Updated - passes currentValue |
| **UI Mgmt** | `MoodColorManagementScreen.kt` | Use shared components |
| **UI Editor** | `EditorMoodColorState.kt` | **NEW** |
| **UI Editor** | `EditorViewModel.kt` | Add mood color handling |
| **UI Editor** | `MoodColorAction.kt` | **NEW** |
| **UI Editor** | `MoodItem.kt` | New layout |
| **UI Editor** | `EditMoodColorDialog.kt` | Bug fix - composite key |
| **Sync** | `FirestoreMapper.kt` | Map `isFavorite` |
| **DI** | `JournalModule.kt` | Register new classes |
| **Res** | `strings.xml` | Add strings |
| **DELETE** | `MoodColorSortSection.kt` | Unused |
| **DELETE** | `MoodColorOrder.kt` | Unused |
| **DELETE** | `MoodColorActionDelegate.kt` | Replaced |
| **DELETE** | `MoodColorManagementUiState.kt` | Replaced |

---

## Offline Behavior

### Optimistic Updates with Proper Rollback

For responsive UX, favorite toggles use optimistic updates:

1. **User taps star** → Store original value, UI immediately updates
2. **Wait 200ms** → Smooth animation before reorder
3. **Database write happens in background**
4. **On success** → Clear pending state, trigger sync
5. **On failure** → Revert UI to ORIGINAL state (not just flipped), show error

```
User taps ★ → [Store original=false] → [Optimistic: true] → [DB write]
                                              ↓
                                    [Success: clear pending]
                                    [Failure: revert to false]
```

### State Tracking

`pendingFavorites: Map<Int, Boolean>` tracks **original values** before optimistic updates:
- Key: mood color ID
- Value: original `isFavorite` value BEFORE the first change
- On failure, revert to this stored value (not just flip current)
- **Key invariant:** Don't overwrite if already pending (preserves true original)

This handles rapid toggles correctly:
1. OFF→ON (store original=false, UI shows true)
2. ON→OFF (already pending, keep original=false, UI shows false)
3. First call fails → revert to original=false ✓
4. Second call fails → revert to original=false ✓

### Offline Scenarios

| Scenario | Behavior |
|----------|----------|
| Toggle favorite while offline | Optimistic update shown, DB write succeeds (local), sync queued |
| Toggle favorite, then go offline | Same as above - local DB is source of truth |
| Rapid toggles offline | Each toggle updates local DB, last state wins |
| App killed during pending toggle | Local DB has the state, sync picks up on next launch |

---

## Edge Cases

| Scenario | Handling |
|----------|----------|
| All moods are favorites | Sort by usage count only |
| No moods are favorites | Sort by usage count (default) |
| New mood created | Not favorite, 0 entries → bottom |
| Mood with 0 entries | Bottom of its group |
| Toggle favorite during sync | `updatedAt` triggers sync |
| Old Firestore docs | `isFavorite` defaults to `false` |
| Duplicate name (different case) | Normalized comparison rejects |
| Rapid favorite toggles | Preserves first original value, correct rollback |
| Edit while pending delete | Dialog still works |
| Network error on save | Error shown, dialog stays open |
| Toggle while offline | Optimistic UI, local DB updated, syncs when online |
| DB write fails | Revert to stored original value, show error snackbar |
| Rapid delete A then B | Only B is undoable; A auto-finalizes |
| Add dialog opened twice | LaunchedEffect re-runs (timestamp key) |
| Name > 50 chars | Validation rejects |
| Firestore conflict | Last-write-wins via `updatedAt` |

---

## Implementation Order

1. **Phase 0** - Bug fix (quick win)
2. **Phase 1** - Data layer foundation (includes missing methods)
3. **Phase 2** - Domain UseCases (enables testing)
4. **Phase 3** - UI State classes (no shared handler)
5. **Phase 4** - Shared components
6. **Phase 5** - Management screen
7. **Phase 6** - Editor screen (same pattern)
8. **Phase 7** - DI wiring
9. **Phase 8** - Cleanup dead code
10. **Phase 9** - Testing
