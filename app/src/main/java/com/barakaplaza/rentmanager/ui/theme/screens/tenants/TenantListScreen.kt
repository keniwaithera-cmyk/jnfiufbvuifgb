package com.barakaplaza.rentmanager.ui.theme.screens.tenants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.barakaplaza.rentmanager.data.DatabaseHelper
import com.barakaplaza.rentmanager.data.SessionManager
import com.barakaplaza.rentmanager.models.HouseModel
import com.barakaplaza.rentmanager.models.TenantModel
import com.barakaplaza.rentmanager.navigation.ROUTE_TENANT_PORTAL
import com.barakaplaza.rentmanager.navigation.ROUTE_UPDATE_TENANT
import com.barakaplaza.rentmanager.util.SmsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TenantListScreen(navController: NavController) {

    val context = LocalContext.current
    val db = DatabaseHelper.getInstance(context)
    val building = SessionManager.getBuilding(context)

    var tenants by remember { mutableStateOf(db.getAllActiveTenants(building)) }
    var houses by remember { mutableStateOf(db.getHouses(building)) }

    var deleteTarget by remember { mutableStateOf<TenantModel?>(null) }
    var vacateTarget by remember { mutableStateOf<TenantModel?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Tenants — $building (${tenants.size})",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        tenants.forEach { t ->
                            val house = houses.firstOrNull {
                                it.houseNumber == t.houseNumber
                            }

                            SmsHelper.sendRentReminder(
                                context,
                                t.phone,
                                t.name,
                                building,
                                t.houseNumber,
                                house?.monthlyRent ?: 0.0
                            )
                        }
                    }) {
                        Icon(Icons.Default.NotificationsActive, null, tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->

        if (tenants.isEmpty()) {
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No tenants in $building", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {

                items(tenants) { tenant ->

                    val house = houses.firstOrNull {
                        it.houseNumber == tenant.houseNumber
                    }

                    TenantCard(
                        tenant = tenant,
                        house = house,
                        onEdit = {
                            navController.navigate("$ROUTE_UPDATE_TENANT/${tenant.id}")
                        },
                        onVacate = { vacateTarget = tenant },
                        onDelete = { deleteTarget = tenant },
                        onPortal = {
                            navController.navigate("$ROUTE_TENANT_PORTAL/${tenant.id}")
                        },
                        onRemind = {
                            SmsHelper.sendRentReminder(
                                context,
                                tenant.phone,
                                tenant.name,
                                building,
                                tenant.houseNumber,
                                house?.monthlyRent ?: 0.0,
                            )
                        }
                    )
                }
            }
        }
    }

    /* ---------------- VACATE DIALOG ---------------- */

    vacateTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { vacateTarget = null },
            title = { Text("Vacate Tenant") },
            text = {
                Text("Mark ${t.name} as vacated from house ${t.houseNumber}?")
            },
            confirmButton = {
                TextButton(onClick = {
                    db.deactivateTenant(t.id, t.houseNumber, t.buildingName)
                    tenants = db.getAllActiveTenants(building)
                    vacateTarget = null
                }) {
                    Text("Vacate", color = Color(0xFFF57C00))
                }
            },
            dismissButton = {
                TextButton(onClick = { vacateTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    /* ---------------- DELETE DIALOG ---------------- */

    deleteTarget?.let { t ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("Delete Tenant") },
            text = {
                Text("⚠️ Permanently delete ${t.name} and all records?")
            },
            confirmButton = {
                TextButton(onClick = {
                    db.deleteTenant(t.id, t.houseNumber, t.buildingName)
                    tenants = db.getAllActiveTenants(building)
                    deleteTarget = null
                }) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* ---------------- TENANT CARD ---------------- */

@Composable
fun TenantCard(
    tenant: TenantModel,
    house: HouseModel?,
    onEdit: () -> Unit,
    onVacate: () -> Unit,
    onDelete: () -> Unit,
    onPortal: () -> Unit,
    onRemind: () -> Unit
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {

        Column(modifier = Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Icon(
                    Icons.Default.Person,
                    null,
                    tint = Color(0xFF388E3C),
                    modifier = Modifier.size(32.dp)
                )

                Spacer(Modifier.width(8.dp))

                Column(Modifier.weight(1f)) {

                    Text(
                        tenant.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )

                    Text(
                        "House ${tenant.houseNumber} — ${tenant.buildingName}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    // ✅ FIXED DOUBLE → STRING ERROR HERE
                    house?.let {
                        Text(
                            "Rent: KSh ${it.monthlyRent.toInt()}",
                            fontSize = 12.sp,
                            color = Color(0xFF388E3C)
                        )
                    }
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF4CAF50)),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Active",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(4.dp, 2.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            Row {
                Icon(Icons.Default.Phone, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(tenant.phone, fontSize = 13.sp)
            }

            Spacer(Modifier.height(2.dp))

            Row {
                Icon(Icons.Default.CalendarToday, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text("Since ${tenant.moveInDate}", fontSize = 12.sp, color = Color.Gray)
            }

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Edit", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onPortal,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("Portal", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onRemind,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(Icons.Default.Sms, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(2.dp))
                    Text("SMS", fontSize = 12.sp)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {

                Button(
                    onClick = onVacate,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF57C00)),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("Vacate", fontSize = 12.sp)
                }

                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}
