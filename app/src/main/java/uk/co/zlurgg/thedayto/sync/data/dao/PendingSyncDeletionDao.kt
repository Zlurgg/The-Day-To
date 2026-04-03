package uk.co.zlurgg.thedayto.sync.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import uk.co.zlurgg.thedayto.sync.data.model.PendingSyncDeletionEntity

@Dao
interface PendingSyncDeletionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(deletion: PendingSyncDeletionEntity)

    @Query("SELECT * FROM pending_sync_deletion WHERE collection = :collection")
    suspend fun getByCollection(collection: String): List<PendingSyncDeletionEntity>

    @Query("SELECT * FROM pending_sync_deletion")
    suspend fun getAll(): List<PendingSyncDeletionEntity>

    @Query("DELETE FROM pending_sync_deletion WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("DELETE FROM pending_sync_deletion WHERE syncId = :syncId AND collection = :collection")
    suspend fun deleteBySyncId(syncId: String, collection: String)
}
