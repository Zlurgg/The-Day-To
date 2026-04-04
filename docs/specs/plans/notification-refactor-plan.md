# Plan: Notification Feature Refactor (v3.2) ✅ APPROVED

## Summary

Refactor notifications from SharedPreferences to Room + Firestore sync, extract to dedicated feature module, and fix user isolation issues.

**Changes from v3.1:** Fixed migration race condition (Mutex instead of AtomicBoolean), removed redundant Application.onCreate() step, removed unused exists() DAO method.

**Changes from v3:** Fixed interface consistency (both `getSettings` and `getSettingsState`), lazy migration in repository, validation in `toDomain()` not constructor, added `SignInSyncResult` for UI feedback, worker testing note.

**Changes from v2:** Addressed staff engineer review feedback on migration atomicity, anonymous settings preservation, receiver lifecycle, clock skew, input validation, phase ordering, and sync clarity.

---

## Prerequisite: DateTime Unification ✅ COMPLETE

**Completed:** The `TimeProvider` abstraction is now in place.

**Available infrastructure:**

```kotlin
// Time access (inject where needed)
interface TimeProvider {
    fun today(): LocalDate
    fun now(): LocalDateTime
    fun instant(): Instant
    fun todayStorageEpoch(): Long  // Atomic: today's date as UTC midnight epoch
}

// Storage conversion (pure extensions, no injection needed)
fun LocalDate.toStorageEpoch(): Long  // LocalDate → UTC midnight epoch seconds
fun Long.toLocalDate(): LocalDate     // Epoch seconds → LocalDate

// UI formatting (stateless object)
object DateFormatter {
    fun formatDate(epochSeconds: Long): String
    fun formatDay(epochSeconds: Long): Int
    fun formatMonthValue(epochSeconds: Long): Int
    fun formatYear(epochSeconds: Long): Int
}
```

**Usage in notifications:**
- `TimeProvider.todayStorageEpoch()` for "today" checks in NotificationWorker
- `TimeProvider.now()` for scheduling calculations
- Inject `TimeProvider` via Koin: `single<TimeProvider> { SystemTimeProvider() }`
- Test with `FakeTimeProvider` for deterministic time control

---

## Critical Issues Addressed

### Issue 1: Repository Responsibility Split

**Current state:**
- `PreferencesRepository` → settings storage (enabled, hour, minute)
- `NotificationRepository` → scheduling/cancellation (WorkManager)

**Resolution:**
- **Remove** notification methods from `PreferencesRepository`
- `NotificationSettingsRepository` (NEW) → Room-based settings storage (CRUD only)
- `NotificationScheduler` (renamed from NotificationRepository) → WorkManager operations only
- `NotificationMigrationService` (NEW) → one-time SharedPreferences migration

```kotlin
// Clear separation
interface NotificationSettingsRepository {
    // For UI - returns sealed class for clear state handling
    suspend fun getSettingsState(userId: String): NotificationSettingsState

    // For internal logic - nullable for simpler conditionals
    suspend fun getSettings(userId: String): NotificationSettings?

    suspend fun saveSettings(userId: String, settings: NotificationSettings)
    suspend fun deleteSettings(userId: String)
}

interface NotificationScheduler {
    fun scheduleDaily(hour: Int, minute: Int)
    fun cancel()
    fun hasPermission(): Boolean
    fun areSystemNotificationsEnabled(): Boolean
}
```

### Issue 2: Entity Primary Key Fix

**Problem:** `id = 1` as PK can't support multiple users.

**Fix:** Use `userId` as primary key:

