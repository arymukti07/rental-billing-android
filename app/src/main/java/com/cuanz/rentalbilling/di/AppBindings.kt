package com.cuanz.rentalbilling.di

import com.cuanz.rentalbilling.payments.MidtransPaymentProvider
import com.cuanz.rentalbilling.payments.PaymentProvider
import com.cuanz.rentalbilling.sync.NoOpSyncService
import com.cuanz.rentalbilling.sync.SyncService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindings {
    @Binds @Singleton
    abstract fun payments(impl: MidtransPaymentProvider): PaymentProvider

    @Binds @Singleton
    abstract fun sync(impl: NoOpSyncService): SyncService
}
