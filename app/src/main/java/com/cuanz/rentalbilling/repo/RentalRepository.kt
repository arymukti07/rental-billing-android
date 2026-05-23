package com.cuanz.rentalbilling.repo

import com.cuanz.rentalbilling.billing.BillingEngine
import com.cuanz.rentalbilling.data.dao.DeviceDao
import com.cuanz.rentalbilling.data.dao.MemberDao
import com.cuanz.rentalbilling.data.dao.SessionDao
import com.cuanz.rentalbilling.data.dao.TransactionDao
import com.cuanz.rentalbilling.data.entity.Device
import com.cuanz.rentalbilling.data.entity.DeviceStatus
import com.cuanz.rentalbilling.data.entity.Member
import com.cuanz.rentalbilling.data.entity.RentalSession
import com.cuanz.rentalbilling.data.entity.SessionStatus
import com.cuanz.rentalbilling.data.entity.Transaction
import com.cuanz.rentalbilling.data.entity.TxnKind
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RentalRepository @Inject constructor(
    private val members: MemberDao,
    private val devices: DeviceDao,
    private val sessions: SessionDao,
    private val txns: TransactionDao,
) {
    fun observeMembers() = members.observeAll()
    fun observeDevices() = devices.observeAll()
    fun observeActiveSessions() = sessions.observeByStatus(SessionStatus.ACTIVE)
    fun observeRecentSessions(limit: Int = 50) = sessions.observeRecent(limit)
    fun observeMemberTxns(memberId: Long) = txns.observeForMember(memberId)

    suspend fun upsertMember(m: Member): Long =
        if (m.id == 0L) members.insert(m) else { members.update(m); m.id }

    suspend fun upsertDevice(d: Device): Long =
        if (d.id == 0L) devices.insert(d) else { devices.update(d); d.id }

    suspend fun topUp(memberId: Long, amount: Long, note: String? = null) {
        require(amount > 0) { "amount must be positive" }
        members.adjustBalance(memberId, amount)
        val after = members.byId(memberId)?.balance
        txns.insert(
            Transaction(
                memberId = memberId,
                kind = TxnKind.TOPUP,
                amount = amount,
                balanceAfter = after,
                note = note
            )
        )
    }

    suspend fun startSession(
        deviceId: Long,
        memberId: Long?,
        plannedMinutes: Int?,
        packageRate: Long?,
        discountPct: Int = 0,
    ): Long {
        val device = devices.byId(deviceId) ?: error("Device $deviceId not found")
        check(device.status == DeviceStatus.AVAILABLE) { "Device not available" }
        val session = RentalSession(
            deviceId = deviceId,
            memberId = memberId,
            startedAt = System.currentTimeMillis(),
            plannedMinutes = plannedMinutes,
            packageRate = packageRate,
            hourlyRateSnapshot = device.hourlyRate,
            discountPct = discountPct,
            status = SessionStatus.ACTIVE,
        )
        val sid = sessions.insert(session)
        devices.setStatus(deviceId, DeviceStatus.IN_USE)
        return sid
    }

    /**
     * End a session and post the final bill.
     * If member has a balance, deduct from balance first; remainder is cash.
     */
    suspend fun endSession(sessionId: Long): EndResult {
        val s = sessions.byId(sessionId) ?: error("Session $sessionId not found")
        check(s.status == SessionStatus.ACTIVE) { "Session not active" }
        val now = System.currentTimeMillis()
        val updated = s.copy(endedAt = now, status = SessionStatus.ENDED)
        val bill = BillingEngine.computeNow(updated, now)
        val final = updated.copy(finalBill = bill.total)
        sessions.update(final)
        devices.setStatus(s.deviceId, DeviceStatus.AVAILABLE)

        var paidFromBalance = 0L
        var cashDue = bill.total
        if (s.memberId != null && bill.total > 0) {
            val mem = members.byId(s.memberId)
            if (mem != null && mem.balance > 0) {
                paidFromBalance = minOf(mem.balance, bill.total)
                if (paidFromBalance > 0) {
                    members.adjustBalance(s.memberId, -paidFromBalance)
                    val after = members.byId(s.memberId)?.balance
                    txns.insert(
                        Transaction(
                            memberId = s.memberId,
                            sessionId = sessionId,
                            kind = TxnKind.RENTAL_CHARGE,
                            amount = -paidFromBalance,
                            balanceAfter = after,
                            note = "Session #$sessionId"
                        )
                    )
                    cashDue -= paidFromBalance
                }
            }
        }
        return EndResult(bill = bill, paidFromBalance = paidFromBalance, cashDue = cashDue)
    }

    data class EndResult(val bill: BillingEngine.Bill, val paidFromBalance: Long, val cashDue: Long)
}