```kotlin
@Entity(tableName = "notification_settings")
data class NotificationSettingsEntity(
    @PrimaryKey val userId: String,  // "anonymous" for signed-out, else Firebase UID
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val syncId: String,
    val syncStatus: String,
    val updatedAt: Long  // Firestore server timestamp on upload
) {
    // No init validation - corrupt DB data shouldn't crash the app

    /**
     * Converts to domain model. Returns null if data is corrupt.
     * Corrupt data is logged but doesn't crash - user can reconfigure.
     */
    fun toDomain(): NotificationSettings? {
        if (hour !in 0..23 || minute !in 0..59) {
            Timber.e("Invalid notification settings: hour=$hour, minute=$minute, userId=$userId")
            return null
        }
        return NotificationSettings(enabled, hour, minute)
    }

    companion object {
        fun fromDomain(
            settings: NotificationSettings,
            userId: String,
            syncId: String = UUID.randomUUID().toString(),
            syncStatus: SyncStatus = SyncStatus.LOCAL_ONLY,
            updatedAt: Long = System.currentTimeMillis()
        ) = NotificationSettingsEntity(
            userId = userId,
            enabled = settings.enabled,
            hour = settings.hour,
            minute = settings.minute,
            syncId = syncId,
            syncStatus = syncStatus.toStorageValue(),
            updatedAt = updatedAt
        )
    }
}

// Domain model validates on construction (for new data created in-app)
data class NotificationSettings(
    val enabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0
) {
    init {
        require(hour in 0..23) { "Hour must be 0-23" }
        require(minute in 0..59) { "Minute must be 0-59" }
    }
}

// Type-safe enum storage
fun SyncStatus.toStorageValue(): String = name
fun String.toSyncStatus(): SyncStatus = SyncStatus.valueOf(this)
```

### Issue 3: Sign-In Preserves Anonymous Settings

**Problem (v2):** Deleting anonymous settings before download could lose user's preferences on network failure.

**Fix:** Only delete anonymous after successful download OR user confirms defaults:

```kotlin
suspend fun handleSignInSuccess(userId: String) {
    // 1. Try to download user's remote settings
    val remoteResult = notificationSyncService.download(userId)

    when (remoteResult) {
        is Result.Success -> {
            val remoteSettings = remoteResult.data
            if (remoteSettings != null) {
                // User has cloud settings - use those
                notificationSettingsRepository.saveSettings(userId, remoteSettings)
                notificationSettingsRepository.deleteSettings("anonymous")
                if (remoteSettings.enabled) {
                    notificationScheduler.scheduleDaily(remoteSettings.hour, remoteSettings.minute)
                }
            } else {
                // No cloud settings - migrate anonymous to user
                val anonymousSettings = notificationSettingsRepository.getSettings("anonymous")
                if (anonymousSettings != null) {
                    notificationSettingsRepository.saveSettings(userId, anonymousSettings)
                    notificationSettingsRepository.deleteSettings("anonymous")
                    // Upload to cloud
                    notificationSyncService.upload(anonymousSettings, userId)
                }
            }
        }
        is Result.Error -> {
            // Network failed - keep anonymous settings, don't disrupt user
            // They can sync later when online
            Timber.w("Failed to download settings on sign-in, keeping anonymous")
        }
    }
}
```

### Issue 4: Sign-Out Cleanup

**Decision:** Delete on sign-out. Settings are preferences, not user content.

```kotlin
suspend fun signOut(userId: String) {
    // 1. Cancel scheduled notifications
    notificationScheduler.cancel()

    // 2. Delete user's settings
    notificationSettingsRepository.deleteSettings(userId)

    // 3. Continue with auth sign-out
    authRepository.signOut()
}
```

### Issue 5: Conflict Resolution with Server Timestamps

**Problem (v2):** Using `System.currentTimeMillis()` is vulnerable to clock skew.

**Fix:** Use Firestore server timestamp on upload:

```kotlin
// On upload to Firestore
val docData = mapOf(
    "enabled" to settings.enabled,
    "hour" to settings.hour,
    "minute" to settings.minute,
    "syncId" to settings.syncId,
    "updatedAt" to FieldValue.serverTimestamp()  // Server sets this
)

// Conflict resolution
fun resolveSettingsConflict(
    local: NotificationSettingsEntity,
    remote: NotificationSettingsEntity
): NotificationSettingsEntity {
    // Remote timestamp is authoritative (server-set)
    return if (remote.updatedAt >= local.updatedAt) {
        remote.copy(syncStatus = SyncStatus.SYNCED.toStorageValue())
    } else {
        local.copy(syncStatus = SyncStatus.PENDING_SYNC.toStorageValue())
    }
}
```

**Known limitation:** If user edits offline, local timestamp may be wrong. Document this - acceptable for notification settings (low-stakes data).

### Issue 6: Worker Guard with Session Validation

**Problem (v2):** Stale "anonymous" row could fire notifications for wrong context.

**Fix:** Track active session in settings:

