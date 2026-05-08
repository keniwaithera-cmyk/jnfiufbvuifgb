package com.barakaplaza.rentmanager.ui.theme.screens.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.barakaplaza.rentmanager.data.SessionManager
import com.barakaplaza.rentmanager.navigation.ROUTE_BUILDING_SELECT
import com.barakaplaza.rentmanager.navigation.ROUTE_DASHBOARD
import com.barakaplaza.rentmanager.navigation.ROUTE_SPLASH
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun SplashScreen(navController: NavController) {
    val context       = LocalContext.current
    val logoScale     = remember { Animatable(0f) }
    val logoAlpha     = remember { Animatable(0f) }
    val titleAlpha    = remember { Animatable(0f) }
    val subAlpha      = remember { Animatable(0f) }
    val welcomeAlpha  = remember { Animatable(0f) }
    val welcomeOffset = remember { Animatable(120f) }

    LaunchedEffect(Unit) {
        logoScale.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
        logoAlpha.animateTo(1f, tween(400))
        welcomeAlpha.animateTo(1f, tween(500))
        welcomeOffset.animateTo(0f, tween(500, easing = FastOutSlowInEasing))
        delay(200)
        titleAlpha.animateTo(1f, tween(600))
        delay(200)
        subAlpha.animateTo(1f, tween(500))
        delay(1500)

        // FIXED: If the landlord was already logged in, go straight to Dashboard.
        // Previously the splash always sent the user back to BuildingSelectScreen,
        // forcing a re-login every time the app was reopened.
        val destination = if (SessionManager.isLandlordLoggedIn(context))
            ROUTE_DASHBOARD
        else
            ROUTE_BUILDING_SELECT

        navController.navigate(destination) {
            popUpTo(ROUTE_SPLASH) { inclusive = true }
            launchSingleTop = true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFF1B5E20), Color(0xFF2E7D32), Color(0xFF43A047)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                modifier = Modifier.size(130.dp).scale(logoScale.value).alpha(logoAlpha.value)
                    .clip(CircleShape).background(Color.White.copy(0.15f)),
                contentAlignment = Alignment.Center
            ) { Icon(Icons.Default.Home, null, tint = Color.White, modifier = Modifier.size(80.dp)) }

            Text("WELCOME TO", fontSize = 18.sp, fontWeight = FontWeight.Medium,
                color = Color.White.copy(0.85f), letterSpacing = 4.sp,
                modifier = Modifier.alpha(welcomeAlpha.value).offset { IntOffset(0, welcomeOffset.value.roundToInt()) })

            Text("BARAKA PLAZA", fontSize = 36.sp, fontWeight = FontWeight.ExtraBold,
                color = Color.White, textAlign = TextAlign.Center,
                modifier = Modifier.alpha(titleAlpha.value))

            Box(modifier = Modifier.width(80.dp).height(3.dp).alpha(subAlpha.value)
                .background(Color(0xFFFFC107)))

            Text("Smart Rent Management", fontSize = 14.sp, color = Color.White.copy(0.8f),
                modifier = Modifier.alpha(subAlpha.value))

            Spacer(Modifier.height(32.dp))
            LoadingDots(Modifier.alpha(subAlpha.value))
        }
    }
}

@Composable
private fun LoadingDots(modifier: Modifier = Modifier) {
    val inf = rememberInfiniteTransition(label = "dots")
    val d1 = inf.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500), RepeatMode.Reverse), label = "d1")
    val d2 = inf.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500, delayMillis = 150), RepeatMode.Reverse), label = "d2")
    val d3 = inf.animateFloat(0.3f, 1f, infiniteRepeatable(tween(500, delayMillis = 300), RepeatMode.Reverse), label = "d3")
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        listOf(d1.value, d2.value, d3.value).forEach { a ->
            Box(Modifier.size(10.dp).alpha(a).clip(CircleShape).background(Color.White))
        }
    }
}
