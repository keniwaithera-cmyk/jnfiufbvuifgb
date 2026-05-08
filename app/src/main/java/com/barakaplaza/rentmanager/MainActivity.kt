package com.barakaplaza.rentmanager

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.compose.setContent
import androidx.fragment.app.FragmentActivity
import com.barakaplaza.rentmanager.navigation.AppNavHost
import com.barakaplaza.rentmanager.ui.theme.BarakaPlazaTheme

// FIXED: Changed ComponentActivity → FragmentActivity so BiometricPrompt works correctly.
// BiometricPrompt requires a FragmentActivity host; using ComponentActivity caused a crash
// when the fingerprint login button was tapped.
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        requestPermissions()
        setContent {
            BarakaPlazaTheme { AppNavHost() }
        }
    }

    private fun requestPermissions() {
        val perms = mutableListOf(Manifest.permission.SEND_SMS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            perms.add(Manifest.permission.READ_MEDIA_IMAGES)
        else
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        val needed = perms.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (needed.isNotEmpty())
            ActivityCompat.requestPermissions(this, needed.toTypedArray(), 101)
    }
}
