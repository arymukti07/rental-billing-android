package com.cuanz.rentalbilling.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SessionStatus { ACTIVE, ENDED, CANCELLED }

@Entity(
    tableName = "sessions",
    foreignKeys = [
        ForeignKey(entity = Device::class, parentColumns = ["id"], childColumns = ["deviceId"]),
        ForeignKey(entity = Member::class, parentColumns = ["id"], childColumns = ["memberId"])
    ],
    indices = [Index("deviceId"), Index("memberId")]
)
data class RentalSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceId: Long,
    val memberId: Long?,                // null = walk-in
    val startedAt: Long,
    val endedAt: Long? = null,
    val plannedMinutes: Int? = null,    // for package rentals (e.g. 60-min pack)
    val packageRate: Long? = null,      // package price (overrides hourly when set)
    val hourlyRateSnapshot: Long,       // copy from device at session start
    val discountPct: Int = 0,
    val status: SessionStatus = SessionStatus.ACTIVE,
    val finalBill: Long? = null,        // computed at end
    val notes: String? = null
)
