package com.cuanz.rentalbilling.billing

import com.cuanz.rentalbilling.data.entity.RentalSession
import kotlin.math.ceil
import kotlin.math.max

/**
 * Pure billing engine — no Android dependencies, fully unit-testable.
 *
 * Rules:
 *  - Hourly billing rounds UP to the next 15-minute block.
 *  - Package rentals charge a flat packageRate up to plannedMinutes,
 *    then continue at hourly rate (overstay).
 *  - Overstay penalty multiplier of 1.5x is applied when a session goes past
 *    its plannedMinutes by more than the grace period.
 *  - Member discount is applied at the end (clamped 0..100).
 */
object BillingEngine {

    private const val GRACE_MINUTES = 5
    private const val OVERSTAY_MULTIPLIER = 1.5
    private const val ROUND_BLOCK_MIN = 15

    data class Bill(
        val grossMinutes: Int,
        val baseAmount: Long,
        val overstayAmount: Long,
        val discountAmount: Long,
        val total: Long,
    )

    fun computeNow(session: RentalSession, now: Long = System.currentTimeMillis()): Bill {
        val end = session.endedAt ?: now
        val grossMinutes = max(0, ((end - session.startedAt) / 60_000L).toInt())

        val planned = session.plannedMinutes
        val pkg = session.packageRate

        // Base + overstay
        val (base, overstay) = if (planned != null && pkg != null) {
            val withinPkg = grossMinutes <= planned + GRACE_MINUTES
            if (withinPkg) {
                pkg to 0L
            } else {
                val extraMin = grossMinutes - planned
                val extraBlocks = ceil(extraMin / ROUND_BLOCK_MIN.toDouble()).toInt()
                val extraHourly = (session.hourlyRateSnapshot * extraBlocks * ROUND_BLOCK_MIN) / 60L
                val penalized = (extraHourly * OVERSTAY_MULTIPLIER).toLong()
                pkg to penalized
            }
        } else {
            val blocks = ceil(grossMinutes / ROUND_BLOCK_MIN.toDouble()).toInt()
            val amt = (session.hourlyRateSnapshot * blocks * ROUND_BLOCK_MIN) / 60L
            amt to 0L
        }

        val gross = base + overstay
        val discountPct = session.discountPct.coerceIn(0, 100)
        val discount = (gross * discountPct) / 100L
        val total = gross - discount

        return Bill(
            grossMinutes = grossMinutes,
            baseAmount = base,
            overstayAmount = overstay,
            discountAmount = discount,
            total = total
        )
    }
}
