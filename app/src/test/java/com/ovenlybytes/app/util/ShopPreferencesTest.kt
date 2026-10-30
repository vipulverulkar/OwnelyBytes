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
        assertEquals("Golden Vanilla", AppTheme.HONEY_VANILLA.displayName)
        assertEquals("Pistachio Matcha", AppTheme.PISTACHIO_MATCHA.displayName)
        assertEquals("Lemon Chiffon", AppTheme.LEMON_CHIFFON.displayName)
        assertEquals("Blueberry Macaron", AppTheme.BLUEBERRY_MACARON.displayName)
        assertEquals("Peach Cobbler", AppTheme.PEACH_COBBLER.displayName)
        assertEquals("Dark Mocha", AppTheme.DARK_MOCHA.displayName)

        assertEquals(8, AppTheme.entries.size)
    }

    @Test
    fun testShopDetailsDefaults() {
        val defaultShop = ShopDetails()

        assertEquals("Ovenly Bites", defaultShop.shopName)
        assertEquals("Freshly Baked With Love ❤️", defaultShop.tagline)
        assertTrue(defaultShop.isEgglessOnly)
    }
}
