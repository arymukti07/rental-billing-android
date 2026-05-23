package com.cuanz.rentalbilling.payments

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stub Midtrans implementation. To go live:
 *  - Add okhttp3 + moshi dependencies
 *  - Set serverKey via BuildConfig.MIDTRANS_SERVER_KEY (sandbox or production)
 *  - POST https://app.sandbox.midtrans.com/snap/v1/transactions
 *    Authorization: Basic Base64(serverKey + ":")
 *    Body: { transaction_details: { order_id, gross_amount }, customer_details: {...} }
 *  - Open snapToken in WebView with the SDK or redirect to redirectUrl
 *  - Handle webhook on backend, then call topUp() in repo on settled.
 */
@Singleton
class MidtransPaymentProvider @Inject constructor() : PaymentProvider {

    override suspend fun createTopUpToken(
        memberId: Long,
        amount: Long,
        customer: CustomerInfo
    ): TopUpToken {
        val orderId = "TOP-${memberId}-${UUID.randomUUID().toString().take(8)}"
        // TODO: real HTTP call. Returning a deterministic stub for now.
        return TopUpToken(
            orderId = orderId,
            redirectUrl = "https://app.sandbox.midtrans.com/snap/v3/redirection/$orderId",
            snapToken = "STUB-$orderId"
        )
    }

    override suspend fun checkStatus(orderId: String): PaymentStatus {
        // TODO: GET /v2/{orderId}/status
        return PaymentStatus.PENDING
    }
}
