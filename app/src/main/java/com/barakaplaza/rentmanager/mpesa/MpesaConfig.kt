package com.barakaplaza.rentmanager.mpesa

object MpesaConfig {
    // ── Sandbox credentials (for testing) ─────────────────────────────
    const val CONSUMER_KEY    = "bfqi8rxYgcBn1YUD4WEysc0AJPKrGcuDu3OSSTBwVT1AG6WA"
    const val CONSUMER_SECRET = "alAPNRzB9OMCZUcVnxLIyq5AQDFackZYliPLCtpgKR1QIx4FdveAyqIzP2DksrQE"
    const val SHORTCODE       = "174379"
    const val PASSKEY         = "bfb279f9aa9bdbcf158e97dd71a467cd2e0c893059b10f78e6b72ada1ed2c919"
    const val CALLBACK_URL    = "https://webhook.site/replace-with-yours"
    const val BASE_URL        = "https://sandbox.safaricom.co.ke/"

    // ── Production (uncomment these when going live) ───────────────────
    // const val SHORTCODE    = "247247"
    // const val PASSKEY      = "YOUR_REAL_PASSKEY_FROM_DARAJA"
    // const val CALLBACK_URL = "https://your-real-server.com/mpesa/callback"
    // const val BASE_URL     = "https://api.safaricom.co.ke/"
    // ──────────────────────────────────────────────────────────────────
}

data class MpesaAccessTokenResponse(
    val access_token: String,
    val expires_in: String
)

data class StkPushRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val TransactionType: String = "CustomerPayBillOnline",
    val Amount: String,
    val PartyA: String,
    val PartyB: String,
    val PhoneNumber: String,
    val CallBackURL: String,
    val AccountReference: String,
    val TransactionDesc: String
)

data class StkPushResponse(
    val MerchantRequestID: String?,
    val CheckoutRequestID: String?,
    val ResponseCode: String?,
    val ResponseDescription: String?,
    val CustomerMessage: String?,
    val errorCode: String? = null,
    val errorMessage: String? = null
)

data class StkQueryRequest(
    val BusinessShortCode: String,
    val Password: String,
    val Timestamp: String,
    val CheckoutRequestID: String
)

data class StkQueryResponse(
    val ResponseCode: String?,
    val ResponseDescription: String?,
    val MerchantRequestID: String?,
    val CheckoutRequestID: String?,
    val ResultCode: String?,
    val ResultDesc: String?
)