```kotlin
// NotificationWorker.kt
override suspend fun doWork(): Result {
    val currentUserId = authRepository.getSignedInUser()?.userId ?: "anonymous"
    val settings = notificationSettingsRepository.getSettings(currentUserId)

    // Guard 1: No settings for current user
    if (settings == null) {
        Timber.d("No notification settings for user $currentUserId")
        return Result.success()
    }

    // Guard 2: Notifications disabled
    if (!settings.enabled) {
        Timber.d("Notifications disabled for user $currentUserId")
        return Result.success()
    }

    // Guard 3: Check if entry exists for today
    val todayEpoch = timeProvider.todayStorageEpoch()
    val hasEntry = entryRepository.getEntryByDate(todayEpoch).getOrNull() != null
    if (hasEntry) {
        Timber.d("Entry already exists for today, skipping notification")
        return Result.success()
    }

    // Send notification
    notificationManager.sendDailyReminder()
    return Result.success()
}
```

### Issue 7: Migration Atomicity + Timing

**Problem (v2):**
1. Migration could crash between Room insert and SharedPreferences flag
2. `Application.onCreate()` can't call suspend functions - migration runs async, repository might be accessed before completion

**Fix:**
1. Check Room row existence instead of separate flag (crash-safe)
2. Lazy migration inside repository (guarantees migration before first access)

```kotlin
class NotificationMigrationService(
    private val dao: NotificationSettingsDao,
    private val legacyPrefs: SharedPreferences,
    private val authRepository: AuthRepository
) {
    suspend fun migrateIfNeeded() {
        // Check if legacy keys exist (source of truth for "needs migration")
        val hasLegacyData = legacyPrefs.contains("notification_enabled")
        if (!hasLegacyData) return

        val userId = authRepository.getSignedInUser()?.userId ?: "anonymous"

        // Check if already migrated to Room
        val existingSettings = dao.getByUserId(userId)
        if (existingSettings != null) {
            // Already in Room - just clean up SharedPreferences
            cleanupLegacyPrefs()
            return
        }

        // Read legacy values
        val enabled = legacyPrefs.getBoolean("notification_enabled", false)
        val hour = legacyPrefs.getInt("notification_hour", 9)
        val minute = legacyPrefs.getInt("notification_minute", 0)

        // Insert into Room
        dao.upsert(NotificationSettingsEntity(
            userId = userId,
            enabled = enabled,
            hour = hour,
            minute = minute,
            syncId = UUID.randomUUID().toString(),
            syncStatus = SyncStatus.PENDING_SYNC.toStorageValue(),
            updatedAt = System.currentTimeMillis()
        ))

        // Clean up SharedPreferences (safe to crash here - Room is source of truth)
        cleanupLegacyPrefs()
    }

    private fun cleanupLegacyPrefs() {
        legacyPrefs.edit()
            .remove("notification_enabled")
            .remove("notification_hour")
            .remove("notification_minute")
            .apply()
    }
}

// Repository ensures migration before any access
class NotificationSettingsRepositoryImpl(
    private val dao: NotificationSettingsDao,
    private val migrationService: NotificationMigrationService
) : NotificationSettingsRepository {

    private val migrationMutex = Mutex()
    private var migrated = false

    private suspend fun ensureMigrated() {
        if (migrated) return  // Fast path after first migration
        migrationMutex.withLock {
            if (!migrated) {
                migrationService.migrateIfNeeded()
                migrated = true
            }
        }
    }

    override suspend fun getSettingsState(userId: String): NotificationSettingsState {
        ensureMigrated()
        val entity = dao.getByUserId(userId)
        return if (entity == null) {
            NotificationSettingsState.NotConfigured
        } else {
            val settings = entity.toDomain()
            if (settings != null) {
                NotificationSettingsState.Configured(settings)
            } else {
                // Corrupt data - treat as not configured
                NotificationSettingsState.NotConfigured
            }
        }
    }

    override suspend fun getSettings(userId: String): NotificationSettings? {
        ensureMigrated()
        return dao.getByUserId(userId)?.toDomain()
    }

    // ... saveSettings, deleteSettings
}
```

**Note:** `Mutex` with double-checked locking ensures migration runs exactly once and concurrent callers wait for completion.

---

## Architecture Refinements

### Issue 8: Domain Model with Existence Signal

**Problem (v2):** `NotificationSettings?` doesn't distinguish "not configured" vs "disabled".

