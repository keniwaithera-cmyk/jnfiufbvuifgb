package com.barakaplaza.rentmanager.ui.theme.screens.houses

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.barakaplaza.rentmanager.data.SessionManager
import com.barakaplaza.rentmanager.models.HouseModel
import com.barakaplaza.rentmanager.util.CloudinaryHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HousesScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseHelper.getInstance(context)
    val building = SessionManager.getBuilding(context)
    var houses by remember { mutableStateOf(db.getHouses(building)) }
    var showAdd by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Houses — $building", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)),
                navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                actions = { IconButton(onClick = { showAdd = true }) { Icon(Icons.Default.Add, null, tint = Color.White) } }
            )
        }
    ) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            items(houses) { house ->
                HouseCard(house = house, onImageUploaded = { url ->
                    db.updateHouseImage(house.houseNumber, building, url)
                    houses = db.getHouses(building)
                })
            }
        }
    }

    if (showAdd) {
        AddHouseDialog(building = building, onDismiss = { showAdd = false }, onAdd = { h ->
            db.addHouse(h); houses = db.getHouses(building); showAdd = false
        })
    }
}

@Composable
fun HouseCard(house: HouseModel, onImageUploaded: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var uploading by remember { mutableStateOf(false) }
    var uploadMsg by remember { mutableStateOf("") }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { uploading = true; uploadMsg = ""
            scope.launch {
                val url = CloudinaryHelper.uploadImage(context, it)
                uploading = false
                if (url != null) { onImageUploaded(url); uploadMsg = "✅ Photo uploaded!" }
                else uploadMsg = "❌ Upload failed. Set CLOUDINARY_CLOUD_NAME in AppConstants."
            }
        }
    }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = if (house.isOccupied) Color(0xFFFFF3E0) else Color(0xFFE8F5E9))) {
        Column {
            if (house.imageUrl.isNotBlank()) {
                AsyncImage(model = house.imageUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(160.dp), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Home, null, tint = if (house.isOccupied) Color(0xFFF57C00) else Color(0xFF388E3C), modifier = Modifier.size(52.dp))
                }
            }
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(house.getDisplayName(), fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(house.getRentDisplay(), fontSize = 13.sp, color = Color.Gray)
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = if (house.isOccupied) Color(0xFFF57C00) else Color(0xFF4CAF50)), shape = RoundedCornerShape(8.dp)) {
                        Text(if (house.isOccupied) "Occupied" else "Vacant", color = Color.White, fontSize = 11.sp, modifier = Modifier.padding(6.dp, 3.dp))
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = { picker.launch("image/*") }, modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, Color(0xFF388E3C)), enabled = !uploading) {
                    if (uploading) { CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp); Spacer(Modifier.width(8.dp)); Text("Uploading...") }
                    else { Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(6.dp)); Text(if (house.imageUrl.isNotBlank()) "Update Photo" else "Add House Photo") }
                }
                if (uploadMsg.isNotBlank()) Text(uploadMsg, fontSize = 12.sp, color = if (uploadMsg.startsWith("✅")) Color(0xFF388E3C) else Color.Red)
            }
        }
    }
}

@Composable
fun AddHouseDialog(building: String, onDismiss: () -> Unit, onAdd: (HouseModel) -> Unit) {
    var num by remember { mutableStateOf("") }
    var floor by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var rent by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add House to $building") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = num, onValueChange = { num = it }, label = { Text("House Number") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = floor, onValueChange = { floor = it }, label = { Text("Floor") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = type, onValueChange = { type = it }, label = { Text("Type (e.g. 1 Bedroom)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = rent, onValueChange = { rent = it }, label = { Text("Monthly Rent (KSh)") }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = { TextButton(onClick = {
            val r = rent.toDoubleOrNull() ?: 0.0
            if (num.isNotBlank() && r > 0) onAdd(HouseModel(houseNumber = num, buildingName = building, floor = floor, type = type, monthlyRent = r, description = desc))
        }) { Text("Add") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
