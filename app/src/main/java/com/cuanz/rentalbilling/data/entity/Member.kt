package com.cuanz.rentalbilling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "members")
data class Member(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String? = null,
    val balance: Long = 0,            // in IDR cents (rupiah * 100) — store as long
    val tier: String = "REGULAR",     // REGULAR / VIP
    val createdAt: Long = System.currentTimeMillis()
)