**Fix:** Sealed class for repository return:

```kotlin
// Domain model
data class NotificationSettings(
    val enabled: Boolean = false,
    val hour: Int = 9,
    val minute: Int = 0
) {
    init {
        require(hour in 0..23) { "Hour must be 0-23" }
        require(minute in 0..59) { "Minute must be 0-59" }
    }
}

// Repository result type
sealed interface NotificationSettingsState {
    data object NotConfigured : NotificationSettingsState
    data class Configured(val settings: NotificationSettings) : NotificationSettingsState
}

// Repository interface (both methods for different use cases)
interface NotificationSettingsRepository {
    suspend fun getSettingsState(userId: String): NotificationSettingsState  // For UI
    suspend fun getSettings(userId: String): NotificationSettings?            // For internal logic
    suspend fun saveSettings(userId: String, settings: NotificationSettings)
    suspend fun deleteSettings(userId: String)
}

// UI usage
when (val state = repository.getSettingsState(userId)) {
    is NotificationSettingsState.NotConfigured -> showOnboarding()
    is NotificationSettingsState.Configured -> {
        if (state.settings.enabled) showEnabledUI(state.settings)
        else showDisabledUI(state.settings)
    }
}
```

### Issue 9: Use Case Consolidation

**Before (8 use cases):** Excessive, many are thin wrappers.

**After (3 use cases):**

```kotlin
// 1. Settings management
class NotificationSettingsUseCase(
    private val settingsRepository: NotificationSettingsRepository,
    private val scheduler: NotificationScheduler,
    private val authRepository: AuthRepository
) {
    suspend fun getSettingsState(): NotificationSettingsState
    suspend fun saveSettings(enabled: Boolean, hour: Int, minute: Int)
}

// 2. Permission handling
class NotificationPermissionUseCase(
    private val scheduler: NotificationScheduler
) {
    fun hasPermission(): Boolean
    fun areSystemNotificationsEnabled(): Boolean
    fun shouldShowRationale(activity: Activity): Boolean
}

// 3. Sync operations
class NotificationSyncUseCase(
    private val settingsRepository: NotificationSettingsRepository,
    private val syncService: NotificationSyncService,
    private val scheduler: NotificationScheduler,
    private val authRepository: AuthRepository
) {
    suspend fun handleSignInSuccess(userId: String): SignInSyncResult
    suspend fun handleSignOut(userId: String)
    suspend fun syncNow()
}

// Result type for sign-in sync (allows UI to show appropriate feedback)
sealed interface SignInSyncResult {
    data object DownloadedFromCloud : SignInSyncResult
    data object MigratedAnonymous : SignInSyncResult
    data object OfflineKeptAnonymous : SignInSyncResult
    data object NoSettingsFound : SignInSyncResult
}
```

### Issue 10: Dependency Graph for Sync

**Problem (v2):** SyncRepository integration was unclear.

**Fix:** Clear dependency flow:

```
┌─────────────────────────────────────────────────────────────────┐
│                        SyncCoordinator                          │
│  (orchestrates full sync, called from SyncSettingsViewModel)    │
└─────────────────────────────────────────────────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐
│  EntrySyncRepo  │  │ MoodColorSync   │  │ NotificationSync    │
│                 │  │     Repo        │  │     Service         │
└─────────────────┘  └─────────────────┘  └─────────────────────┘
         │                    │                    │
         ▼                    ▼                    ▼
┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐
│   EntryDao      │  │ MoodColorDao    │  │ NotificationSettings│
│                 │  │                 │  │        Dao          │
└─────────────────┘  └─────────────────┘  └─────────────────────┘
```

```kotlin
// SyncCoordinator.kt (or rename existing SyncRepositoryImpl)
class SyncCoordinator(
    private val entrySyncRepository: EntrySyncRepository,
    private val moodColorSyncRepository: MoodColorSyncRepository,
    private val notificationSyncService: NotificationSyncService,
    private val notificationSettingsRepository: NotificationSettingsRepository
) {
    suspend fun performFullSync(userId: String): Result<SyncResult, DataError.Sync> {
        // 1. Sync entries
        entrySyncRepository.sync(userId)

        // 2. Sync mood colors
        moodColorSyncRepository.sync(userId)

        // 3. Sync notification settings
        val localSettings = notificationSettingsRepository.getSettings(userId)
        if (localSettings != null) {
            notificationSyncService.upload(localSettings, userId)
        }
        when (val remoteResult = notificationSyncService.download(userId)) {
            is Result.Success -> {
                remoteResult.data?.let { remoteSettings ->
                    // Conflict resolution if local exists
                    // ... merge logic ...
                }
            }
            is Result.Error -> {
                Timber.w("Failed to download notification settings: ${remoteResult.error}")
            }
        }

        return Result.Success(SyncResult(...))
    }
}
```

