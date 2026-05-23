package com.cuanz.rentalbilling.sync

import com.cuanz.rentalbilling.data.entity.RentalSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import javax.inject.Inject
import javax.inject.Singleton

/** Default sync impl — does nothing. Replace with Firestore/Supabase when ready. */
@Singleton
class NoOpSyncService @Inject constructor() : SyncService {
    override val isEnabled: Boolean = false
    override suspend fun pushSession(session: RentalSession) = Unit
    override suspend fun pullChanges(sinceMillis: Long): Flow<RentalSession> = emptyFlow()
}
