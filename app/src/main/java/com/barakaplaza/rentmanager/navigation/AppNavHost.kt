package com.barakaplaza.rentmanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.barakaplaza.rentmanager.ui.theme.screens.building.BuildingSelectScreen
import com.barakaplaza.rentmanager.ui.theme.screens.dashboard.DashboardScreen
import com.barakaplaza.rentmanager.ui.theme.screens.houses.HousesScreen
import com.barakaplaza.rentmanager.ui.theme.screens.login.LoginScreen
import com.barakaplaza.rentmanager.ui.theme.screens.payments.PaymentHistoryScreen
import com.barakaplaza.rentmanager.ui.theme.screens.payments.PaymentScreen
import com.barakaplaza.rentmanager.ui.theme.screens.portal.SuggestionsScreen
import com.barakaplaza.rentmanager.ui.theme.screens.portal.TenantPortalScreen
import com.barakaplaza.rentmanager.ui.theme.screens.splash.SplashScreen
import com.barakaplaza.rentmanager.ui.theme.screens.tenants.AddTenantScreen
import com.barakaplaza.rentmanager.ui.theme.screens.tenants.TenantListScreen
import com.barakaplaza.rentmanager.ui.theme.screens.tenants.TenantRegisterScreen
import com.barakaplaza.rentmanager.ui.theme.screens.tenants.UpdateTenantScreen

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = ROUTE_SPLASH) {
        composable(ROUTE_SPLASH)           { SplashScreen(navController) }
        composable(ROUTE_BUILDING_SELECT)  { BuildingSelectScreen(navController) }
        composable(ROUTE_LOGIN)            { LoginScreen(navController) }
        composable(ROUTE_DASHBOARD)        { DashboardScreen(navController) }
        composable(ROUTE_ADD_TENANT)       { AddTenantScreen(navController) }
        composable(ROUTE_VIEW_TENANTS)     { TenantListScreen(navController) }
        composable(ROUTE_HOUSES)           { HousesScreen(navController) }
        composable(ROUTE_PAYMENTS)         { PaymentScreen(navController) }
        composable(ROUTE_PAYMENT_HISTORY)  { PaymentHistoryScreen(navController) }
        composable(ROUTE_SUGGESTIONS)      { SuggestionsScreen(navController) }
        composable(ROUTE_TENANT_REGISTER)  { TenantRegisterScreen(navController) }

        composable("$ROUTE_UPDATE_TENANT/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { UpdateTenantScreen(navController, it.arguments?.getInt("id") ?: 0) }

        composable("$ROUTE_TENANT_PORTAL/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { TenantPortalScreen(navController, it.arguments?.getInt("id") ?: 0) }
    }
}
