package com.barakaplaza.rentmanager.mpesa

import android.util.Base64
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

sealed class MpesaResult<out T> {
    data class Success<T>(val data: T) : MpesaResult<T>()
    data class Error(val message: String) : MpesaResult<Nothing>()
}

object MpesaRepository {

    private const val TAG = "MpesaRepository"

    suspend fun getAccessToken(): MpesaResult<String> {
        return try {
            val credentials = "${MpesaConfig.CONSUMER_KEY}:${MpesaConfig.CONSUMER_SECRET}"
            val encoded = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
            val response = DarajaClient.apiService.getAccessToken("Basic $encoded")
            if (response.isSuccessful) {
                val token = response.body()?.access_token
                if (!token.isNullOrEmpty()) MpesaResult.Success(token)
                else MpesaResult.Error("Empty access token")
            } else {
                MpesaResult.Error("Auth failed: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Token error: ${e.message}")
            MpesaResult.Error("Network error: ${e.message ?: "Unknown"}")
        }
    }

    suspend fun initiateStkPush(
        phoneNumber: String,
        amount: Int,
        accountRef: String,
        description: String
    ): MpesaResult<StkPushResponse> {
        val tokenResult = getAccessToken()
        if (tokenResult is MpesaResult.Error) return tokenResult
        val accessToken = (tokenResult as MpesaResult.Success).data

        return try {
            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val rawPassword = "${MpesaConfig.SHORTCODE}${MpesaConfig.PASSKEY}$timestamp"
            val password = Base64.encodeToString(rawPassword.toByteArray(), Base64.NO_WRAP)

            val request = StkPushRequest(
                BusinessShortCode = MpesaConfig.SHORTCODE,
                Password          = password,
                Timestamp         = timestamp,
                Amount            = amount.toString(),
                PartyA            = phoneNumber,
                PartyB            = MpesaConfig.SHORTCODE,
                PhoneNumber       = phoneNumber,
                CallBackURL       = MpesaConfig.CALLBACK_URL,
                AccountReference  = accountRef,
                TransactionDesc   = description
            )

            val response = DarajaClient.apiService.initiateStkPush("Bearer $accessToken", request)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.ResponseCode == "0") MpesaResult.Success(body)
                else MpesaResult.Error(body?.ResponseDescription ?: body?.errorMessage ?: "STK Push failed")
            } else {
                MpesaResult.Error("STK error: ${response.code()} - ${response.message()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "STK Push error: ${e.message}")
            MpesaResult.Error("Network error: ${e.message ?: "Unknown"}")
        }
    }

    suspend fun queryStkStatus(checkoutRequestId: String): MpesaResult<StkQueryResponse> {
        val tokenResult = getAccessToken()
        if (tokenResult is MpesaResult.Error) return tokenResult
        val accessToken = (tokenResult as MpesaResult.Success).data
        return try {
            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val rawPassword = "${MpesaConfig.SHORTCODE}${MpesaConfig.PASSKEY}$timestamp"
            val password = Base64.encodeToString(rawPassword.toByteArray(), Base64.NO_WRAP)
            val request = StkQueryRequest(MpesaConfig.SHORTCODE, password, timestamp, checkoutRequestId)
            val response = DarajaClient.apiService.queryStkStatus("Bearer $accessToken", request)
            if (response.isSuccessful) MpesaResult.Success(response.body()!!)
            else MpesaResult.Error("Query failed: ${response.code()}")
        } catch (e: Exception) {
            MpesaResult.Error("Query error: ${e.message ?: "Unknown"}")
        }
    }

    fun formatPhone(phone: String): String {
        val c = phone.trim().replace("\\s+".toRegex(), "")
        return when {
            c.startsWith("+254") -> c.removePrefix("+")
            c.startsWith("0")    -> "254${c.removePrefix("0")}"
            c.startsWith("254")  -> c
            else                 -> "254$c"
        }
    }
}
