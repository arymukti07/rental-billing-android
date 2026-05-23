package com.cuanz.rentalbilling.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DeviceType { PHONE, TABLET }
enum class DeviceStatus { AVAILABLE, IN_USE, MAINTENANCE }

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val code: String,                  // e.g. "TAB-01"
    val name: String,                  // e.g. "iPad 9th Gen"
    val type: DeviceType = DeviceType.TABLET,
    val hourlyRate: Long,              // IDR per hour
    val status: DeviceStatus = DeviceStatus.AVAILABLE,
    val notes: String? = null
)
