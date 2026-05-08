package com.barakaplaza.rentmanager.ui.theme.screens.building

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController  // ✅ NavController comes from here
// ❌ REMOVED: import androidx.navigation.navigate  ← this was causing the red error
import com.barakaplaza.rentmanager.data.SessionManager
import com.barakaplaza.rentmanager.models.BuildingModel
import com.barakaplaza.rentmanager.navigation.ROUTE_LOGIN
import com.barakaplaza.rentmanager.navigation.ROUTE_TENANT_REGISTER

@Composable
fun BuildingSelectScreen(navController: NavController) {
    val context = LocalContext.current
    var showRoleDialog by remember { mutableStateOf(false) }
    var selectedBuilding by remember { mutableStateOf("") }

    val colors = listOf(
        Color(0xFF388E3C), Color(0xFF1565C0), Color(0xFF6A1B9A), Color(0xFF00695C)
    )

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32)))
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))

            Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(64.dp))
            Text("Baraka Plaza Group", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Select your building to continue", fontSize = 14.sp, color = Color.White.copy(0.8f))

            Spacer(Modifier.height(32.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(BuildingModel.BUILDINGS.zip(colors)) { (building, color) ->
                    BuildingCard(
                        building = building,
                        color = color,
                        onClick = {
                            selectedBuilding = building.name
                            SessionManager.setBuilding(context, building.name)
                            showRoleDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showRoleDialog) {
        AlertDialog(
            onDismissRequest = { showRoleDialog = false },
            title = { Text(selectedBuilding, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Who are you?", fontSize = 16.sp)

                    Button(
                        onClick = {
                            showRoleDialog = false
                            navController.navigate(ROUTE_LOGIN)  // ✅ works via NavController
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                    ) {
                        Icon(Icons.Default.AdminPanelSettings, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Landlord / Admin")
                    }

                    OutlinedButton(
                        onClick = {
                            showRoleDialog = false
                            navController.navigate(ROUTE_TENANT_REGISTER)  // ✅ works via NavController
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Person, null)
                        Spacer(Modifier.width(8.dp))
                        Text("I am a Tenant")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showRoleDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun BuildingCard(building: BuildingModel, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(56.dp).background(Color.White.copy(0.2f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Apartment, null, tint = Color.White, modifier = Modifier.size(36.dp)) }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(building.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(building.location, fontSize = 13.sp, color = Color.White.copy(0.8f))
                Text("${building.totalHouses} units", fontSize = 12.sp, color = Color.White.copy(0.7f))
            }

            Icon(Icons.Default.ArrowForwardIos, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}