package com.cuanz.rentalbilling.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.cuanz.rentalbilling.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions WHERE memberId = :memberId ORDER BY createdAt DESC LIMIT :limit")
    fun observeForMember(memberId: Long, limit: Int = 50): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 100): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(txn: Transaction): Long
}
