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
import com.barakaplaza.rentmanager.data.SessionManager
import com.barakaplaza.rentmanager.models.TenantModel
import com.barakaplaza.rentmanager.navigation.ROUTE_VIEW_TENANTS
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseHelper.getInstance(context)
    val building = SessionManager.getBuilding(context)

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var idNumber by remember { mutableStateOf("") }
    var emergency by remember { mutableStateOf("") }
    var selectedHouse by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }

    val vacantHouses = remember { db.getVacantHouses(building) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Tenant — $building", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)),
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).padding(16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name *") }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone *") }, leadingIcon = { Icon(Icons.Default.Phone, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, leadingIcon = { Icon(Icons.Default.Email, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = idNumber, onValueChange = { idNumber = it }, label = { Text("National ID *") }, leadingIcon = { Icon(Icons.Default.Badge, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(value = emergency, onValueChange = { emergency = it }, label = { Text("Emergency Contact") }, leadingIcon = { Icon(Icons.Default.ContactPhone, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true)

            ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                OutlinedTextField(
                    value = selectedHouse, onValueChange = {}, readOnly = true,
                    label = { Text("Select House *") }, leadingIcon = { Icon(Icons.Default.Home, null) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    if (vacantHouses.isEmpty()) DropdownMenuItem(text = { Text("No vacant houses") }, onClick = {})
                    vacantHouses.forEach { h ->
                        DropdownMenuItem(text = { Text("${h.getDisplayName()} — ${h.getRentDisplay()}") },
                            onClick = { selectedHouse = h.houseNumber; expanded = false })
                    }
                }
            }

            if (error.isNotBlank()) Text(error, color = Color.Red, fontSize = 13.sp)

            Button(
                modifier = Modifier.fillMaxWidth().height(52.dp),
                onClick = {
                    when {
                        name.isBlank()         -> error = "Name is required"
                        phone.isBlank()        -> error = "Phone is required"
                        idNumber.isBlank()     -> error = "ID number is required"
                        selectedHouse.isBlank()-> error = "Please select a house"
                        else -> {
                            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val id = db.addTenant(TenantModel(name = name, phone = phone, email = email,
                                idNumber = idNumber, houseNumber = selectedHouse, buildingName = building,
                                moveInDate = today, emergencyContact = emergency))
                            if (id > 0) {
                                // SMS welcome to tenant + alert to admin
                                val house = db.getVacantHouses(building).firstOrNull { it.houseNumber == selectedHouse }
                                val rent = house?.monthlyRent ?: 0.0
                                com.barakaplaza.rentmanager.util.SmsHelper.sendRegistrationWelcome(
                                    context, phone, name, building, selectedHouse, rent)
                                com.barakaplaza.rentmanager.util.SmsHelper.sendNewTenantAlertToAdmin(
                                    context, name, phone, building, selectedHouse, rent)
                                navController.navigate(ROUTE_VIEW_TENANTS) { popUpTo(0) }
                            } else error = "Failed — house may already be occupied"
                        }
                    }
                }
            ) { Text("Register Tenant", fontSize = 16.sp) }
        }
    }
}
