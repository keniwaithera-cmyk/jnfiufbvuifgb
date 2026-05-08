package com.barakaplaza.rentmanager.ui.theme.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.barakaplaza.rentmanager.data.DatabaseHelper
import com.barakaplaza.rentmanager.data.SessionManager
import com.barakaplaza.rentmanager.navigation.*
import java.text.SimpleDateFormat
import java.util.*

// ── Purple Palette ────────────────────────────────────────────────────────
private val Purple900  = Color(0xFF4A148C)  // darkest — status bar, top bar
private val Purple800  = Color(0xFF6A1B9A)  // hero card, bottom bar
private val Purple700  = Color(0xFF7B1FA2)  // primary buttons
private val Purple500  = Color(0xFF9C27B0)  // stat cards
private val PurpleAccent = Color(0xFFCE93D8) // notification card bg
private val DeepViolet = Color(0xFF311B92)  // collection card

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseHelper.getInstance(context)
    val building = SessionManager.getBuilding(context)
    val landlordName = remember { db.getLandlordName() }
    val month = remember { SimpleDateFormat("MMMM", Locale.getDefault()).format(Date()) }
    val year  = remember { SimpleDateFormat("yyyy", Locale.getDefault()).format(Date()) }

    var tenantCount by remember { mutableIntStateOf(db.countActiveTenants(building)) }
    var vacantCount by remember { mutableIntStateOf(db.countVacantHouses(building)) }
    var monthTotal  by remember { mutableDoubleStateOf(db.totalCollectedThisMonth(month, year, building)) }
    var unreadSugg  by remember { mutableIntStateOf(db.countUnreadSuggestions()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(building, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Welcome, $landlordName", fontSize = 12.sp, color = Color.White.copy(0.8f))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Purple900  // 🟣 was green
                ),
                actions = {
                    TextButton(onClick = {
                        SessionManager.logout(context)
                        navController.navigate(ROUTE_BUILDING_SELECT) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) { Text("Logout", color = Color.White) }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Purple900) { // 🟣 was dark green
                NavigationBarItem(selected = true,  onClick = {}, icon = { Icon(Icons.Default.Home, null) }, label = { Text("Home") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(ROUTE_ADD_TENANT) },    icon = { Icon(Icons.Default.PersonAdd, null) }, label = { Text("Add") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(ROUTE_VIEW_TENANTS) },  icon = { Icon(Icons.Default.People, null) },    label = { Text("Tenants") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(ROUTE_PAYMENTS) },      icon = { Icon(Icons.Default.Payment, null) },   label = { Text("Pay") })
                NavigationBarItem(selected = false, onClick = { navController.navigate(ROUTE_HOUSES) },        icon = { Icon(Icons.Default.Apartment, null) }, label = { Text("Houses") })
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero collection card 🟣
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DeepViolet), // 🟣 was dark green
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("$month Collection", color = Color.White, fontSize = 13.sp)
                    Text("KSh ${"%.0f".format(monthTotal)}", fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                    Text("Paybill: 247247 | Acc: 0726352340", color = Color.White.copy(0.75f), fontSize = 12.sp)
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(Modifier.weight(1f), "Tenants", tenantCount.toString(), Purple700)  // 🟣
                StatCard(Modifier.weight(1f), "Vacant",  vacantCount.toString(), Color(0xFF4527A0)) // 🟣 deep purple
            }

            if (unreadSugg > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { navController.navigate(ROUTE_SUGGESTIONS) },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEDE7F6)) // 🟣 light purple bg
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Notifications, null, tint = Purple800)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "$unreadSugg new tenant suggestion(s) — tap to view",
                            color = Purple800,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Text("Quick Actions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Purple900)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(Modifier.weight(1f), "Add Tenant",   Icons.Default.PersonAdd, Purple700)       { navController.navigate(ROUTE_ADD_TENANT) }
                ActionCard(Modifier.weight(1f), "View Tenants", Icons.Default.People,    Color(0xFF4527A0)) { navController.navigate(ROUTE_VIEW_TENANTS) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(Modifier.weight(1f), "Record Payment",  Icons.Default.Payment, Purple800)        { navController.navigate(ROUTE_PAYMENTS) }
                ActionCard(Modifier.weight(1f), "Payment History", Icons.Default.History, Color(0xFF6200EA)) { navController.navigate(ROUTE_PAYMENT_HISTORY) }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard(Modifier.weight(1f), "Manage Houses", Icons.Default.Apartment, Purple500)        { navController.navigate(ROUTE_HOUSES) }
                ActionCard(Modifier.weight(1f), "Suggestions",   Icons.Default.Feedback,  Color(0xFF37474F)) { navController.navigate(ROUTE_SUGGESTIONS) }
            }
        }
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, color = Color.White, fontSize = 12.sp)
            Spacer(Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActionCard(modifier: Modifier, label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(32.dp))
            Spacer(Modifier.height(6.dp))
            Text(label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}