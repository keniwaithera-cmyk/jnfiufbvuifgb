package com.barakaplaza.rentmanager.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView

// ── Purple Palette ─────────────────────────────────────────────────────────
val Purple900  = Color(0xFF4A148C)  // deep purple - primary
val Purple700  = Color(0xFF7B1FA2)  // medium purple
val Purple500  = Color(0xFF9C27B0)  // light purple
val PurpleAccent = Color(0xFFE040FB) // bright accent
val DeepViolet = Color(0xFF311B92)  // dark violet

// ── Supporting colours (kept from original) ────────────────────────────────
val Blue700    = Color(0xFF1976D2)
val Teal600    = Color(0xFF00897B)
val Orange700  = Color(0xFFF57C00)
val Red700     = Color(0xFFD32F2F)
val Amber500   = Color(0xFFFFC107)

private val ColorScheme = lightColorScheme(
    primary        = Purple700,      // main buttons, FABs, top bars
    onPrimary      = Color.White,    // text/icons on primary
    primaryContainer   = Purple900,  // darker containers
    secondary      = PurpleAccent,   // secondary actions
    onSecondary    = Color.White,
    tertiary       = Teal600,        // accents
    error          = Red700,
    background     = Color(0xFFF3E5F5), // very light purple background
    surface        = Color.White,
    onBackground   = Purple900,
    onSurface      = Purple900,
)

@Composable
fun BarakaPlazaTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            @Suppress("DEPRECATION")
            (view.context as Activity).window.statusBarColor = Purple900.toArgb() // 👈 purple status bar
        }
    }
    MaterialTheme(colorScheme = ColorScheme, content = content)
}