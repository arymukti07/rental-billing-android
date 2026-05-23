package com.cuanz.rentalbilling.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import com.cuanz.rentalbilling.data.dao.DeviceDao
import com.cuanz.rentalbilling.data.dao.MemberDao
import com.cuanz.rentalbilling.data.dao.SessionDao
import com.cuanz.rentalbilling.data.dao.TransactionDao
import com.cuanz.rentalbilling.data.entity.Device
import com.cuanz.rentalbilling.data.entity.Member
import com.cuanz.rentalbilling.data.entity.RentalSession
import com.cuanz.rentalbilling.data.entity.Transaction

@Database(
    entities = [Member::class, Device::class, RentalSession::class, Transaction::class],
    version = 1,
    exportSchema = false
)
abstract class RentalDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDao
    abstract fun deviceDao(): DeviceDao
    abstract fun sessionDao(): SessionDao
    abstract fun transactionDao(): TransactionDao

    companion object {
        fun build(ctx: Context): RentalDatabase =
            Room.databaseBuilder(ctx, RentalDatabase::class.java, "rental.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