### Issue 11: Sync Retry Mechanism

**Question from review:** Is sync fire-and-forget or guaranteed delivery?

**Decision:** Fire-and-forget for notification settings.

**Rationale:**
- Notification settings are low-stakes (user can re-configure)
- Full sync happens on app launch and sign-in
- PENDING_SYNC status is cleared on next successful sync
- No background retry worker needed

**If guaranteed delivery needed later:** Add `NotificationSyncWorker` that runs periodically to push PENDING_SYNC items.

---

## Edge Cases Addressed

### Issue 12: Timezone Change via WorkManager

**Problem (v2):** BroadcastReceiver with inline coroutine is leaky (scope never cancelled, 10s limit).

**Fix:** Enqueue WorkManager job instead:

```kotlin
// TimezoneChangeReceiver.kt
class TimezoneChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            Timber.d("Timezone changed, enqueueing reschedule work")

            val workRequest = OneTimeWorkRequestBuilder<RescheduleNotificationWorker>()
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    "reschedule_notification",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )
        }
    }
}

// RescheduleNotificationWorker.kt
class RescheduleNotificationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val settingsRepo: NotificationSettingsRepository = get()
        val scheduler: NotificationScheduler = get()
        val authRepo: AuthRepository = get()

        val userId = authRepo.getSignedInUser()?.userId ?: "anonymous"
        val settings = settingsRepo.getSettings(userId)

        if (settings?.enabled == true) {
            scheduler.scheduleDaily(settings.hour, settings.minute)
        }

        return Result.success()
    }
}
```

**Worker Testing Note:**
Workers use Koin's global `get()` for dependency injection. For testing:
- Use `KoinTestRule` to provide test modules
- Or implement `WorkerFactory` for constructor injection (more testable but more setup)
- Current approach works and matches existing patterns in codebase

### Issue 13: Firestore Document Structure

**Path:** `users/{userId}/settings/notifications`

**Structure:**
- `users` = collection
- `{userId}` = document (contains user profile fields)
- `settings` = subcollection
- `notifications` = document

```kotlin
// Firestore document structure
val notificationDoc = firestore
    .collection("users")
    .document(userId)
    .collection("settings")
    .document("notifications")

// Document fields
mapOf(
    "enabled" to true,
    "hour" to 9,
    "minute" to 30,
    "syncId" to "uuid-here",
    "updatedAt" to FieldValue.serverTimestamp()
)
```

### Issue 14: Doze Mode / Battery Optimization

**Decision:** Keep WorkManager, document limitations.

**UI text for settings:**
> "Reminder times may vary slightly on some devices due to battery optimization."

---

## Revised Implementation Phases

### Phase 1: Room Storage + Package Structure

**Goal:** Replace SharedPreferences with Room, create notification/ package from start.

**Steps:**
1. Create `notification/` package structure (avoid Phase 3 churn)
2. Create `NotificationSettingsEntity` with input validation
3. Create `NotificationSettingsDao`
4. Add to AppDatabase with migration (version 5 → 6)
5. Create `NotificationSettingsRepository` interface + impl (with lazy migration via Mutex)
6. Create `NotificationMigrationService`
7. Create `NotificationSettingsState` sealed class
8. Update existing use cases to use new repository
9. Remove notification methods from `PreferencesRepository`
10. Add `TimezoneChangeReceiver` + `RescheduleNotificationWorker`
11. Write migration tests

**Files:**
```
notification/
├── domain/
│   ├── model/NotificationSettings.kt
│   ├── model/NotificationSettingsState.kt
│   └── repository/NotificationSettingsRepository.kt
├── data/
│   ├── local/NotificationSettingsEntity.kt
│   ├── local/NotificationSettingsDao.kt
│   ├── repository/NotificationSettingsRepositoryImpl.kt
│   ├── migration/NotificationMigrationService.kt
│   └── worker/RescheduleNotificationWorker.kt
└── di/NotificationModule.kt

core/
├── data/receiver/TimezoneChangeReceiver.kt (manifest-registered)
└── data/database/TheDayToDatabase.kt (add entity, migration)
```

