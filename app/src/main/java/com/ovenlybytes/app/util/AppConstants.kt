package com.ovenlybytes.app.util

object AppConstants {
    const val APP_NAME = "Ovenly Bytes"
    const val DEFAULT_SHOP_NAME = "Ovenly Bites"
    const val CURRENCY_SYMBOL = "₹"
    const val DEFAULT_COUNTRY_CODE = "91"

    // Portion sizes
    const val PORTION_250G = "250 g"
    const val PORTION_500G = "500 g"
    const val PORTION_HALF_KG = "½ KG"
    const val PORTION_1KG = "1 KG"
    const val PORTION_1_5KG = "1.5 KG"
    const val PORTION_2KG = "2 KG"
    const val PORTION_CUSTOM = "Custom"
    const val PORTION_CUSTOM_ADD = "+ Custom Size / Price"

    val DEFAULT_PORTION_PRESETS = listOf(
        PORTION_250G,
        PORTION_500G,
        PORTION_1KG,
        PORTION_1_5KG,
        PORTION_2KG,
        PORTION_CUSTOM
    )

    // Pagination
    const val DEFAULT_PAGE_SIZE = 5

    // Database & SharedPreferences
    const val DATABASE_NAME = "ovenly_bites_database.db"
    const val PREFS_NAME = "ovenly_shop_settings"

    // Default Time Slots
    val DEFAULT_TIME_SLOTS = listOf(
        "10:00 AM - 12:00 PM",
        "1:00 PM - 3:00 PM",
        "5:00 PM - 7:00 PM",
        "7:00 PM - 9:00 PM",
        "Anytime"
    )
}
