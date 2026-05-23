package com.cuanz.rentalbilling.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.cuanz.rentalbilling.billing.BillingEngine
import com.cuanz.rentalbilling.data.entity.Device
import com.cuanz.rentalbilling.data.entity.Member
import com.cuanz.rentalbilling.data.entity.RentalSession
import java.io.File
import java.io.FileOutputStream

/**
 * Generates a single-page A6-ish receipt PDF for an ended session.
 * Saves to app cache; UI shares via FileProvider.
 */
object ReceiptPdf {

    fun generate(
        ctx: Context,
        session: RentalSession,
        device: Device?,
        member: Member?,
        bill: BillingEngine.Bill,
        shopName: String = "Cuanz Rental"
    ): File {
        val doc = PdfDocument()
        val pageWidth = 300
        val pageHeight = 480
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas

        val title = Paint().apply {
            textSize = 16f
            isFakeBoldText = true
            typeface = Typeface.DEFAULT_BOLD
        }
        val body = Paint().apply { textSize = 11f }
        val small = Paint().apply { textSize = 9f }

        var y = 24
        canvas.drawText(shopName, 20f, y.toFloat(), title); y += 18
        canvas.drawText("Receipt #${session.id}", 20f, y.toFloat(), small); y += 14
        canvas.drawText(Format.datetime(session.startedAt), 20f, y.toFloat(), small); y += 18

        canvas.drawText("Device: ${device?.let { "${it.code} ${it.name}" } ?: "—"}", 20f, y.toFloat(), body); y += 14
        canvas.drawText("Member: ${member?.name ?: "Walk-in"}", 20f, y.toFloat(), body); y += 14
        canvas.drawText("Duration: ${Format.minutesToHm(bill.grossMinutes)}", 20f, y.toFloat(), body); y += 18

        canvas.drawText("Base", 20f, y.toFloat(), body)
        canvas.drawText(Format.rupiah(bill.baseAmount), 200f, y.toFloat(), body); y += 14

        if (bill.overstayAmount > 0) {
            canvas.drawText("Overstay (1.5x)", 20f, y.toFloat(), body)
            canvas.drawText(Format.rupiah(bill.overstayAmount), 200f, y.toFloat(), body); y += 14
        }
        if (bill.discountAmount > 0) {
            canvas.drawText("Discount (-)", 20f, y.toFloat(), body)
            canvas.drawText("- ${Format.rupiah(bill.discountAmount)}", 200f, y.toFloat(), body); y += 14
        }
        y += 8
        canvas.drawText("TOTAL", 20f, y.toFloat(), title)
        canvas.drawText(Format.rupiah(bill.total), 200f, y.toFloat(), title); y += 24

        canvas.drawText("Thank you for renting with us!", 20f, y.toFloat(), small)

        doc.finishPage(page)

        val dir = File(ctx.cacheDir, "receipts").apply { mkdirs() }
        val out = File(dir, "receipt-${session.id}.pdf")
        FileOutputStream(out).use { doc.writeTo(it) }
        doc.close()
        return out
    }
}
