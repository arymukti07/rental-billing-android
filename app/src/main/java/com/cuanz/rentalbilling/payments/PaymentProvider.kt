package com.cuanz.rentalbilling.payments

/**
 * Payment provider abstraction. Real impl uses Midtrans Snap;
 * tests and offline mode use FakePaymentProvider.
 */
interface PaymentProvider {
    suspend fun createTopUpToken(memberId: Long, amount: Long, customer: CustomerInfo): TopUpToken
    suspend fun checkStatus(orderId: String): PaymentStatus
}

data class CustomerInfo(val name: String, val phone: String, val email: String?)
data class TopUpToken(val orderId: String, val redirectUrl: String, val snapToken: String)

enum class PaymentStatus { PENDING, SETTLED, EXPIRED, CANCELLED, FAILED }
