package com.cuanz.rentalbilling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TxnKind { TOPUP, RENTAL_CHARGE, REFUND }

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val memberId: Long?,
    val sessionId: Long? = null,
    val kind: TxnKind,
    val amount: Long,                  // positive = credit, negative = debit (IDR)
    val balanceAfter: Long?,
    val createdAt: Long = System.currentTimeMillis(),
    val note: String? = null
)
