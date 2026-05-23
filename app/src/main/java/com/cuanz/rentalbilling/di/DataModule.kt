package com.cuanz.rentalbilling.di

import android.content.Context
import com.cuanz.rentalbilling.data.RentalDatabase
import com.cuanz.rentalbilling.data.dao.DeviceDao
import com.cuanz.rentalbilling.data.dao.MemberDao
import com.cuanz.rentalbilling.data.dao.SessionDao
import com.cuanz.rentalbilling.data.dao.TransactionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun db(@ApplicationContext ctx: Context): RentalDatabase = RentalDatabase.build(ctx)

    @Provides fun members(db: RentalDatabase): MemberDao = db.memberDao()
    @Provides fun devices(db: RentalDatabase): DeviceDao = db.deviceDao()
    @Provides fun sessions(db: RentalDatabase): SessionDao = db.sessionDao()
    @Provides fun txns(db: RentalDatabase): TransactionDao = db.transactionDao()
}
