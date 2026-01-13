# Firestore Migration Plan

## Overview

This document outlines the plan to migrate The Day To from local-only storage (Room) to a hybrid local + cloud architecture using Firebase Firestore.

**Last Updated**: 2025-12-04

---

## Current State Analysis

### Authentication
- Firebase Auth is **already implemented** and working
- Uses Google Sign-In with modern Credential Manager API
- Provides user identity via `UserData(userId, username, profilePictureUrl)`
- `AuthRepository.getSignedInUser()` returns current user

### Data Storage (Current)
- **100% local storage** via Room database (version 3)
- **Normalized schema**:
  - `EntryEntity(moodColorId, content, dateStamp, id)`
  - `MoodColorEntity(mood, moodNormalized, color, isDeleted, dateStamp, id)`
- **Foreign key relationship**: Entry references MoodColor by ID
- **No userId field** - data is device-scoped, not user-scoped
- **No cloud sync** - data lost if device is lost/reset/uninstalled

### Why Migrate?
1. **Data persistence** - Survive app uninstall/device reset
2. **Multi-device sync** - Access mood logs on any device
3. **User-scoped data** - Data tied to Google account, not device
4. **Portfolio showcase** - Demonstrates cloud integration skills

---

## Target Architecture: Hybrid Local + Cloud

### Pattern: **Room as Cache, Firestore as Source of Truth**

Following Google's recommended pattern for offline-first apps:

```
User Action
    │
    ▼
ViewModel → UseCase → Repository
                          │
              ┌───────────┴───────────┐
              ▼                       ▼
        Room (local)           Firestore (cloud)
              │                       │
              └───────────┬───────────┘
                          ▼
                   UI displays from Room
```

**Data Flow**:
1. **Writes**: Save to Room immediately (optimistic) → sync to Firestore in background
2. **Reads**: Read from Room (instant), Firestore syncs in background
3. **Offline**: Queue writes locally with `syncPending` flag, sync when online
4. **Conflict resolution**: Last-write-wins based on `lastModified` timestamp

### Why Keep Room?
- **Offline-first UX** - App works instantly without internet
- **Performance** - Local queries are instant (no network latency)
- **Battery efficiency** - Fewer network calls
- **Seamless offline mode** - Create/edit offline, syncs when back online

---

## Firestore Data Structure

### Collection Schema

```
/users/{userId}
    /entries/{entryId}
        - moodName: String        // Denormalized from MoodColor
        - moodColor: String       // Denormalized from MoodColor
        - content: String
        - dateStamp: Long
        - lastModified: Long      // For conflict resolution
        - localId: Int?           // Reference to Room ID (nullable)

    /moodColors/{moodColorId}
        - mood: String
        - moodNormalized: String
        - color: String
        - isDeleted: Boolean
        - dateStamp: Long
        - lastModified: Long
        - localId: Int?
```

**Note**: Entries store denormalized mood data (name + color) for efficient Firestore queries. This avoids needing to join collections in Firestore which doesn't support joins.

---

## Implementation Plan

### Phase 1: Setup & Dependencies

#### 1.1 Add Firestore Dependency

**libs.versions.toml**:
```toml
[versions]
firebaseFirestore = "25.1.3"

[libraries]
firebase-firestore = { module = "com.google.firebase:firebase-firestore", version.ref = "firebaseFirestore" }
```

**app/build.gradle.kts**:
```kotlin
implementation(libs.firebase.firestore)
```

#### 1.2 Remove Unused Dependencies

Remove from `libs.versions.toml`:
```toml
# DELETE these lines - location not used
playServicesLocation = "21.3.0"
play-services-location = { module = "com.google.android.gms:play-services-location", version.ref = "playServicesLocation" }
```

#### 1.3 Enable Firestore in Firebase Console

