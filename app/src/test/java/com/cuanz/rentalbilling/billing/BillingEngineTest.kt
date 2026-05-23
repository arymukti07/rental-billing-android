package com.cuanz.rentalbilling.billing

import com.cuanz.rentalbilling.data.entity.RentalSession
import com.cuanz.rentalbilling.data.entity.SessionStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for the pure billing engine.
 * Covers: hourly rounding, package + overstay penalty, member discount,
 * grace period, and 0-duration edge cases.
 */
class BillingEngineTest {

    private fun session(
        startedAt: Long = 0L,
        endedAt: Long? = null,
        plannedMinutes: Int? = null,
        packageRate: Long? = null,
        hourlyRate: Long = 10_000L,
        discountPct: Int = 0,
    ) = RentalSession(
        deviceId = 1,
        memberId = null,
        startedAt = startedAt,
        endedAt = endedAt,
        plannedMinutes = plannedMinutes,
        packageRate = packageRate,
        hourlyRateSnapshot = hourlyRate,
        discountPct = discountPct,
        status = SessionStatus.ACTIVE,
    )

    @Test fun `hourly billing rounds up to 15 minute blocks`() {
        // 1 minute → 1 block of 15 min → 2500 IDR
        val s = session(startedAt = 0L, endedAt = 60_000L) // 1 min
        val bill = BillingEngine.computeNow(s)
        assertEquals(1, bill.grossMinutes)
        assertEquals(2_500L, bill.baseAmount)
        assertEquals(0L, bill.overstayAmount)
        assertEquals(2_500L, bill.total)
    }

    @Test fun `exactly 60 minutes is one full hour`() {
        val s = session(startedAt = 0L, endedAt = 60 * 60_000L) // 60 min
        val bill = BillingEngine.computeNow(s)
        assertEquals(60, bill.grossMinutes)
        assertEquals(10_000L, bill.total)
    }

    @Test fun `61 minutes rounds up to 75 minute block`() {
        val s = session(startedAt = 0L, endedAt = 61 * 60_000L)
        val bill = BillingEngine.computeNow(s)
        // ceil(61/15)=5 blocks * 15 min @ 10000/hour = 12500
        assertEquals(12_500L, bill.total)
    }

    @Test fun `package within grace period charges flat rate`() {
        // 60-min package for 25k, used 62 min (within 5-min grace)
        val s = session(
            startedAt = 0L, endedAt = 62 * 60_000L,
            plannedMinutes = 60, packageRate = 25_000L
        )
        val bill = BillingEngine.computeNow(s)
        assertEquals(25_000L, bill.baseAmount)
        assertEquals(0L, bill.overstayAmount)
        assertEquals(25_000L, bill.total)
    }

    @Test fun `package overstay charges penalty multiplier`() {
        // 60-min package, used 90 min → 30 min over → 2 blocks of 15 min @ 1.5x
        val s = session(
            startedAt = 0L, endedAt = 90 * 60_000L,
            plannedMinutes = 60, packageRate = 25_000L
        )
        val bill = BillingEngine.computeNow(s)
        assertEquals(25_000L, bill.baseAmount)
        // extraMin = 30, ceil(30/15)=2 blocks, 2*15*10000/60 = 5000, *1.5 = 7500
        assertEquals(7_500L, bill.overstayAmount)
        assertEquals(32_500L, bill.total)
    }

    @Test fun `member discount applied to total`() {
        val s = session(
            startedAt = 0L, endedAt = 60 * 60_000L,
            discountPct = 20
        )
        val bill = BillingEngine.computeNow(s)
        assertEquals(10_000L, bill.baseAmount)
        assertEquals(2_000L, bill.discountAmount)
        assertEquals(8_000L, bill.total)
    }

    @Test fun `discount clamped to valid range`() {
        val s = session(
            startedAt = 0L, endedAt = 60 * 60_000L,
            discountPct = 250 // invalid
        )
        val bill = BillingEngine.computeNow(s)
        // clamped to 100% → total 0
        assertEquals(0L, bill.total)
    }

    @Test fun `zero duration session bills zero`() {
        val s = session(startedAt = 1000L, endedAt = 1000L)
        val bill = BillingEngine.computeNow(s)
        assertEquals(0, bill.grossMinutes)
        assertEquals(0L, bill.total)
    }

    @Test fun `negative duration treated as zero`() {
        val s = session(startedAt = 2000L, endedAt = 1000L)
        val bill = BillingEngine.computeNow(s)
        assertEquals(0, bill.grossMinutes)
        assertEquals(0L, bill.total)
    }

    @Test fun `package and discount stack correctly`() {
        // 60-min package 25k, 90 min usage, 10% discount
        // gross = 25000 + 7500 = 32500, discount 10% = 3250, total 29250
        val s = session(
            startedAt = 0L, endedAt = 90 * 60_000L,
            plannedMinutes = 60, packageRate = 25_000L,
            discountPct = 10
        )
        val bill = BillingEngine.computeNow(s)
        assertEquals(3_250L, bill.discountAmount)
        assertEquals(29_250L, bill.total)
    }
}
