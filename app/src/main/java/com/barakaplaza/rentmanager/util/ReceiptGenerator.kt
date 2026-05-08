package com.barakaplaza.rentmanager.util

import android.content.Context
import android.util.Log
import com.barakaplaza.rentmanager.models.PaymentModel
import com.barakaplaza.rentmanager.models.TenantModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ReceiptGenerator {

    private const val TAG = "ReceiptGenerator"

    fun generate(context: Context, tenant: TenantModel, payment: PaymentModel, rentAmount: Double): String? {
        return try {
            val dir = File(context.filesDir, "receipts").also { it.mkdirs() }
            val fileName = "receipt_${payment.mpesaCode.ifBlank { System.currentTimeMillis().toString() }}.txt"
            val file = File(dir, fileName)
            FileOutputStream(file).use { it.write(buildContent(tenant, payment, rentAmount).toByteArray()) }
            Log.d(TAG, "Receipt saved: ${file.absolutePath}")
            file.absolutePath
        } catch (e: Exception) {
            Log.e(TAG, "Receipt error: ${e.message}")
            null
        }
    }

    fun generateReference(houseNumber: String): String {
        val ts = SimpleDateFormat("yyMMddHHmmss", Locale.getDefault()).format(Date())
        return "BP-${houseNumber.uppercase()}-$ts"
    }

    fun read(path: String): String? =
        try { File(path).takeIf { it.exists() }?.readText() } catch (e: Exception) { null }

    private fun buildContent(tenant: TenantModel, payment: PaymentModel, rentAmount: Double): String {
        val sep  = "================================================\n"
        val thin = "------------------------------------------------\n"
        val balance = payment.amount - rentAmount
        val mpesaLine = if (payment.mpesaCode.isNotBlank()) "M-Pesa:   ${payment.mpesaCode}\n" else ""
        val notesLine = if (payment.notes.isNotBlank()) "Notes:    ${payment.notes}\n" else ""
        val balanceLine = if (balance >= 0)
            "Overpaid:     KSh ${"%.0f".format(kotlin.math.abs(balance))}\n"
        else
            "Balance Due:  KSh ${"%.0f".format(kotlin.math.abs(balance))}\n"

        return buildString {
            append(sep)
            append("        ${tenant.buildingName}\n")
            append("     RENT PAYMENT RECEIPT\n")
            append(sep)
            append("\nReceipt No: ${payment.mpesaCode.ifBlank { "CASH-${System.currentTimeMillis()}" }}\n")
            append("Date:       ${payment.paymentDate}\n\n")
            append(thin)
            append("TENANT DETAILS\n")
            append(thin)
            append("Name:     ${tenant.name}\n")
            append("Phone:    ${tenant.phone}\n")
            append("ID No:    ${tenant.idNumber}\n")
            append("House:    ${tenant.houseNumber} — ${tenant.buildingName}\n\n")
            append(thin)
            append("PAYMENT DETAILS\n")
            append(thin)
            append("Period:   ${payment.paymentMonth} ${payment.paymentYear}\n")
            append("Amount:   KSh ${"%.0f".format(payment.amount)}\n")
            append("Method:   ${payment.paymentMethod}\n")
            append(mpesaLine)
            append("Status:   ${payment.status}\n")
            append(notesLine)
            append("\n")
            append(thin)
            append("Monthly Rent: KSh ${"%.0f".format(rentAmount)}\n")
            append(balanceLine)
            append("\n")
            append(sep)
            append("  Thank you for your payment!\n")
            append("  ${tenant.buildingName} Management\n")
            append("  Next rent due: 5th of next month\n")
            append("  Admin: ${AppConstants.ADMIN_PHONE}\n")
            append(sep)
            append("\nGenerated: ${SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
        }
    }
}
