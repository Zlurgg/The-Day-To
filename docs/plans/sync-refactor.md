# Sync Refactor Plan

## Problem Statement

The current sync implementation has fundamental issues causing data loss and duplication:
1. Upload happens before download, overwriting user data in Firestore
2. Seeded mood colors overwrite user edits on reinstall
3. No automatic sync on app startup or operations
4. Duplicate entries possible (no unique constraint per day)

## Goals

- Reliable bidirectional sync that never loses user data
- Seamless multi-device experience
- Simple, maintainable code following project standards

---

## Changes

### 1. Reorder Sync Phases

**Current (wrong):**
```
Phase 1: Upload mood colors  ← Overwrites remote!
Phase 2: Upload entries
Phase 3: Download mood colors
Phase 4: Download entries
```

**Proposed:**
```
Phase 1: Download mood colors (merge, don't overwrite pending local changes)
Phase 2: Download entries (merge, don't overwrite pending local changes)
Phase 3: Upload mood colors (only modified, skip seeds)
Phase 4: Upload entries
```

**Files:** `SyncRepositoryImpl.performFullSync()`

**Critical: Conflict Resolution Must Preserve Pending Local Changes**

The current `resolveEntryConflict` uses `remoteTime >= localTime`, which loses offline edits:
1. User edits offline (localTime = X, syncStatus = PENDING_SYNC)
2. Another device syncs (remoteTime = X+1)
3. Offline device comes online, downloads → remote wins → local edit lost

**Fix:** During download, skip overwriting items with `syncStatus = PENDING_SYNC`:
```kotlin
fun resolveEntryConflict(local: Entry, remote: Entry): Entry {
    // Never overwrite pending local changes during download
    // Let upload phase handle the conflict
    if (local.syncStatus == SyncStatus.PENDING_SYNC) {
        return local
    }

    val localTime = local.updatedAt ?: 0L
    val remoteTime = remote.updatedAt ?: 0L

    return if (remoteTime >= localTime) {
        remote.copy(id = local.id)
    } else {
        local.copy(syncStatus = SyncStatus.PENDING_SYNC)
    }
}
```

This ensures:
- Offline edits preserved through download phase
- Upload phase sends them to Firestore
- Last-write-wins applies at Firestore level

**Edge cases:**
- Network fails mid-sync → WorkManager retries, no partial state issues
- Conflict during download with pending local → local preserved, uploaded next
- True conflict (both edited same item) → last-write-wins at upload time

---

### 2. Skip Seed Uploads

**Change:** Don't upload mood colors with `updatedAt <= 0`

**Option A - Filter in upload method:**
```kotlin
// SyncRepositoryImpl.uploadPendingMoodColors()
moodColors.filter { it.updatedAt ?: 0L > 0L }.forEach { ... }
```

**Option B - Filter in DAO query:**
```kotlin
// MoodColorDao
@Query("SELECT * FROM mood_color WHERE syncStatus IN ('PENDING_SYNC', 'PENDING_DELETE') AND (updatedAt IS NULL OR updatedAt > 0)")
suspend fun getMoodColorsPendingSync(): List<MoodColorEntity>
```

**Recommendation:** Option B - single responsibility, query returns only what should sync.

**Edge cases:**
- User edits seeded mood → `updatedAt` becomes current time → uploads correctly
- Seeded mood never edited → stays local, downloaded version wins on other devices

---

### 3. Fix Seeding Race Condition

**Current (wrong):**
```kotlin
// SyncSettingsViewModel.handleSignInSuccess()
seedDefaultMoodColorsUseCase()      // Not awaited!
markLocalDataForSync()               // Runs before seeding completes
```

**Proposed:**
```kotlin
// Seeding already called in Application.onCreate() - remove from here
// OR ensure proper await order
```

**Recommendation:** Remove `seedDefaultMoodColorsUseCase()` from `handleSignInSuccess()` entirely. Seeding happens in `Application.onCreate()` on first launch - no need to call again.

**Files:** `SyncSettingsViewModel.handleSignInSuccess()`

---

### 4. Sync on App Startup

**Trigger sync when app starts if user is signed in.**

**Options:**

**A) In Application.onCreate():**
```kotlin
// TheDayToApplication.onCreate()
if (authRepository.getSignedInUser() != null) {
    syncScheduler.requestImmediateSync()
}
```
- Simple but runs before UI ready
- Koin may not be fully initialized

**B) In MainActivity.onCreate():**
```kotlin
// MainActivity.onCreate()
lifecycleScope.launch {
    if (authRepository.getSignedInUser() != null) {
        syncScheduler.requestImmediateSync()
    }
}
```
- Runs after DI ready
- Only triggers when user opens app

