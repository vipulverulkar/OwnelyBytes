package com.ovenlybytes.app.util

import com.ovenlybytes.app.model.AppTheme
import com.ovenlybytes.app.model.ShopDetails
import org.junit.Assert.*
import org.junit.Test

class ShopPreferencesTest {

    @Test
    fun testAppThemeEnumProperties() {
        assertEquals("Artisan Cocoa", AppTheme.ARTISAN_COCOA.displayName)
        assertEquals("Strawberry Shortcake", AppTheme.STRAWBERRY_SHORTCAKE.displayName)
        assertEquals("Royal Red Velvet", AppTheme.ROYAL_RED_VELVET.displayName)
        assertEquals("Golden Vanilla", AppTheme.GOLDEN_VANILLA.displayName)
        assertEquals("Salted Caramel", AppTheme.SALTED_CARAMEL.displayName)
        assertEquals("Pistachio Matcha", AppTheme.PISTACHIO_MATCHA.displayName)
        assertEquals("Emerald Mint", AppTheme.EMERALD_MINT.displayName)
        assertEquals("Lemon Chiffon", AppTheme.LEMON_CHIFFON.displayName)
        assertEquals("Lavender Earl Grey", AppTheme.LAVENDER_EARL_GREY.displayName)
        assertEquals("Blueberry Macaron", AppTheme.BLUEBERRY_MACARON.displayName)
        assertEquals("Peach Cobbler", AppTheme.PEACH_COBBLER.displayName)
        assertEquals("Midnight Truffle", AppTheme.MIDNIGHT_TRUFFLE.displayName)

        assertEquals(12, AppTheme.entries.size)
    }

    @Test
    fun testShopDetailsDefaults() {
        val defaultShop = ShopDetails()

        assertEquals("Ovenly Bites", defaultShop.shopName)
        assertEquals("Freshly Baked With Love ❤️", defaultShop.tagline)
        assertTrue(defaultShop.isEgglessOnly)
    }
}
