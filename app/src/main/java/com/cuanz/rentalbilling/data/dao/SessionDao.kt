package com.cuanz.rentalbilling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cuanz.rentalbilling.data.entity.RentalSession
import com.cuanz.rentalbilling.data.entity.SessionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Query("SELECT * FROM sessions WHERE status = :status ORDER BY startedAt DESC")
    fun observeByStatus(status: SessionStatus = SessionStatus.ACTIVE): Flow<List<RentalSession>>

    @Query("SELECT * FROM sessions ORDER BY startedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 50): Flow<List<RentalSession>>

    @Query("SELECT * FROM sessions WHERE id = :id")
    suspend fun byId(id: Long): RentalSession?

    @Query("SELECT * FROM sessions WHERE deviceId = :deviceId AND status = 'ACTIVE' LIMIT 1")
    suspend fun activeForDevice(deviceId: Long): RentalSession?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(session: RentalSession): Long

    @Update
    suspend fun update(session: RentalSession)

    @Query("SELECT COALESCE(SUM(finalBill), 0) FROM sessions WHERE status = 'ENDED' AND endedAt BETWEEN :from AND :to")
    suspend fun revenueBetween(from: Long, to: Long): Long
}