**C) In OverviewViewModel.init():**
```kotlin
// OverviewViewModel.init()
viewModelScope.launch {
    if (authRepository.getSignedInUser() != null) {
        syncScheduler.requestImmediateSync()
    }
}
```
- Cleanest - ViewModel is the right layer for this
- Main screen triggers sync

**Recommendation:** Option C - follows existing patterns, ViewModel already has access to auth state.

**Edge cases:**
- User not signed in → no sync triggered
- Network unavailable → WorkManager queues and retries when available

---

### 5. Sync on Operations

**Trigger sync after user creates/edits/deletes entries or mood colors.**

**Options:**

**A) In each UseCase:**
```kotlin
class AddEntryUseCase(..., private val syncScheduler: SyncScheduler) {
    suspend operator fun invoke(entry: Entry) {
        repository.insertEntry(entry)
        if (preferencesRepository.isSyncEnabled()) {
            syncScheduler.requestImmediateSync()
        }
    }
}
```
- Explicit per operation
- Violates SRP - UseCase doing sync scheduling

**B) In Repository:**
```kotlin
class EntryRepositoryImpl(..., private val syncScheduler: SyncScheduler) {
    override suspend fun insertEntry(entry: Entry) {
        // ... insert logic
        if (preferencesRepository.isSyncEnabled()) {
            syncScheduler.requestImmediateSync()
        }
    }
}
```
- Centralised but repository layer shouldn't know about sync scheduling

**C) Via Room callback/trigger:**
- Room doesn't support post-commit hooks cleanly

**D) Debounced sync in ViewModel:**
```kotlin
// EditorViewModel
private val syncTrigger = Channel<Unit>(Channel.CONFLATED)

init {
    syncTrigger.receiveAsFlow()
        .debounce(2000) // Wait 2s after last change
        .collect { syncScheduler.requestImmediateSync() }
}

fun onEntrySaved() {
    syncTrigger.trySend(Unit)
}
```
- Prevents rapid-fire syncs during editing
- ViewModel appropriate for UI-triggered actions
- **Note:** Uses `Channel` not `StateFlow` - StateFlow won't re-emit identical values

**Recommendation:** Option D - debounced in ViewModel. Prevents excessive syncs while ensuring changes sync within 2 seconds.

**Edge cases:**
- Rapid edits → debounce prevents sync spam
- App closed during debounce → sync on dismiss catches it

---

### 6. Sync on App Dismiss

**Trigger sync when app goes to background.**

**Implementation:**
```kotlin
// MainActivity or TheDayToApplication
ProcessLifecycleOwner.get().lifecycle.addObserver(
    object : DefaultLifecycleObserver {
        override fun onStop(owner: LifecycleOwner) {
            if (authRepository.getSignedInUser() != null) {
                syncScheduler.requestImmediateSync()
            }
        }
    }
)
```

**Files:** `TheDayToApplication.onCreate()` or `MainActivity.onCreate()`

