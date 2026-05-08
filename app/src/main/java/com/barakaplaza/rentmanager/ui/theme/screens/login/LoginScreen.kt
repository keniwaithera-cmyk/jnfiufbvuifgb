package com.barakaplaza.rentmanager.ui.theme.screens.login

import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.barakaplaza.rentmanager.data.DatabaseHelper
import com.barakaplaza.rentmanager.data.SessionManager
import com.barakaplaza.rentmanager.navigation.ROUTE_BUILDING_SELECT
import com.barakaplaza.rentmanager.navigation.ROUTE_DASHBOARD

@Composable
fun LoginScreen(navController: NavController) {
    var phone     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var error     by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val context  = LocalContext.current
    val building = remember { SessionManager.getBuilding(context) }

    // FIXED: isLoading is reset to false BEFORE navigating so there is no
    // state update on an already-disposed composable (which caused the crash).
    fun doLogin() {
        if (phone.isBlank() || password.isBlank()) {
            error = "Please enter phone and password"
            return
        }
        isLoading = true
        error = ""
        try {
            val db    = DatabaseHelper.getInstance(context)
            val valid = db.validateLogin(phone.trim(), password.trim())
            if (valid) {
                SessionManager.setLandlordLoggedIn(context, true)
                isLoading = false          // reset BEFORE navigate
                navController.navigate(ROUTE_DASHBOARD) {
                    popUpTo(0) { inclusive = true }
                    launchSingleTop = true // prevent duplicate back-stack entry
                }
            } else {
                error     = "Wrong phone or password.\nDefault: 0726352338 / admin1234"
                isLoading = false
            }
        } catch (e: Exception) {
            Log.e("LoginScreen", "Login error: ${e.message}", e)
            error     = "Login error: ${e.message}"
            isLoading = false
        }
    }

    // FIXED: same pattern — reset state before navigating, launchSingleTop added.
    fun showBiometric() {
        try {
            val activity = context as? FragmentActivity
            if (activity == null) {
                error = "Biometric not supported on this device"
                return
            }
            val bm = BiometricManager.from(context)
            if (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                != BiometricManager.BIOMETRIC_SUCCESS) {
                error = "Fingerprint not set up on this device"
                return
            }
            val executor = ContextCompat.getMainExecutor(context)
            val prompt = BiometricPrompt(
                activity, executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        SessionManager.setLandlordLoggedIn(context, true)
                        navController.navigate(ROUTE_DASHBOARD) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                    override fun onAuthenticationError(code: Int, msg: CharSequence) {
                        error = "Fingerprint error: $msg"
                    }
                    override fun onAuthenticationFailed() {
                        error = "Fingerprint not recognised. Try again."
                    }
                }
            )
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("$building — Landlord Login")
                .setSubtitle("Touch fingerprint sensor")
                .setNegativeButtonText("Use Password")
                .build()
                .also { prompt.authenticate(it) }
        } catch (e: Exception) {
            error = "Biometric error: ${e.message}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047))
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(68.dp))
            }

            Spacer(Modifier.height(10.dp))
            Text(building, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Text("Landlord Login", fontSize = 14.sp, color = Color.White.copy(0.8f))
            Spacer(Modifier.height(24.dp))

            // Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it; error = "" },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    Spacer(Modifier.height(10.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; error = "" },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )

                    // Error message
                    if (error.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                error,
                                color = Color.Red,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Login button
                    Button(
                        onClick = { doLogin() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF388E3C))
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Login, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Fingerprint button
                    OutlinedButton(
                        onClick = { showBiometric() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.Fingerprint, null, tint = Color(0xFF388E3C))
                        Spacer(Modifier.width(8.dp))
                        Text("Login with Fingerprint", color = Color(0xFF388E3C))
                    }

                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Default: 0726352338 / admin1234",
                        fontSize = 12.sp, color = Color.Gray
                    )

                    Spacer(Modifier.height(6.dp))
                    TextButton(onClick = {
                        navController.navigate(ROUTE_BUILDING_SELECT) {
                            popUpTo(0) { inclusive = true }
                            launchSingleTop = true
                        }
                    }) {
                        Text("← Change Building", color = Color.Gray, fontSize = 13.sp)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
