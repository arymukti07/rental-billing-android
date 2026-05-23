package com.cuanz.rentalbilling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cuanz.rentalbilling.data.entity.Device
import com.cuanz.rentalbilling.data.entity.DeviceStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceDao {
    @Query("SELECT * FROM devices ORDER BY code ASC")
    fun observeAll(): Flow<List<Device>>

    @Query("SELECT * FROM devices WHERE status = :status")
    fun observeByStatus(status: DeviceStatus): Flow<List<Device>>

    @Query("SELECT * FROM devices WHERE id = :id")
    suspend fun byId(id: Long): Device?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(device: Device): Long

    @Update
    suspend fun update(device: Device)

    @Query("UPDATE devices SET status = :status WHERE id = :id")
    suspend fun setStatus(id: Long, status: DeviceStatus)
}
