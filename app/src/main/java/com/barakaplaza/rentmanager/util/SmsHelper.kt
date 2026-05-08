package com.barakaplaza.rentmanager.util

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast

object SmsHelper {
    private const val TAG = "SmsHelper"

    fun send(context: Context, phone: String, message: String) {
        if (phone.isBlank()) return
        try {
            val formatted = formatPhone(phone)
            @Suppress("DEPRECATION")
            val sms: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                context.getSystemService(SmsManager::class.java)
            else SmsManager.getDefault()
            val parts = sms.divideMessage(message)
            sms.sendMultipartTextMessage(formatted, null, parts, null, null)
            Log.d(TAG, "SMS sent to $formatted")
        } catch (e: Exception) {
            Log.e(TAG, "SMS failed: ${e.message}")
            Toast.makeText(context, "SMS failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun sendRegistrationWelcome(context: Context, phone: String, name: String,
                                 building: String, house: String, rent: Double) {
        send(context, phone, "Welcome to $building!\nDear $name, you are registered.\nHouse: $house\nRent: KSh ${"%.0f".format(rent)}/month\nPaybill: ${AppConstants.MPESA_PAYBILL}\nAccount: ${AppConstants.MPESA_ACCOUNT}\nDue on 5th monthly.")
    }

    fun sendPaymentReceipt(context: Context, phone: String, name: String,
                            building: String, house: String, amount: Double,
                            mpesaCode: String, date: String) {
        val code = if (mpesaCode.isNotBlank()) "\nM-PESA: $mpesaCode" else ""
        send(context, phone, "=== $building RECEIPT ===\nTenant: $name\nHouse: $house\nAmount: KSh ${"%.0f".format(amount)}\nDate: $date$code\nStatus: CONFIRMED\nThank you!\nAdmin: ${AppConstants.ADMIN_PHONE}")
    }

    fun sendAdminPaymentAlert(context: Context, name: String, building: String,
                               house: String, amount: Double, mpesaCode: String) {
        val code = if (mpesaCode.isNotBlank()) "\nM-PESA: $mpesaCode" else ""
        send(context, AppConstants.ADMIN_PHONE, "PAYMENT - $building\nTenant: $name\nHouse: $house\nAmount: KSh ${"%.0f".format(amount)}$code")
    }

    fun sendRentReminder(context: Context, phone: String, name: String,
                          building: String, house: String, rent: Double) {
        send(context, phone, "RENT REMINDER - $building\nDear $name, your rent of KSh ${"%.0f".format(rent)} for House $house is due by 5th.\nPaybill: ${AppConstants.MPESA_PAYBILL}, Account: ${AppConstants.MPESA_ACCOUNT}")
    }

    fun sendNewTenantAlertToAdmin(context: Context, name: String, phone: String,
                                   building: String, house: String, rent: Double) {
        send(context, AppConstants.ADMIN_PHONE, "NEW TENANT - $building\nName: $name\nPhone: $phone\nHouse: $house\nRent: KSh ${"%.0f".format(rent)}")
    }

    private fun formatPhone(phone: String): String {
        val c = phone.trim().replace("\\s+".toRegex(), "")
        return when {
            c.startsWith("+254") -> c
            c.startsWith("0")    -> "+254${c.removePrefix("0")}"
            c.startsWith("254")  -> "+$c"
            else                 -> c
        }
    }
}
