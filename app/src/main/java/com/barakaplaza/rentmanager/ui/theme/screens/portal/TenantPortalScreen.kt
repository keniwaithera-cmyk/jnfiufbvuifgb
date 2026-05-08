package com.barakaplaza.rentmanager.ui.theme.screens.portal

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.barakaplaza.rentmanager.data.DatabaseHelper
import com.barakaplaza.rentmanager.models.*
import com.barakaplaza.rentmanager.navigation.ROUTE_BUILDING_SELECT
import com.barakaplaza.rentmanager.util.AppConstants
import com.barakaplaza.rentmanager.mpesa.*
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantPortalScreen(navController: NavController, tenantId: Int) {

    val context = LocalContext.current
    val db = DatabaseHelper.getInstance(context)
    val scope = rememberCoroutineScope()

    val tenant = remember { db.getTenantById(tenantId) }

    if (tenant == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Tenant not found")
        }
        return
    }

    val house = remember {
        db.getHouses(tenant.buildingName)
            .firstOrNull { it.houseNumber == tenant.houseNumber }
    }

    var tab by remember { mutableIntStateOf(0) }
    val tabs = listOf("My House", "Suggestion", "Pay Promise", "Payments")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenant Portal", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1565C0)),
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate(ROUTE_BUILDING_SELECT) { popUpTo(0) }
                    }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF1565C0)) {
                tabs.forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i },
                        icon = {
                            Icon(
                                when (i) {
                                    0 -> Icons.Default.Home
                                    1 -> Icons.Default.Feedback
                                    2 -> Icons.Default.CalendarToday
                                    else -> Icons.Default.History
                                }, null
                            )
                        },
                        label = { Text(label, fontSize = 10.sp) }
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when (tab) {
                0 -> HouseTab(tenant, house)
                1 -> SuggestionTab(tenant, db)
                2 -> PromiseTab(tenant, db)
                3 -> PaymentsTab(tenantId, db, scope, context, tenant, house)
            }
        }
    }
}

/* ---------------- HOUSE TAB ---------------- */

@Composable
private fun HouseTab(tenant: TenantModel, house: HouseModel?) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(56.dp))
                Text(tenant.name, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${tenant.buildingName} — House ${tenant.houseNumber}", color = Color.White.copy(0.8f))
            }
        }

        house?.let { h ->

            if (h.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = h.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("House Details", fontWeight = FontWeight.Bold)
                    Divider()
                    InfoRow("Building", h.buildingName)
                    InfoRow("House", h.houseNumber)
                    InfoRow("Type", h.type)
                    InfoRow("Floor", h.floor)
                    InfoRow("Rent", h.getRentDisplay())
                    InfoRow("Paybill", AppConstants.MPESA_PAYBILL)
                    InfoRow("Account", AppConstants.MPESA_ACCOUNT)
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Medium)
    }
}

/* ---------------- SUGGESTION TAB ---------------- */

@Composable
private fun SuggestionTab(tenant: TenantModel, db: DatabaseHelper) {

    var text by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {

        OutlinedTextField(
            value = text,
            onValueChange = { text = it; sent = false },
            label = { Text("Suggestion") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                db.addSuggestion(
                    SuggestionModel(
                        tenantId = tenant.id,
                        tenantName = tenant.name,
                        houseNumber = tenant.houseNumber,
                        buildingName = tenant.buildingName,
                        suggestion = text,
                        dateSubmitted = today
                    )
                )

                sent = true
                text = ""
            },
            enabled = text.isNotBlank()
        ) {
            Text("Send")
        }

        if (sent) Text("✅ Sent!", color = Color.Green)
    }
}

/* ---------------- PROMISE TAB ---------------- */

@Composable
private fun PromiseTab(tenant: TenantModel, db: DatabaseHelper) {

    var amount by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var sent by remember { mutableStateOf(false) }

    Column(Modifier.padding(16.dp)) {

        OutlinedTextField(
            value = amount,
            onValueChange = { amount = it },
            label = { Text("Amount") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        OutlinedTextField(
            value = date,
            onValueChange = { date = it },
            label = { Text("Date") }
        )

        Button(
            onClick = {
                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                db.addPromise(
                    PaymentPromiseModel(
                        tenantId = tenant.id,
                        tenantName = tenant.name,
                        houseNumber = tenant.houseNumber,
                        buildingName = tenant.buildingName,
                        promisedAmount = amount.toDoubleOrNull() ?: 0.0,
                        promisedDate = date,
                        message = "",
                        dateSubmitted = today
                    )
                )

                sent = true
            }
        ) {
            Text("Send")
        }

        if (sent) Text("✅ Promise Sent", color = Color.Green)
    }
}

/* ---------------- PAYMENTS TAB ---------------- */

@Composable
private fun PaymentsTab(
    tenantId: Int,
    db: DatabaseHelper,
    scope: CoroutineScope,
    context: Context,
    tenant: TenantModel,
    house: HouseModel?
) {

    var payments by remember { mutableStateOf(listOf<PaymentModel>()) }
    var loading by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        payments = db.getPaymentsByTenant(tenantId)
    }

    val total = payments.filter { it.status == "CONFIRMED" }.sumOf { it.amount }

    Column(Modifier.fillMaxSize().padding(16.dp)) {

        Text("Total Paid: KSh ${total.toInt()}", fontWeight = FontWeight.Bold)

        if (house != null) {
            Button(
                onClick = {
                    if (tenant.phone.isBlank()) {
                        status = "❌ Invalid phone"
                        return@Button
                    }

                    loading = true

                    scope.launch {
                        val result = MpesaRepository.initiateStkPush(
                            MpesaRepository.formatPhone(tenant.phone),
                            house.monthlyRent.roundToInt(),
                            "${AppConstants.MPESA_ACCOUNT}-${tenant.houseNumber}",
                            "Rent Payment"
                        )

                        status = when (result) {
                            is MpesaResult.Success -> "✅ Payment request sent"
                            is MpesaResult.Error -> "❌ ${result.message}"
                        }

                        payments = db.getPaymentsByTenant(tenantId)
                        loading = false
                    }
                }
            ) {
                if (loading) CircularProgressIndicator()
                else Text("Pay Rent")
            }
        }

        Text(status)

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(payments) {
                Text("${it.paymentDate} - ${it.getAmountDisplay()}")
            }
        }
    }
}