package com.cuanz.rentalbilling.sync

import com.cuanz.rentalbilling.data.entity.RentalSession
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction over a remote backend (Firebase, Supabase, custom REST).
 * Default impl is NoOpSyncService — works fully offline.
 *
 * To enable Firebase:
 *  - Add google-services plugin and google-services.json
 *  - Implement FirestoreSyncService with collection refs:
 *      branches/{branchId}/sessions/{sessionId}
 *      branches/{branchId}/devices/{deviceId}
 *      branches/{branchId}/members/{memberId}
 *  - Bind in di/SyncModule
 */
interface SyncService {
    val isEnabled: Boolean
    suspend fun pushSession(session: RentalSession)
    suspend fun pullChanges(sinceMillis: Long): Flow<RentalSession>
}
