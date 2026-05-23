package com.cuanz.rentalbilling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.cuanz.rentalbilling.data.entity.Member
import kotlinx.coroutines.flow.Flow

@Dao
interface MemberDao {
    @Query("SELECT * FROM members ORDER BY name ASC")
    fun observeAll(): Flow<List<Member>>

    @Query("SELECT * FROM members WHERE id = :id")
    suspend fun byId(id: Long): Member?

    @Query("SELECT * FROM members WHERE phone = :phone LIMIT 1")
    suspend fun byPhone(phone: String): Member?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(member: Member): Long

    @Update
    suspend fun update(member: Member)

    @Query("UPDATE members SET balance = balance + :delta WHERE id = :id")
    suspend fun adjustBalance(id: Long, delta: Long)
}