1. Go to Firebase Console → Project "the-day-to"
2. Build → Firestore Database → Create database
3. Start in **production mode** (we'll add security rules)

#### 1.4 Configure Security Rules

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can only access their own data
    match /users/{userId}/{document=**} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

---

### Phase 2: Domain Layer Updates

#### 2.1 Add `getCurrentUserId()` to AuthRepository

**auth/domain/repository/AuthRepository.kt**:
```kotlin
interface AuthRepository {
    suspend fun signIn(): SignInResult
    suspend fun signOut()
    fun getSignedInUser(): UserData?

    /**
     * Gets the current user's Firebase UID.
     * @return userId if signed in, null otherwise
     */
    fun getCurrentUserId(): String? = getSignedInUser()?.userId
}
```

#### 2.2 Update Domain Models

**journal/domain/model/Entry.kt** (add sync fields):
```kotlin
data class Entry(
    val id: Int? = null,
    val moodColorId: Int,
    val moodName: String,      // Denormalized for Firestore
    val moodColor: String,     // Denormalized for Firestore
    val content: String,
    val dateStamp: Long,
    val lastModified: Long = System.currentTimeMillis(),
    val syncPending: Boolean = false,
    val firestoreId: String? = null
)
```

**journal/domain/model/MoodColor.kt** (add sync fields):
```kotlin
data class MoodColor(
    val id: Int? = null,
    val mood: String,
    val moodNormalized: String,
    val color: String,
    val isDeleted: Boolean = false,
    val dateStamp: Long,
    val lastModified: Long = System.currentTimeMillis(),
    val syncPending: Boolean = false,
    val firestoreId: String? = null
)
```

#### 2.3 Create Sync Repository Interface

**journal/domain/repository/SyncRepository.kt**:
```kotlin
/**
 * Repository interface for cloud synchronization.
 * Handles sync state and triggers background sync operations.
 */
interface SyncRepository {
    /** Flow of current sync status */
    val syncStatus: StateFlow<SyncStatus>

    /** Trigger immediate sync of pending items */
    suspend fun syncNow(): Result<SyncResult>

    /** Check if there are items pending sync */
    suspend fun hasPendingSync(): Boolean
}

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Error(val message: String) : SyncStatus
}

data class SyncResult(
    val entriesSynced: Int,
    val moodColorsSynced: Int,
    val errors: List<String>
)
```

---

### Phase 3: Data Layer - Entities & Mappers

#### 3.1 Update Room Entities

**journal/data/model/EntryEntity.kt**:
```kotlin
@Entity(
    tableName = "entry",
    foreignKeys = [
        ForeignKey(
            entity = MoodColorEntity::class,
            parentColumns = ["id"],
            childColumns = ["moodColorId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [
        Index(value = ["moodColorId"]),
        Index(value = ["syncPending"])  // For efficient pending queries
    ]
)
data class EntryEntity(
    val moodColorId: Int,
    val content: String,
    val dateStamp: Long,
    val lastModified: Long = System.currentTimeMillis(),
    val syncPending: Boolean = false,
    val firestoreId: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)
```

**journal/data/model/MoodColorEntity.kt**:
```kotlin
@Entity(
    tableName = "mood_color",
    indices = [
        Index(value = ["mood"]),
        Index(value = ["moodNormalized"], unique = true),
        Index(value = ["syncPending"])
    ]
)
data class MoodColorEntity(
    val mood: String,
    val moodNormalized: String,
    val color: String,
    val isDeleted: Boolean = false,
    val dateStamp: Long,
    val lastModified: Long = System.currentTimeMillis(),
    val syncPending: Boolean = false,
    val firestoreId: String? = null,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)
```

#### 3.2 Update Database Version

**core/data/database/TheDayToDatabase.kt**:
```kotlin
@Database(
    entities = [EntryEntity::class, MoodColorEntity::class],
    version = 4,  // Incremented for sync fields
    exportSchema = false
)
abstract class TheDayToDatabase : RoomDatabase() {
    // ...
}
```

#### 3.3 Create Firestore DTOs

**journal/data/remote/dto/EntryDto.kt**:
```kotlin
/**
 * Firestore document representation of an Entry.
 * Uses denormalized mood data for efficient queries.
 *
 * Default values required for Firestore deserialization.
 */
data class EntryDto(
    val moodName: String = "",
    val moodColor: String = "",
    val content: String = "",
    val dateStamp: Long = 0L,
    val lastModified: Long = 0L,
    val localId: Int? = null,
    @DocumentId val id: String? = null
)
```

**journal/data/remote/dto/MoodColorDto.kt**:
```kotlin
data class MoodColorDto(
    val mood: String = "",
    val moodNormalized: String = "",
    val color: String = "",
    val isDeleted: Boolean = false,
    val dateStamp: Long = 0L,
    val lastModified: Long = 0L,
    val localId: Int? = null,
    @DocumentId val id: String? = null
)
```

#### 3.4 Create Firestore Mappers

**journal/data/mapper/EntryFirestoreMapper.kt**:
```kotlin
/**
 * Mappers for converting between EntryEntity and EntryDto.
 * Handles denormalization of mood data for Firestore storage.
 */

fun EntryEntity.toDto(moodColor: MoodColorEntity): EntryDto {
    return EntryDto(
        moodName = moodColor.mood,
        moodColor = moodColor.color,
        content = content,
        dateStamp = dateStamp,
        lastModified = lastModified,
        localId = id
    )
}

fun EntryDto.toEntity(moodColorId: Int): EntryEntity {
    return EntryEntity(
        moodColorId = moodColorId,
        content = content,
        dateStamp = dateStamp,
        lastModified = lastModified,
        syncPending = false,
        firestoreId = id,
        id = localId
    )
}
```

**journal/data/mapper/MoodColorFirestoreMapper.kt**:
```kotlin
fun MoodColorEntity.toDto(): MoodColorDto {
    return MoodColorDto(
        mood = mood,
        moodNormalized = moodNormalized,
        color = color,
        isDeleted = isDeleted,
        dateStamp = dateStamp,
        lastModified = lastModified,
        localId = id
    )
}

fun MoodColorDto.toEntity(): MoodColorEntity {
    return MoodColorEntity(
        mood = mood,
        moodNormalized = moodNormalized,
        color = color,
        isDeleted = isDeleted,
        dateStamp = dateStamp,
        lastModified = lastModified,
        syncPending = false,
        firestoreId = id,
        id = localId
    )
}
```

---

### Phase 4: Data Layer - Firestore Data Sources

#### 4.1 Create FirestoreEntryDataSource

**journal/data/remote/source/FirestoreEntryDataSource.kt**:
```kotlin
/**
 * Data source for Entry operations in Firestore.
 * Handles CRUD operations for user-scoped entry documents.
 *
 * Collection path: /users/{userId}/entries/{entryId}
 */
class FirestoreEntryDataSource(
    private val firestore: FirebaseFirestore
) {
    private fun getUserEntriesCollection(userId: String): CollectionReference {
        return firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(ENTRIES_COLLECTION)
    }

    suspend fun insertEntry(userId: String, entry: EntryDto): Result<String> = try {
        Timber.d("Inserting entry to Firestore for user: $userId")

        val docRef = getUserEntriesCollection(userId).document()
        val entryWithId = entry.copy(id = docRef.id)

        docRef.set(entryWithId).await()

        Timber.i("Entry inserted successfully: ${docRef.id}")
        Result.success(docRef.id)
    } catch (e: Exception) {
        Timber.e(e, "Failed to insert entry to Firestore")
        Result.failure(e)
    }

    suspend fun updateEntry(userId: String, entry: EntryDto): Result<Unit> = try {
        val entryId = requireNotNull(entry.id) { "Entry ID required for update" }
        Timber.d("Updating entry in Firestore: $entryId")

        getUserEntriesCollection(userId)
            .document(entryId)
            .set(entry)
            .await()

        Timber.i("Entry updated successfully: $entryId")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Failed to update entry in Firestore")
        Result.failure(e)
    }

    suspend fun deleteEntry(userId: String, entryId: String): Result<Unit> = try {
        Timber.d("Deleting entry from Firestore: $entryId")

        getUserEntriesCollection(userId)
            .document(entryId)
            .delete()
            .await()

        Timber.i("Entry deleted successfully: $entryId")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Failed to delete entry from Firestore")
        Result.failure(e)
    }

    fun observeEntries(userId: String): Flow<List<EntryDto>> = callbackFlow {
        Timber.d("Starting Firestore entries listener for user: $userId")

        val listener = getUserEntriesCollection(userId)
            .orderBy("dateStamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to entries")
                    close(error)
                    return@addSnapshotListener
                }

                val entries = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(EntryDto::class.java)
                } ?: emptyList()

                Timber.d("Received ${entries.size} entries from Firestore")
                trySend(entries)
            }

        awaitClose {
            Timber.d("Stopping Firestore entries listener")
            listener.remove()
        }
    }

    suspend fun getAllEntries(userId: String): Result<List<EntryDto>> = try {
        Timber.d("Fetching all entries from Firestore for user: $userId")

        val snapshot = getUserEntriesCollection(userId)
            .orderBy("dateStamp", Query.Direction.DESCENDING)
            .get()
            .await()

        val entries = snapshot.documents.mapNotNull { doc ->
            doc.toObject(EntryDto::class.java)
        }

        Timber.i("Fetched ${entries.size} entries from Firestore")
        Result.success(entries)
    } catch (e: Exception) {
        Timber.e(e, "Failed to fetch entries from Firestore")
        Result.failure(e)
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val ENTRIES_COLLECTION = "entries"
    }
}
```

#### 4.2 Create FirestoreMoodColorDataSource

**journal/data/remote/source/FirestoreMoodColorDataSource.kt**:
```kotlin
/**
 * Data source for MoodColor operations in Firestore.
 * Handles CRUD operations for user-scoped mood color documents.
 *
 * Collection path: /users/{userId}/moodColors/{moodColorId}
 */
class FirestoreMoodColorDataSource(
    private val firestore: FirebaseFirestore
) {
    private fun getUserMoodColorsCollection(userId: String): CollectionReference {
        return firestore.collection(USERS_COLLECTION)
            .document(userId)
            .collection(MOOD_COLORS_COLLECTION)
    }

    suspend fun insertMoodColor(userId: String, moodColor: MoodColorDto): Result<String> = try {
        Timber.d("Inserting mood color to Firestore: ${moodColor.mood}")

        val docRef = getUserMoodColorsCollection(userId).document()
        val moodColorWithId = moodColor.copy(id = docRef.id)

        docRef.set(moodColorWithId).await()

        Timber.i("Mood color inserted successfully: ${docRef.id}")
        Result.success(docRef.id)
    } catch (e: Exception) {
        Timber.e(e, "Failed to insert mood color to Firestore")
        Result.failure(e)
    }

    suspend fun updateMoodColor(userId: String, moodColor: MoodColorDto): Result<Unit> = try {
        val moodColorId = requireNotNull(moodColor.id) { "MoodColor ID required for update" }
        Timber.d("Updating mood color in Firestore: $moodColorId")

        getUserMoodColorsCollection(userId)
            .document(moodColorId)
            .set(moodColor)
            .await()

        Timber.i("Mood color updated successfully: $moodColorId")
        Result.success(Unit)
    } catch (e: Exception) {
        Timber.e(e, "Failed to update mood color in Firestore")
        Result.failure(e)
    }

    fun observeMoodColors(userId: String): Flow<List<MoodColorDto>> = callbackFlow {
        Timber.d("Starting Firestore mood colors listener for user: $userId")

        val listener = getUserMoodColorsCollection(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Timber.e(error, "Error listening to mood colors")
                    close(error)
                    return@addSnapshotListener
                }

                val moodColors = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(MoodColorDto::class.java)
                } ?: emptyList()

                Timber.d("Received ${moodColors.size} mood colors from Firestore")
                trySend(moodColors)
            }

        awaitClose {
            Timber.d("Stopping Firestore mood colors listener")
            listener.remove()
        }
    }

    suspend fun getAllMoodColors(userId: String): Result<List<MoodColorDto>> = try {
        Timber.d("Fetching all mood colors from Firestore for user: $userId")

        val snapshot = getUserMoodColorsCollection(userId)
            .get()
            .await()

        val moodColors = snapshot.documents.mapNotNull { doc ->
            doc.toObject(MoodColorDto::class.java)
        }

        Timber.i("Fetched ${moodColors.size} mood colors from Firestore")
        Result.success(moodColors)
    } catch (e: Exception) {
        Timber.e(e, "Failed to fetch mood colors from Firestore")
        Result.failure(e)
    }

    companion object {
        private const val USERS_COLLECTION = "users"
        private const val MOOD_COLORS_COLLECTION = "moodColors"
    }
}
```

---

### Phase 5: Data Layer - Repository Updates

#### 5.1 Update DAO Queries

**journal/data/dao/EntryDao.kt** (add sync queries):
```kotlin
@Dao
interface EntryDao {
    // Existing queries...

    @Query("SELECT * FROM entry WHERE syncPending = 1")
    suspend fun getPendingSyncEntries(): List<EntryEntity>

    @Query("UPDATE entry SET syncPending = 0, firestoreId = :firestoreId WHERE id = :localId")
    suspend fun markSynced(localId: Int, firestoreId: String)

    @Query("UPDATE entry SET syncPending = 1, lastModified = :timestamp WHERE id = :id")
    suspend fun markPendingSync(id: Int, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<EntryEntity>)
}
```

**journal/data/dao/MoodColorDao.kt** (add sync queries):
```kotlin
@Dao
interface MoodColorDao {
    // Existing queries...

    @Query("SELECT * FROM mood_color WHERE syncPending = 1")
    suspend fun getPendingSyncMoodColors(): List<MoodColorEntity>

    @Query("UPDATE mood_color SET syncPending = 0, firestoreId = :firestoreId WHERE id = :localId")
    suspend fun markSynced(localId: Int, firestoreId: String)

    @Query("UPDATE mood_color SET syncPending = 1, lastModified = :timestamp WHERE id = :id")
    suspend fun markPendingSync(id: Int, timestamp: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(moodColors: List<MoodColorEntity>)

    @Query("SELECT * FROM mood_color WHERE moodNormalized = :moodNormalized LIMIT 1")
    suspend fun getByNormalizedMood(moodNormalized: String): MoodColorEntity?
}
```

#### 5.2 Update EntryRepositoryImpl

**journal/data/repository/EntryRepositoryImpl.kt**:
```kotlin
/**
 * Repository implementation with offline-first sync strategy.
 *
 * Write operations:
 * 1. Save to Room immediately (optimistic update)
 * 2. Mark as syncPending
 * 3. Trigger background sync to Firestore
 *
 * Read operations:
 * 1. Return data from Room (instant)
 * 2. Sync from Firestore in background
 */
class EntryRepositoryImpl(
    private val entryDao: EntryDao,
    private val moodColorDao: MoodColorDao,
    private val firestoreSource: FirestoreEntryDataSource,
    private val authRepository: AuthRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : EntryRepository {

    override fun getEntries(): Flow<List<Entry>> {
        return entryDao.getEntries()
            .map { entities -> entities.map { it.toDomain() } }
            .flowOn(dispatcher)
    }

    override suspend fun insertEntry(entry: Entry): Result<Unit> = withContext(dispatcher) {
        try {
            val entity = entry.toEntity().copy(
                syncPending = true,
                lastModified = System.currentTimeMillis()
            )

            val localId = entryDao.insertEntry(entity)
            Timber.d("Entry saved locally with ID: $localId")

            // Trigger background sync
            syncEntryToFirestore(localId.toInt())

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert entry")
            Result.failure(e)
        }
    }

    override suspend fun updateEntry(entry: Entry): Result<Unit> = withContext(dispatcher) {
        try {
            val entity = entry.toEntity().copy(
                syncPending = true,
                lastModified = System.currentTimeMillis()
            )

            entryDao.updateEntry(entity)
            Timber.d("Entry updated locally: ${entry.id}")

            // Trigger background sync
            entry.id?.let { syncEntryToFirestore(it) }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update entry")
            Result.failure(e)
        }
    }

    override suspend fun deleteEntry(entry: Entry): Result<Unit> = withContext(dispatcher) {
        try {
            entryDao.deleteEntry(entry.toEntity())
            Timber.d("Entry deleted locally: ${entry.id}")

            // Delete from Firestore if it was synced
            entry.firestoreId?.let { firestoreId ->
                authRepository.getCurrentUserId()?.let { userId ->
                    firestoreSource.deleteEntry(userId, firestoreId)
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete entry")
            Result.failure(e)
        }
    }

    private suspend fun syncEntryToFirestore(localId: Int) {
        val userId = authRepository.getCurrentUserId() ?: run {
            Timber.w("Cannot sync - user not signed in")
            return
        }

        try {
            val entry = entryDao.getEntryById(localId) ?: return
            val moodColor = moodColorDao.getMoodColorById(entry.moodColorId) ?: return

            val dto = entry.toDto(moodColor)

            val result = if (entry.firestoreId != null) {
                firestoreSource.updateEntry(userId, dto.copy(id = entry.firestoreId))
            } else {
                firestoreSource.insertEntry(userId, dto)
            }

            result.onSuccess { firestoreId ->
                entryDao.markSynced(localId, firestoreId)
                Timber.i("Entry synced to Firestore: $firestoreId")
            }.onFailure { error ->
                Timber.w(error, "Entry sync failed, will retry later")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error during entry sync")
        }
    }
}
```

---

### Phase 6: Sync Service

#### 6.1 Create SyncRepositoryImpl

**journal/data/repository/SyncRepositoryImpl.kt**:
```kotlin
/**
 * Manages bidirectional sync between Room and Firestore.
 *
 * Responsibilities:
 * - Upload pending local changes to Firestore
 * - Download remote changes to Room
 * - Handle conflict resolution (last-write-wins)
 * - Expose sync status for UI
 */
class SyncRepositoryImpl(
    private val entryDao: EntryDao,
    private val moodColorDao: MoodColorDao,
    private val firestoreEntrySource: FirestoreEntryDataSource,
    private val firestoreMoodColorSource: FirestoreMoodColorDataSource,
    private val authRepository: AuthRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : SyncRepository {

    private val _syncStatus = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    override val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    override suspend fun syncNow(): Result<SyncResult> = withContext(dispatcher) {
        val userId = authRepository.getCurrentUserId() ?: run {
            Timber.w("Cannot sync - user not signed in")
            return@withContext Result.failure(Exception("User not signed in"))
        }

        _syncStatus.value = SyncStatus.Syncing
        Timber.i("Starting sync for user: $userId")

        try {
            val errors = mutableListOf<String>()

            // 1. Upload pending mood colors first (entries depend on them)
            val moodColorsSynced = syncPendingMoodColors(userId, errors)

            // 2. Upload pending entries
            val entriesSynced = syncPendingEntries(userId, errors)

            // 3. Download remote changes
            downloadRemoteChanges(userId)

            _syncStatus.value = SyncStatus.Idle

            val result = SyncResult(
                entriesSynced = entriesSynced,
                moodColorsSynced = moodColorsSynced,
                errors = errors
            )

            Timber.i("Sync completed: $result")
            Result.success(result)
        } catch (e: Exception) {
            Timber.e(e, "Sync failed")
            _syncStatus.value = SyncStatus.Error(e.message ?: "Unknown error")
            Result.failure(e)
        }
    }

    override suspend fun hasPendingSync(): Boolean = withContext(dispatcher) {
        val pendingEntries = entryDao.getPendingSyncEntries()
        val pendingMoodColors = moodColorDao.getPendingSyncMoodColors()
        pendingEntries.isNotEmpty() || pendingMoodColors.isNotEmpty()
    }

    private suspend fun syncPendingMoodColors(
        userId: String,
        errors: MutableList<String>
    ): Int {
        val pending = moodColorDao.getPendingSyncMoodColors()
        Timber.d("Syncing ${pending.size} pending mood colors")

        var synced = 0
        pending.forEach { entity ->
            val dto = entity.toDto()
            val result = if (entity.firestoreId != null) {
                firestoreMoodColorSource.updateMoodColor(userId, dto.copy(id = entity.firestoreId))
                    .map { entity.firestoreId }
            } else {
                firestoreMoodColorSource.insertMoodColor(userId, dto)
            }

            result.onSuccess { firestoreId ->
                entity.id?.let { moodColorDao.markSynced(it, firestoreId) }
                synced++
            }.onFailure { error ->
                errors.add("MoodColor ${entity.mood}: ${error.message}")
            }
        }

        return synced
    }

    private suspend fun syncPendingEntries(
        userId: String,
        errors: MutableList<String>
    ): Int {
        val pending = entryDao.getPendingSyncEntries()
        Timber.d("Syncing ${pending.size} pending entries")

        var synced = 0
        pending.forEach { entity ->
            val moodColor = moodColorDao.getMoodColorById(entity.moodColorId)
            if (moodColor == null) {
                errors.add("Entry ${entity.id}: MoodColor not found")
                return@forEach
            }

            val dto = entity.toDto(moodColor)
            val result = if (entity.firestoreId != null) {
                firestoreEntrySource.updateEntry(userId, dto.copy(id = entity.firestoreId))
                    .map { entity.firestoreId!! }
            } else {
                firestoreEntrySource.insertEntry(userId, dto)
            }

            result.onSuccess { firestoreId ->
                entity.id?.let { entryDao.markSynced(it, firestoreId) }
                synced++
            }.onFailure { error ->
                errors.add("Entry ${entity.id}: ${error.message}")
            }
        }

        return synced
    }

    private suspend fun downloadRemoteChanges(userId: String) {
        Timber.d("Downloading remote changes")

        // Download mood colors
        firestoreMoodColorSource.getAllMoodColors(userId).onSuccess { remoteMoodColors ->
            val entities = remoteMoodColors.map { it.toEntity() }
            moodColorDao.upsertAll(entities)
            Timber.d("Downloaded ${entities.size} mood colors")
        }

        // Download entries (need to resolve moodColorId from mood name)
        firestoreEntrySource.getAllEntries(userId).onSuccess { remoteEntries ->
            remoteEntries.forEach { dto ->
                val moodColor = moodColorDao.getByNormalizedMood(dto.moodName.lowercase())
                if (moodColor != null) {
                    val entity = dto.toEntity(moodColor.id!!)
                    entryDao.upsertAll(listOf(entity))
                }
            }
            Timber.d("Downloaded ${remoteEntries.size} entries")
        }
    }
}
```

#### 6.2 Create SyncWorker for Background Sync

**journal/data/service/SyncWorker.kt**:
```kotlin
/**
 * WorkManager worker for periodic background sync.
 * Runs every 15 minutes when network is available.
 */
class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val syncRepository: SyncRepository by inject(SyncRepository::class.java)

    override suspend fun doWork(): Result {
        Timber.d("SyncWorker started")

        return try {
            val result = syncRepository.syncNow()

            result.fold(
                onSuccess = { syncResult ->
                    Timber.i("SyncWorker completed: $syncResult")
                    Result.success()
                },
                onFailure = { error ->
                    Timber.w(error, "SyncWorker failed, will retry")
                    Result.retry()
                }
            )
        } catch (e: Exception) {
            Timber.e(e, "SyncWorker error")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "sync_worker"

        fun createPeriodicWorkRequest(): PeriodicWorkRequest {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            return PeriodicWorkRequestBuilder<SyncWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES  // Flex interval
            )
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
        }
    }
}
```

---

### Phase 7: DI Module Updates

**di/AppModule.kt** (add Firestore dependencies):
```kotlin
val appModule = module {
    // Existing dependencies...

    // Firebase Firestore
    single { Firebase.firestore }

    // Firestore Data Sources
    single { FirestoreEntryDataSource(get()) }
    single { FirestoreMoodColorDataSource(get()) }

    // Update Repository bindings
    single<EntryRepository> {
        EntryRepositoryImpl(
            entryDao = get(),
            moodColorDao = get(),
            firestoreSource = get(),
            authRepository = get()
        )
    }

    single<SyncRepository> {
        SyncRepositoryImpl(
            entryDao = get(),
            moodColorDao = get(),
            firestoreEntrySource = get(),
            firestoreMoodColorSource = get(),
            authRepository = get()
        )
    }
}
```

---

### Phase 8: Data Migration

#### 8.1 Create Migration Use Case

**journal/domain/usecases/MigrateLocalDataUseCase.kt**:
```kotlin
/**
 * One-time migration of existing local data to Firestore.
 * Runs on first sign-in after update to sync-enabled version.
 */
class MigrateLocalDataUseCase(
    private val entryDao: EntryDao,
    private val moodColorDao: MoodColorDao,
    private val syncRepository: SyncRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(userId: String): Result<MigrationResult> {
        if (preferencesRepository.isDataMigrated()) {
            Timber.d("Data already migrated, skipping")
            return Result.success(MigrationResult(0, 0, skipped = true))
        }

        Timber.i("Starting data migration for user: $userId")

        return try {
            // Mark all existing data as pending sync
            val entries = entryDao.getAllEntriesSync()
            entries.forEach { entry ->
                entry.id?.let { entryDao.markPendingSync(it) }
            }

            val moodColors = moodColorDao.getAllMoodColorsSync()
            moodColors.forEach { moodColor ->
                moodColor.id?.let { moodColorDao.markPendingSync(it) }
            }

            Timber.d("Marked ${entries.size} entries and ${moodColors.size} mood colors for sync")

            // Trigger sync
            val syncResult = syncRepository.syncNow().getOrThrow()

            // Mark migration complete
            preferencesRepository.setDataMigrated(true)

            val result = MigrationResult(
                entriesMigrated = syncResult.entriesSynced,
                moodColorsMigrated = syncResult.moodColorsSynced,
                skipped = false
            )

            Timber.i("Migration completed: $result")
            Result.success(result)
        } catch (e: Exception) {
            Timber.e(e, "Migration failed")
            Result.failure(e)
        }
    }
}

data class MigrationResult(
    val entriesMigrated: Int,
    val moodColorsMigrated: Int,
    val skipped: Boolean = false
)
```

#### 8.2 Update PreferencesRepository

Add to **core/domain/repository/PreferencesRepository.kt**:
```kotlin
interface PreferencesRepository {
    // Existing methods...

    suspend fun isDataMigrated(): Boolean
    suspend fun setDataMigrated(migrated: Boolean)
}
```

Add to **core/data/repository/PreferencesRepositoryImpl.kt**:
```kotlin
override suspend fun isDataMigrated(): Boolean {
    return prefs.getBoolean(KEY_DATA_MIGRATED, false)
}

override suspend fun setDataMigrated(migrated: Boolean) {
    prefs.edit { putBoolean(KEY_DATA_MIGRATED, migrated) }
}

companion object {
    // Existing keys...
    private const val KEY_DATA_MIGRATED = "firestore_data_migrated"
}
```

---

### Phase 9: UI Updates

#### 9.1 Add Sync Status to Overview

**journal/ui/overview/OverviewViewModel.kt**:
```kotlin
class OverviewViewModel(
    // Existing dependencies...
    private val syncRepository: SyncRepository
) : ViewModel() {

    // Existing state...

    val syncStatus = syncRepository.syncStatus
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SyncStatus.Idle)

    fun onAction(action: OverviewAction) {
        when (action) {
            // Existing actions...
            is OverviewAction.SyncNow -> syncNow()
        }
    }

    private fun syncNow() {
        viewModelScope.launch {
            syncRepository.syncNow()
        }
    }
}
```

#### 9.2 Add Sync Indicator to TopAppBar

**journal/ui/overview/OverviewScreen.kt**:
```kotlin
@Composable
private fun OverviewTopBar(
    syncStatus: SyncStatus,
    onSyncClick: () -> Unit,
    // Other params...
) {
    TopAppBar(
        title = { Text("The Day To") },
        actions = {
            // Sync indicator
            when (syncStatus) {
                SyncStatus.Syncing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                }
                is SyncStatus.Error -> {
                    IconButton(onClick = onSyncClick) {
                        Icon(
                            Icons.Default.SyncProblem,
                            contentDescription = "Sync error",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                SyncStatus.Idle -> {
                    IconButton(onClick = onSyncClick) {
                        Icon(
                            Icons.Default.CloudDone,
                            contentDescription = "Synced"
                        )
                    }
                }
            }
            // Other actions...
        }
    )
}
```

---

## Testing Strategy

### Unit Tests

#### Mapper Tests
```kotlin
class EntryFirestoreMapperTest {
    @Test
    fun `toDto converts entity with mood color correctly`() {
        val entity = EntryEntity(
            moodColorId = 1,
            content = "Great day!",
            dateStamp = 1234567890L,
            lastModified = 1234567900L,
            id = 42
        )
        val moodColor = MoodColorEntity(
            mood = "Happy",
            moodNormalized = "happy",
            color = "#FF5733",
            dateStamp = 1234567800L,
            id = 1
        )

        val dto = entity.toDto(moodColor)

        assertEquals("Happy", dto.moodName)
        assertEquals("#FF5733", dto.moodColor)
        assertEquals("Great day!", dto.content)
        assertEquals(1234567890L, dto.dateStamp)
        assertEquals(42, dto.localId)
    }

    @Test
    fun `toEntity converts dto with moodColorId correctly`() {
        val dto = EntryDto(
            moodName = "Happy",
            moodColor = "#FF5733",
            content = "Great day!",
            dateStamp = 1234567890L,
            lastModified = 1234567900L,
            localId = 42,
            id = "firestore-id-123"
        )

        val entity = dto.toEntity(moodColorId = 1)

        assertEquals(1, entity.moodColorId)
        assertEquals("Great day!", entity.content)
        assertEquals(1234567890L, entity.dateStamp)
        assertEquals("firestore-id-123", entity.firestoreId)
        assertEquals(false, entity.syncPending)
    }
}
```

#### Repository Tests
```kotlin
class EntryRepositoryImplTest {
    private lateinit var entryDao: FakeEntryDao
    private lateinit var moodColorDao: FakeMoodColorDao
    private lateinit var firestoreSource: FakeFirestoreEntryDataSource
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var repository: EntryRepositoryImpl

    @Before
    fun setup() {
        entryDao = FakeEntryDao()
        moodColorDao = FakeMoodColorDao()
        firestoreSource = FakeFirestoreEntryDataSource()
        authRepository = FakeAuthRepository()

        repository = EntryRepositoryImpl(
            entryDao = entryDao,
            moodColorDao = moodColorDao,
            firestoreSource = firestoreSource,
            authRepository = authRepository,
            dispatcher = UnconfinedTestDispatcher()
        )
    }

    @Test
    fun `insertEntry saves locally with syncPending true`() = runTest {
        authRepository.setUser(UserData("user123", "Test", null))
        moodColorDao.insert(testMoodColor)

        val entry = testEntry.copy(syncPending = false)
        repository.insertEntry(entry)

        val saved = entryDao.getEntries().first().first()
        assertTrue(saved.syncPending)
        assertNotNull(saved.lastModified)
    }

    @Test
    fun `insertEntry triggers Firestore sync when user signed in`() = runTest {
        authRepository.setUser(UserData("user123", "Test", null))
        moodColorDao.insert(testMoodColor)

        repository.insertEntry(testEntry)

        assertTrue(firestoreSource.insertCalled)
    }

    @Test
    fun `insertEntry skips Firestore when user not signed in`() = runTest {
        authRepository.setUser(null)
        moodColorDao.insert(testMoodColor)

        repository.insertEntry(testEntry)

        assertFalse(firestoreSource.insertCalled)
    }
}
```

#### Sync Repository Tests
```kotlin
class SyncRepositoryImplTest {
    @Test
    fun `syncNow uploads pending entries to Firestore`() = runTest {
        // Setup pending entries
        entryDao.insertEntry(testEntry.copy(syncPending = true))
        authRepository.setUser(testUser)

        val result = syncRepository.syncNow()

        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.entriesSynced)
        assertFalse(entryDao.getEntries().first().first().syncPending)
    }

    @Test
    fun `syncNow downloads remote entries to Room`() = runTest {
        authRepository.setUser(testUser)
        firestoreSource.setRemoteEntries(listOf(remoteEntry))
        moodColorDao.insert(testMoodColor)

        syncRepository.syncNow()

        val localEntries = entryDao.getEntries().first()
        assertEquals(1, localEntries.size)
        assertEquals(remoteEntry.content, localEntries.first().content)
    }

    @Test
    fun `syncNow returns error when user not signed in`() = runTest {
        authRepository.setUser(null)

        val result = syncRepository.syncNow()

        assertTrue(result.isFailure)
    }

    @Test
    fun `hasPendingSync returns true when entries pending`() = runTest {
        entryDao.insertEntry(testEntry.copy(syncPending = true))

        assertTrue(syncRepository.hasPendingSync())
    }
}
```

### Integration Tests

```kotlin
@RunWith(AndroidJUnit4::class)
class SyncIntegrationTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: TheDayToDatabase
    private lateinit var firestore: FirebaseFirestore

    @Before
    fun setup() {
        // Use Room in-memory database
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TheDayToDatabase::class.java
        ).allowMainThreadQueries().build()

        // Use Firestore emulator
        firestore = Firebase.firestore
        firestore.useEmulator("10.0.2.2", 8080)
    }

    @Test
    fun fullSyncCycle_uploadsAndDownloadsCorrectly() = runTest {
        // 1. Insert local entry
        val entry = testEntry.copy(syncPending = true)
        database.entryDao().insertEntry(entry)

        // 2. Sync to Firestore
        syncRepository.syncNow()

        // 3. Clear local database
        database.clearAllTables()

        // 4. Sync from Firestore
        syncRepository.syncNow()

        // 5. Verify entry restored
        val restored = database.entryDao().getEntries().first()
        assertEquals(1, restored.size)
        assertEquals(entry.content, restored.first().content)
    }
}
```

### Manual Testing Checklist

- [ ] Create entry online → syncs to Firestore immediately
- [ ] Create entry offline → queued → syncs when online
- [ ] Edit entry → updates in Firestore
- [ ] Delete entry → removed from Firestore
- [ ] Sign in on Device A → create entry → sign in on Device B → entry appears
- [ ] Uninstall app → reinstall → sign in → entries restored
- [ ] Create mood color → syncs to Firestore
- [ ] Conflict: edit same entry on two devices → last-write-wins
- [ ] Network interruption during sync → retries automatically
- [ ] Sync indicator shows correct status (idle/syncing/error)

---

## File Structure

```
journal/
├── data/
│   ├── dao/
│   │   ├── EntryDao.kt              # Updated with sync queries
│   │   └── MoodColorDao.kt          # Updated with sync queries
│   ├── mapper/
│   │   ├── EntryMapper.kt           # Existing
│   │   ├── MoodColorMapper.kt       # Existing
│   │   ├── EntryFirestoreMapper.kt  # NEW
│   │   └── MoodColorFirestoreMapper.kt  # NEW
│   ├── model/
│   │   ├── EntryEntity.kt           # Updated with sync fields
│   │   └── MoodColorEntity.kt       # Updated with sync fields
│   ├── remote/
│   │   ├── dto/
│   │   │   ├── EntryDto.kt          # NEW
│   │   │   └── MoodColorDto.kt      # NEW
│   │   └── source/
│   │       ├── FirestoreEntryDataSource.kt      # NEW
│   │       └── FirestoreMoodColorDataSource.kt  # NEW
│   ├── repository/
│   │   ├── EntryRepositoryImpl.kt   # Updated with Firestore
│   │   ├── MoodColorRepositoryImpl.kt  # Updated with Firestore
│   │   └── SyncRepositoryImpl.kt    # NEW
│   └── service/
│       └── SyncWorker.kt            # NEW
├── domain/
│   ├── model/
│   │   ├── Entry.kt                 # Updated with sync fields
│   │   └── MoodColor.kt             # Updated with sync fields
│   ├── repository/
│   │   ├── EntryRepository.kt       # Existing
│   │   ├── MoodColorRepository.kt   # Existing
│   │   └── SyncRepository.kt        # NEW
│   └── usecases/
│       └── MigrateLocalDataUseCase.kt  # NEW
└── ui/
    └── overview/
        ├── OverviewViewModel.kt     # Updated with sync status
        └── OverviewScreen.kt        # Updated with sync indicator
```

---

## Implementation Order

1. **Phase 1**: Setup & Dependencies (30 min)
2. **Phase 2**: Domain Layer Updates (1 hour)
3. **Phase 3**: Entities & Mappers with tests (2 hours)
4. **Phase 4**: Firestore Data Sources with tests (2 hours)
5. **Phase 5**: Repository Updates with tests (3 hours)
6. **Phase 6**: Sync Service with tests (2 hours)
7. **Phase 7**: DI Module Updates (30 min)
8. **Phase 8**: Data Migration with tests (1 hour)
9. **Phase 9**: UI Updates (1 hour)
10. **Final**: Integration testing & manual testing (2 hours)

**Total Estimated Time**: 14-16 hours

---

## References

- [Firestore Documentation](https://firebase.google.com/docs/firestore)
- [Build Offline-First Apps](https://firebase.google.com/docs/firestore/solutions/offline-data)
- [Room + Remote Data Architecture](https://developer.android.com/topic/architecture/data-layer)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [WorkManager Guide](https://developer.android.com/topic/libraries/architecture/workmanager)
