package org.resqnet.sos.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import org.resqnet.sos.data.model.SosDeliveryState
import org.resqnet.sos.data.model.SosEntity

@Dao
interface SosDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSos(sos: SosEntity)

    @Update
    suspend fun updateSos(sos: SosEntity)

    @Query("UPDATE sos_messages SET deliveryState = :newState, lastUpdatedAt = :updatedAt WHERE messageId = :messageId")
    suspend fun updateDeliveryState(messageId: String, newState: SosDeliveryState, updatedAt: Long)

    @Query("SELECT * FROM sos_messages ORDER BY createdAt DESC")
    fun getAllSosFlow(): Flow<List<SosEntity>>

    @Query("SELECT * FROM sos_messages WHERE messageId = :messageId LIMIT 1")
    suspend fun getSosById(messageId: String): SosEntity?

    @Query("SELECT * FROM sos_messages WHERE incidentId = :incidentId ORDER BY createdAt DESC")
    fun getSosByIncidentFlow(incidentId: String): Flow<List<SosEntity>>

    @Query("SELECT COUNT(*) FROM sos_messages WHERE deliveryState IN ('SAVED_LOCALLY', 'QUEUED_OFFLINE', 'RELAYED_BY_PEER')")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT * FROM sos_messages WHERE deliveryState IN ('SAVED_LOCALLY', 'QUEUED_OFFLINE') ORDER BY createdAt ASC")
    suspend fun getPendingOutboundMessages(): List<SosEntity>

    @Query("DELETE FROM sos_messages WHERE messageId = :messageId")
    suspend fun deleteSosById(messageId: String)
}