**Edge cases:**
- App killed immediately → WorkManager survives, completes sync
- No network → WorkManager queues with network constraint
- Already syncing → `ExistingWorkPolicy.KEEP` lets in-progress sync complete (don't use `REPLACE` which cancels it)

---

### 7. Entry Uniqueness Constraint

**Add unique constraint: one entry per day per user.**

**Schema change:**
```kotlin
@Entity(
    tableName = "entry",
    indices = [
        Index(value = ["dateStamp", "userId"], unique = true),  // NEW
        // ... existing indices
    ]
)
```

**Migration:**
```kotlin
val MIGRATION_X_Y = object : Migration(X, Y) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Handle duplicates - keep entry with latest updatedAt (not id!)
        // id order doesn't correlate with creation time for synced entries
        // Uses subquery approach compatible with all SQLite versions (no window functions)
        // dateStamp is NOT NULL (Long, non-nullable), userId is nullable (String?)
        database.execSQL("""
            DELETE FROM entry WHERE id NOT IN (
                SELECT e1.id FROM entry e1
                WHERE e1.id = (
                    SELECT e2.id FROM entry e2
                    WHERE e2.dateStamp = e1.dateStamp
                      AND COALESCE(e2.userId, '') = COALESCE(e1.userId, '')
                    ORDER BY COALESCE(e2.updatedAt, 0) DESC, e2.id DESC
                    LIMIT 1
                )
            )
        """)
        // Add unique index
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_entry_dateStamp_userId ON entry(dateStamp, userId)")
    }
}
```

**Why not `MAX(id)`?** Downloaded entries get Room-assigned IDs that don't correlate with creation time. Using `updatedAt` ensures we keep the most recently edited entry.

**Why not window functions?** `ROW_NUMBER()` requires SQLite 3.25+ (Android API 30+). The subquery approach works on all versions.

**Edge cases:**
- Existing duplicates → migration keeps most recent by `updatedAt`, falls back to highest `id` as tiebreaker
- Null userId → constraint allows multiple nulls (SQLite behavior) - entries without userId are pre-sign-in, will be adopted
- Conflict on insert → `OnConflictStrategy.REPLACE` handles - new entry replaces old
- Null updatedAt → `COALESCE(updatedAt, 0)` treats as oldest

---

### 8. Adopt Orphaned Data

**Set userId on orphaned items (userId = null) when signing in.**

**Change markLocalDataForSync:**
```kotlin
// MoodColorDao
@Query("""
    UPDATE mood_color
    SET syncStatus = 'PENDING_SYNC', userId = :userId
    WHERE syncStatus = 'LOCAL_ONLY' AND (userId IS NULL OR userId = :userId)
""")
suspend fun markLocalOnlyAsPendingSync(userId: String): Int

// EntryDao - same pattern
```

**Or separate adoption step:**
```kotlin
// SyncRepository
suspend fun adoptOrphanedData(userId: String): Int {
    val entries = entryDao.adoptOrphans(userId)
    val moods = moodColorDao.adoptOrphans(userId)
    return entries + moods
}

// Called in handleSignInSuccess before markLocalDataForSync
```

**Recommendation:** Separate method - clearer intent, single responsibility.

**Edge cases:**
- Multiple users on same device → orphans adopted by first sign-in only
- User signs out, new user signs in → orphaned data stays with original adopter (by syncId in Firestore)

---

### 9. Handle Sign-Out

**When user signs out, clean up sync state.**

**Implementation:**
```kotlin
// SyncSettingsViewModel or SignOutUseCase
suspend fun handleSignOut() {
    // 1. Cancel any pending sync work
    syncScheduler.cancelPendingSync()

    // 2. Mark synced items as LOCAL_ONLY (they stay on device)
    entryDao.markSyncedAsLocalOnly()
    moodColorDao.markSyncedAsLocalOnly()

    // 3. Clear userId from local items (optional - prevents confusion)
    // OR keep userId to remember original owner
}

// SyncScheduler
fun cancelPendingSync() {
    workManager.cancelUniqueWork(SYNC_WORK_NAME)
}

// EntryDao
@Query("UPDATE entry SET syncStatus = 'LOCAL_ONLY' WHERE syncStatus = 'SYNCED'")
suspend fun markSyncedAsLocalOnly(): Int
```

**Behavior choices:**

| Option | On sign-out | On different user sign-in | Recommendation |
|--------|-------------|---------------------------|----------------|
| Keep data, clear userId | Data stays, becomes orphan | New user adopts all | Simple but mixes users' data |
| Keep data, preserve userId | Data stays with original userId | New user sees nothing until they create | ✅ Cleaner separation |
| Delete synced data | Only LOCAL_ONLY data remains | Clean slate | Data loss if only device |

**Recommendation:** Keep data with preserved userId. On sign-in, only adopt items where `userId IS NULL`.

**Files:** `SyncSettingsViewModel`, `SyncScheduler`, DAOs

**Edge cases:**
- Sign out during sync → cancel work, mark remaining as LOCAL_ONLY
- Sign in as different user → their Firestore data downloads, local data with old userId untouched
- Sign back in as same user → data with matching userId already present, sync merges

---

### 10. Remove Sync Counts from UI (was 9)

**Remove download/upload counts - confusing for users.**

**Files:**
- `SyncSettingsState.kt` - remove count fields if present
- `AccountScreen.kt` - remove count display
- `SyncResult.kt` - can keep for logging, just don't expose in UI

**Keep:**
- Last sync timestamp (useful)
- Sync status indicator (syncing/success/error)

---

### 11. Remove createdAt (was 10)

**Not used anywhere - delete from FirestoreMapper.**

**Files:** `FirestoreMapper.kt`

```kotlin
// Remove this line from Entry.toFirestoreMap():
"createdAt" to (updatedAt?.let { ... } ?: Timestamp.now())
```

---

## Validation

### Edge Cases Addressed

| Scenario | Handling |
|----------|----------|
| Reinstall, user has edits in Firestore | Download first → edits preserved |
| User edits seeded mood | updatedAt > 0 → uploads and syncs |
| Rapid edits | Debounced sync (via Channel) prevents spam |
| App killed mid-sync | WorkManager completes in background |
| No network | WorkManager queues with network constraint |
| Multiple devices editing same entry | Last-write-wins by updatedAt |
| Existing duplicate entries | Migration keeps most recent by updatedAt |
| Orphaned data from before sign-in | Adopted with userId on sign-in |
| Offline edits during sync | PENDING_SYNC items preserved during download, uploaded after |
| Sign out during sync | Work cancelled, items marked LOCAL_ONLY |
| Sign in as different user | Old user's data untouched, new user's data downloaded |
| Already syncing when new sync requested | KEEP policy lets in-progress sync complete |

### Assumptions Validated

| Assumption | Validation |
|------------|------------|
| WorkManager survives app death | ✅ Core WorkManager guarantee (except force-stop) |
| SQLite unique allows multiple NULLs | ✅ SQLite spec - NULL != NULL |
| Seeds always have updatedAt=0 | ✅ Set in SeedDefaultMoodColorsUseCase (hardcoded 0L) |
| Seeding happens before sign-in | ✅ Called in Application.onCreate() |
| StateFlow re-emits identical values | ❌ It doesn't - use Channel for debounce trigger |
| Migration subquery works on all SQLite | ✅ Avoids window functions, compatible with all Android versions |

### Simpler Alternatives Considered

| Original Idea | Simpler Alternative | Decision |
|---------------|---------------------|----------|
| Sync on every operation | Debounced sync | ✅ Debounce prevents excessive API calls |
| Complex merge logic | Download-first + last-write-wins | ✅ Simple and correct |
| Track sync per-item | WorkManager handles batching | ✅ Let WorkManager manage |

### Performance Considerations

| Concern | Mitigation |
|---------|------------|
| Too many syncs | 2-second debounce on operations via Channel |
| Large sync payloads | Already fetches all items - consider pagination later |
| Sync blocking UI | WorkManager runs in background |
| Database migrations | One-time cost, tested in migration tests |
| Duplicate sync requests | ExistingWorkPolicy.KEEP prevents redundant work |

### Security Considerations

| Concern | Status |
|---------|--------|
| User data isolation | ✅ Firestore rules enforce userId match |
| Orphan adoption | ✅ Only adopts null userId items |
| Auth state checks | ✅ All sync triggers check signed-in state |

---

## Clean Architecture Compliance

### Single Responsibility

| Component | Responsibility |
|-----------|----------------|
| SyncRepository | Firestore ↔ Room data transfer |
| SyncScheduler | WorkManager job scheduling |
| SyncWorker | Execute sync via UseCase |
| PerformSyncUseCase | Orchestrate sync with auth/prefs checks |
| ViewModel | Trigger sync based on UI events |

### DRY

- Sync triggering consolidated in `SyncScheduler.requestImmediateSync()`
- Conflict resolution in single `FirestoreMapper` location
- userId adoption in single DAO method

### Dependency Direction

```
UI → ViewModel → UseCase → Repository → DAO/Firestore
         ↓
    SyncScheduler (injected)
```

No circular dependencies. Sync scheduling injected where needed.

---

## Implementation Order

1. **Remove createdAt** - Quick win, no dependencies
2. **Fix conflict resolution** - Preserve PENDING_SYNC items during download (critical fix)
3. **Reorder sync phases** - Download before upload
4. **Skip seed uploads** - Prevents overwrite
5. **Remove seeding from handleSignInSuccess** - Fix race condition
6. **Adopt orphans** - Required for userId constraint
7. **Entry uniqueness constraint** - Schema change with migration (uses updatedAt not id)
8. **Handle sign-out** - Cancel sync, mark items LOCAL_ONLY
9. **Sync on startup** - Add to OverviewViewModel
10. **Sync on operations** - Add debounced sync via Channel (not StateFlow)
11. **Sync on dismiss** - Add lifecycle observer with KEEP policy
12. **Remove sync counts from UI** - Cleanup

---

## Testing Strategy

### Unit Tests

- `SyncRepositoryImplTest` - verify download-before-upload order
- `FirestoreMapperTest` - verify PENDING_SYNC items not overwritten during conflict resolution
- `MoodColorDaoTest` - verify seed filtering in pending sync query
- Migration test - verify duplicate handling uses updatedAt (not id) and index creation

### Integration Tests

- Fresh install → sign in → verify seeded moods don't upload
- Edit mood → verify updatedAt changes → verify uploads
- Create entry → verify adopts userId → verify syncs
- **Offline edit preservation:** Edit offline → sync from another device → come online → verify local edit preserved and uploaded
- **Sign out:** Sign out during sync → verify work cancelled → verify items marked LOCAL_ONLY

### Manual Testing

1. Device A: Create entry, edit mood color
2. Device B: Fresh install, sign in
3. Verify: Entry and edited mood appear on Device B
4. Device B: Edit entry
5. Device A: Open app
6. Verify: Device B's edit appears on Device A

---

## Rollback Plan

If issues discovered post-release:
1. Sync order change is safe - no data loss possible with download-first
2. Unique constraint can be dropped in migration if causing issues
3. Sync triggers can be disabled via feature flag (add if needed)

---

## Open Questions

1. Should we add a "last synced" indicator per item? (Not in scope - keep simple)
2. Pagination for large datasets? (Defer - current approach works for typical usage)
3. Offline conflict queue? (Defer - WorkManager retry is sufficient)
