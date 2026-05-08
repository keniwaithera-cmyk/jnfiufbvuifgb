package com.barakaplaza.rentmanager.ui.theme.screens.payments

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.barakaplaza.rentmanager.data.DatabaseHelper
import com.barakaplaza.rentmanager.data.SessionManager
import com.barakaplaza.rentmanager.models.PaymentModel
import com.barakaplaza.rentmanager.models.TenantModel
import com.barakaplaza.rentmanager.mpesa.MpesaRepository
import com.barakaplaza.rentmanager.mpesa.MpesaResult
import com.barakaplaza.rentmanager.util.AppConstants
import com.barakaplaza.rentmanager.util.ReceiptGenerator
import com.barakaplaza.rentmanager.util.SmsHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseHelper.getInstance(context)
    val building = SessionManager.getBuilding(context)
    val scope = rememberCoroutineScope()

    val tenants = remember { db.getAllActiveTenants(building) }
    var selectedTenant by remember { mutableStateOf<TenantModel?>(null) }
    var tenantExpanded by remember { mutableStateOf(false) }
    var amount by remember { mutableStateOf("") }
    var mpesaCode by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var method by remember { mutableStateOf(PaymentModel.METHOD_MPESA) }
    var stkStatus by remember { mutableStateOf("") }
    var stkLoading by remember { mutableStateOf(false) }
    var checkoutId by remember { mutableStateOf("") }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Record Payment — $building", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)),
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Paybill info card
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20)), shape = RoundedCornerShape(16.dp)) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PhoneAndroid, null, tint = Color(0xFF69F0AE), modifier = Modifier.size(36.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("M-PESA Paybill", color = Color.White, fontWeight = FontWeight.Bold)
                        Text("Paybill: ${AppConstants.MPESA_PAYBILL}", color = Color.White.copy(0.85f), fontSize = 13.sp)
                        Text("Account: ${AppConstants.MPESA_ACCOUNT}", color = Color.White.copy(0.85f), fontSize = 13.sp)
                    }
                }
            }

            // Tenant selector
            ExposedDropdownMenuBox(expanded = tenantExpanded, onExpandedChange = { tenantExpanded = it }) {
                OutlinedTextField(
                    value = selectedTenant?.let { "${it.name} — House ${it.houseNumber}" } ?: "",
                    onValueChange = {}, readOnly = true,
                    label = { Text("Select Tenant") }, leadingIcon = { Icon(Icons.Default.Person, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(tenantExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = tenantExpanded, onDismissRequest = { tenantExpanded = false }) {
                    tenants.forEach { t ->
                        DropdownMenuItem(text = { Text("${t.name} — House ${t.houseNumber}") },
                            onClick = { selectedTenant = t; tenantExpanded = false; saved = false; stkStatus = "" })
                    }
                }
            }

            // Show expected rent
            selectedTenant?.let { t ->
                val house = db.getHouses(building).firstOrNull { it.houseNumber == t.houseNumber }
                house?.let {
                    Text("Expected rent: ${it.getRentDisplay()}", fontSize = 12.sp,
                        color = Color(0xFF388E3C), fontWeight = FontWeight.SemiBold)
                    if (amount.isBlank()) amount = "%.0f".format(it.monthlyRent)
                }
            }

            OutlinedTextField(value = amount, onValueChange = { amount = it; saved = false },
                label = { Text("Amount (KSh)") }, leadingIcon = { Icon(Icons.Default.AttachMoney, null) },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))

            // Method chips
            Text("Payment Method", fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(PaymentModel.METHOD_MPESA, PaymentModel.METHOD_CASH).forEach { m ->
                    FilterChip(selected = method == m, onClick = { method = m }, label = { Text(m) })
                }
            }

            if (method == PaymentModel.METHOD_MPESA) {
                // STK Push via Retrofit Daraja API
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)),
                    enabled = amount.isNotBlank() && selectedTenant != null && !stkLoading,
                    onClick = {
                        val tenant = selectedTenant ?: return@Button
                        val amt = amount.toDoubleOrNull()?.toInt() ?: return@Button
                        val phone = MpesaRepository.formatPhone(tenant.phone)
                        stkLoading = true; stkStatus = "Sending M-PESA prompt..."
                        scope.launch {
                            when (val result = MpesaRepository.initiateStkPush(
                                phone, amt,
                                "${AppConstants.MPESA_ACCOUNT}-${tenant.houseNumber}",
                                "Rent Payment - $building House ${tenant.houseNumber}"
                            )) {
                                is MpesaResult.Success -> {
                                    checkoutId = result.data.CheckoutRequestID ?: ""
                                    stkStatus = "✅ ${result.data.CustomerMessage ?: "STK Push sent! Check your phone."}"
                                }
                                is MpesaResult.Error -> {
                                    stkStatus = "❌ ${result.message}"
                                }
                            }
                            stkLoading = false
                        }
                    }
                ) {
                    if (stkLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    else Icon(Icons.Default.PhoneAndroid, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Send M-PESA Prompt to Tenant Phone")
                }

                if (stkStatus.isNotBlank()) {
                    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = if (stkStatus.startsWith("✅")) Color(0xFFE8F5E9) else Color(0xFFFFEBEE))) {
                        Text(stkStatus, modifier = Modifier.padding(12.dp), fontSize = 13.sp,
                            color = if (stkStatus.startsWith("✅")) Color(0xFF2E7D32) else Color.Red)
                    }
                }

                OutlinedTextField(value = mpesaCode, onValueChange = { mpesaCode = it },
                    label = { Text("M-PESA Confirmation Code (after payment)") },
                    leadingIcon = { Icon(Icons.Default.Receipt, null) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true)
            }

            OutlinedTextField(value = notes, onValueChange = { notes = it },
                label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth())

            // Save payment + send SMS receipt
            Button(
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = selectedTenant != null && amount.isNotBlank(),
                onClick = {
                    val tenant = selectedTenant ?: return@Button
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val mFmt = SimpleDateFormat("MMMM", Locale.getDefault())
                    val yFmt = SimpleDateFormat("yyyy", Locale.getDefault())
                    val now = Date()
                    val dateStr = sdf.format(now)

                    val id = db.addPayment(PaymentModel(
                        tenantId = tenant.id, tenantName = tenant.name,
                        houseNumber = tenant.houseNumber, buildingName = building,
                        amount = amt, paymentMethod = method, paymentDate = dateStr,
                        paymentMonth = mFmt.format(now), paymentYear = yFmt.format(now),
                        mpesaCode = mpesaCode, status = PaymentModel.STATUS_CONFIRMED, notes = notes
                    ))

                    if (id > 0) {
                        // Get house rent for receipt
                        val house = db.getHouses(building).firstOrNull { it.houseNumber == tenant.houseNumber }
                        val rentAmt = house?.monthlyRent ?: amt

                        // Generate receipt file
                        ReceiptGenerator.generate(context, tenant,
                            PaymentModel(id = id.toInt(), tenantId = tenant.id, tenantName = tenant.name,
                                houseNumber = tenant.houseNumber, buildingName = building, amount = amt,
                                paymentMethod = method, paymentDate = dateStr,
                                paymentMonth = mFmt.format(now), paymentYear = yFmt.format(now),
                                mpesaCode = mpesaCode, status = PaymentModel.STATUS_CONFIRMED, notes = notes),
                            rentAmt)

                        // SMS receipt to tenant
                        SmsHelper.sendPaymentReceipt(context, tenant.phone, tenant.name,
                            building, tenant.houseNumber, amt, mpesaCode, dateStr)
                        // SMS alert to admin
                        SmsHelper.sendAdminPaymentAlert(context, tenant.name, building,
                            tenant.houseNumber, amt, mpesaCode)

                        saved = true
                        amount = ""; mpesaCode = ""; notes = ""
                        selectedTenant = null; stkStatus = ""
                    }
                }
            ) {
                Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp))
                Text("Save Payment & Send Receipt SMS", fontSize = 15.sp)
            }

            if (saved) {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                    shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF2E7D32))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("Payment saved! ✅", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            Text("SMS receipt sent to tenant & admin.", color = Color(0xFF2E7D32), fontSize = 13.sp)
                            Text("Receipt saved to device.", color = Color(0xFF2E7D32), fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentHistoryScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseHelper.getInstance(context)
    val building = SessionManager.getBuilding(context)
    val payments = remember { db.getAllPayments(building) }
    val total = payments.filter { it.status == PaymentModel.STATUS_CONFIRMED }.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment History — $building", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)),
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Card(modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Total Collected", color = Color.White, fontSize = 13.sp)
                        Text("KSh ${"%.0f".format(total)}", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text("${payments.size} transactions", color = Color.White.copy(0.7f), fontSize = 12.sp)
                    }
                }
            }
            items(payments) { p ->
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(3.dp)) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Receipt, null, tint = Color(0xFF1976D2), modifier = Modifier.size(30.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(p.tenantName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("House ${p.houseNumber} • ${p.paymentDate}", fontSize = 12.sp, color = Color.Gray)
                            Text(p.paymentMethod, fontSize = 11.sp, color = Color.Gray)
                            if (p.mpesaCode.isNotBlank()) Text("Code: ${p.mpesaCode}", fontSize = 11.sp, color = Color.Gray)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(p.getAmountDisplay(), fontWeight = FontWeight.Bold, color = Color(0xFF388E3C), fontSize = 15.sp)
                            Card(colors = CardDefaults.cardColors(
                                containerColor = if (p.status == PaymentModel.STATUS_CONFIRMED) Color(0xFF4CAF50) else Color(0xFFF57C00)),
                                shape = RoundedCornerShape(6.dp)) {
                                Text(p.status, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp, 2.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
