package com.barakaplaza.rentmanager.ui.theme.screens.portal

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuggestionsScreen(navController: NavController) {
    val context = LocalContext.current
    val db = DatabaseHelper.getInstance(context)
    var suggestions by remember { mutableStateOf(db.getAllSuggestions()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tenant Suggestions (${suggestions.count { !it.isRead }} unread)", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF388E3C)),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        if (suggestions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No suggestions yet", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(padding).padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(suggestions) { sug ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (sug.isRead) Color.White else Color(0xFFF3E5F5)
                        ),
                        elevation = CardDefaults.cardElevation(3.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Feedback, null, tint = Color(0xFF6A1B9A), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(sug.tenantName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("${sug.buildingName} — House ${sug.houseNumber}", fontSize = 12.sp, color = Color.Gray)
                                }
                                if (!sug.isRead) {
                                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF6A1B9A)), shape = RoundedCornerShape(6.dp)) {
                                        Text("NEW", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(4.dp, 2.dp))
                                    }
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(sug.suggestion, fontSize = 14.sp)
                            Spacer(Modifier.height(4.dp))
                            Text(sug.dateSubmitted, fontSize = 11.sp, color = Color.Gray)
                            if (!sug.isRead) {
                                Spacer(Modifier.height(8.dp))
                                TextButton(onClick = {
                                    db.markSuggestionRead(sug.id)
                                    suggestions = db.getAllSuggestions()
                                }) { Text("Mark as Read") }
                            }
                        }
                    }
                }
            }
        }
    }
}
