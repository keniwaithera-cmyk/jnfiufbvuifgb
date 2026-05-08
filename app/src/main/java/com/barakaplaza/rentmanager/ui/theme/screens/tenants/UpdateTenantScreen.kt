package com.barakaplaza.rentmanager.ui.theme.screens.tenants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.barakaplaza.rentmanager.models.TenantModel
import com.barakaplaza.rentmanager.navigation.ROUTE_VIEW_TENANTS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateTenantScreen(navController: NavController, tenantId: Int) {
    val context = LocalContext.current
    val db = DatabaseHelper.getInstance(context)
    val existing = remember { db.getTenantById(tenantId) }

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var email by remember { mutableStateOf(existing?.email ?: "") }
    var idNumber by remember { mutableStateOf(existing?.idNumber ?: "") }
    var emergency by remember { mutableStateOf(existing?.emergencyContact ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Edit Tenant", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)),
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } })
        }
    ) { padding ->
        if (existing == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Tenant not found", color = Color.Red) }
            return@Scaffold
        }
        Column(modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = existing.houseNumber, onValueChange = {}, readOnly = true, label = { Text("House (read only)") }, leadingIcon = { Icon(Icons.Default.Home, null) }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = idNumber, onValueChange = { idNumber = it }, label = { Text("ID Number") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = emergency, onValueChange = { emergency = it }, label = { Text("Emergency Contact") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(8.dp))
            Button(modifier = Modifier.fillMaxWidth().height(52.dp), onClick = {
                db.updateTenant(existing.copy(name = name, phone = phone, email = email, idNumber = idNumber, emergencyContact = emergency))
                navController.navigate(ROUTE_VIEW_TENANTS) { popUpTo(0) }
            }) { Text("Save Changes", fontSize = 16.sp) }
        }
    }
}
