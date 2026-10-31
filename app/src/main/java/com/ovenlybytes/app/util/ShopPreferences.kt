package com.ovenlybytes.app.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ovenlybytes.app.model.ShopDetails

class ShopPreferences(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            AppConstants.PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.values().first(),
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (_: Exception) {
        context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getShopDetails(): ShopDetails {
        return ShopDetails(
            shopName = prefs.getString(KEY_SHOP_NAME, AppConstants.DEFAULT_SHOP_NAME) ?: AppConstants.DEFAULT_SHOP_NAME,
            tagline = prefs.getString(KEY_TAGLINE, "Freshly Baked With Love ❤️") ?: "Freshly Baked With Love ❤️",
            ownerName = prefs.getString(KEY_OWNER_NAME, "Ananya Gupta") ?: "Ananya Gupta",
            phone = prefs.getString(KEY_PHONE, "9876543210") ?: "9876543210",
            email = prefs.getString(KEY_EMAIL, "contact@ovenlybites.com") ?: "contact@ovenlybites.com",
            address = prefs.getString(KEY_ADDRESS, "Shop #4, Baker Street, Connaught Place, New Delhi") ?: "Shop #4, Baker Street, Connaught Place, New Delhi",
            fssaiNumber = prefs.getString(KEY_FSSAI, "10021011000123") ?: "10021011000123",
            gstNumber = prefs.getString(KEY_GST, "07AAAAA0000A1Z5") ?: "07AAAAA0000A1Z5",
            isEgglessOnly = prefs.getBoolean(KEY_EGGLESS, true)
        )
    }

    fun saveShopDetails(details: ShopDetails) {
        prefs.edit()
            .putString(KEY_SHOP_NAME, details.shopName)
            .putString(KEY_TAGLINE, details.tagline)
            .putString(KEY_OWNER_NAME, details.ownerName)
            .putString(KEY_PHONE, details.phone)
            .putString(KEY_EMAIL, details.email)
            .putString(KEY_ADDRESS, details.address)
            .putString(KEY_FSSAI, details.fssaiNumber)
            .putString(KEY_GST, details.gstNumber)
            .putBoolean(KEY_EGGLESS, details.isEgglessOnly)
            .apply()
    }

    fun getAppTheme(): com.ovenlybytes.app.model.AppTheme {
        val name = prefs.getString(KEY_APP_THEME, com.ovenlybytes.app.model.AppTheme.ARTISAN_COCOA.name)
        return try {
            com.ovenlybytes.app.model.AppTheme.valueOf(name ?: com.ovenlybytes.app.model.AppTheme.ARTISAN_COCOA.name)
        } catch (e: Exception) {
            com.ovenlybytes.app.model.AppTheme.ARTISAN_COCOA
        }
    }

    fun saveAppTheme(theme: com.ovenlybytes.app.model.AppTheme) {
        prefs.edit()
            .putString(KEY_APP_THEME, theme.name)
            .apply()
    }

    companion object {
        private const val KEY_SHOP_NAME = "shop_name"
        private const val KEY_TAGLINE = "tagline"
        private const val KEY_OWNER_NAME = "owner_name"
        private const val KEY_PHONE = "phone"
        private const val KEY_EMAIL = "email"
        private const val KEY_ADDRESS = "address"
        private const val KEY_FSSAI = "fssai_number"
        private const val KEY_GST = "gst_number"
        private const val KEY_EGGLESS = "is_eggless"
        private const val KEY_APP_THEME = "app_theme"
    }
}