### Phase 2: Sign-In/Sign-Out Integration

**Goal:** Proper handling on auth transitions with anonymous settings preservation.

**Steps:**
1. Implement `handleSignInSuccess()` with network failure handling
2. Implement `handleSignOut()`
3. Update Worker guards
4. Test sign-out clears settings
5. Test sign-in with no remote settings (migrates anonymous)
6. Test sign-in with remote settings (overwrites anonymous)
7. Test sign-in with network failure (preserves anonymous)

### Phase 3: Use Case Consolidation + Scheduler Rename

**Goal:** Clean up architecture.

**Steps:**
1. Consolidate use cases (8 → 3)
2. Rename `NotificationRepository` → `NotificationScheduler`
3. Move UI components to `notification/ui/`
4. Update ViewModel to use new use cases
5. Update DI registrations

### Phase 4: Firestore Sync

**Goal:** Sync settings across devices.

**Steps:**
1. Create `NotificationSyncService` interface + impl
2. Implement upload with server timestamp
3. Implement download
4. Add conflict resolution
5. Integrate with `SyncCoordinator.performFullSync()`
6. Test sync scenarios

---

## Revised DAO

```kotlin
@Dao
interface NotificationSettingsDao {
    @Query("SELECT * FROM notification_settings WHERE userId = :userId")
    suspend fun getByUserId(userId: String): NotificationSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: NotificationSettingsEntity)

    @Query("DELETE FROM notification_settings WHERE userId = :userId")
    suspend fun deleteByUserId(userId: String)

    @Query("UPDATE notification_settings SET syncStatus = :status WHERE userId = :userId")
    suspend fun updateSyncStatus(userId: String, status: String)

    @Query("SELECT * FROM notification_settings WHERE syncStatus = 'PENDING_SYNC' AND userId = :userId")
    suspend fun getPendingSync(userId: String): NotificationSettingsEntity?
}
```

---

## Migration Strategy

```kotlin
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS notification_settings (
                userId TEXT PRIMARY KEY NOT NULL,
                enabled INTEGER NOT NULL DEFAULT 0,
                hour INTEGER NOT NULL DEFAULT 9,
                minute INTEGER NOT NULL DEFAULT 0,
                syncId TEXT NOT NULL,
                syncStatus TEXT NOT NULL DEFAULT 'LOCAL_ONLY',
                updatedAt INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}
```

**Migration tests required:**
- Empty DB → new table created
- Existing DB → table added, no data loss
- SharedPreferences → Room migration runs once
- Crash during migration → recovers correctly

---

## Decision Log

| Issue | Decision | Rationale |
|-------|----------|-----------|
| Orphaned settings | Delete on sign-out | Settings aren't user content |
| Entity PK | userId as PK | Supports multiple users |
| Conflict resolution | Server timestamp wins | Avoids clock skew issues |
| Anonymous on sign-in | Preserve until download succeeds | Network failure shouldn't lose settings |
| Migration atomicity | Check Room existence, not flag | Crash-safe |
| Migration timing | Lazy in repository with Mutex | Guarantees migration before first access, blocks concurrent access |
| Existence signal | Sealed class | Clear UI states |
| Receiver lifecycle | WorkManager job | Avoids coroutine leaks |
| Sync retry | Fire-and-forget | Low-stakes data |
| Phase ordering | Create notification/ in Phase 1 | Avoids file move churn |
| Input validation | toDomain() returns null | Corrupt DB data shouldn't crash app |
| Repository interface | Both getSettings + getSettingsState | UI needs sealed, internal needs nullable |

---

## Approval Checklist

- [x] Room storage with userId PK approved
- [x] NotificationSettingsState sealed class approved
- [x] Anonymous settings preservation on sign-in approved
- [x] Migration atomicity approach (lazy in repository) approved
- [x] TimezoneChangeReceiver via WorkManager approved
- [x] Server timestamp for conflict resolution approved
- [x] Fire-and-forget sync (no retry worker) approved
- [x] Package structure from Phase 1 approved
- [x] Input validation in toDomain() (not constructor) approved
