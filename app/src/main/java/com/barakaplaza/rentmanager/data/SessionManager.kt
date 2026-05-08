package com.barakaplaza.rentmanager.data

import android.content.Context

object SessionManager {
    private const val PREF = "baraka_session"

    fun setBuilding(context: Context, name: String) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putString("building", name).apply()

    fun getBuilding(context: Context): String =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("building", "Baraka Plaza") ?: "Baraka Plaza"

    fun setLandlordLoggedIn(context: Context, value: Boolean) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean("landlord_in", value).apply()

    fun isLandlordLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).getBoolean("landlord_in", false)

    fun logout(context: Context) =
        context.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().putBoolean("landlord_in", false).apply()
}
