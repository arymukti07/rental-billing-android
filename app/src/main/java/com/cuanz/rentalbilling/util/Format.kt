package com.cuanz.rentalbilling.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object Format {
    private val idr = NumberFormat.getNumberInstance(Locale("id", "ID"))
    private val dt = SimpleDateFormat("dd MMM yyyy HH:mm", Locale("id", "ID"))
        .apply { timeZone = TimeZone.getTimeZone("Asia/Jakarta") }
    private val d = SimpleDateFormat("yyyy-MM-dd", Locale("id", "ID"))
        .apply { timeZone = TimeZone.getTimeZone("Asia/Jakarta") }

    fun rupiah(amount: Long): String = "Rp ${idr.format(amount)}"
    fun datetime(epochMillis: Long): String = dt.format(Date(epochMillis))
    fun date(epochMillis: Long): String = d.format(Date(epochMillis))

    fun minutesToHm(minutes: Int): String {
        val h = minutes / 60
        val m = minutes % 60
        return if (h > 0) "${h}h ${m}m" else "${m}m"
    }
}
