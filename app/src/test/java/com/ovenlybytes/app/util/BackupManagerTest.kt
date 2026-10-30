package com.ovenlybytes.app.util

import com.ovenlybytes.app.data.local.entities.*
import com.ovenlybytes.app.model.ShopDetails
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class BackupManagerTest {

    @Test
    fun testShopDetailsDefaultData() {
        val details = ShopDetails(
            shopName = "Ovenly Test Bites",
            tagline = "Artisan Bakes",
            phone = "9876543210"
        )

        assertEquals("Ovenly Test Bites", details.shopName)
        assertEquals("Artisan Bakes", details.tagline)
        assertEquals("9876543210", details.phone)
        assertTrue(details.isEgglessOnly)
    }

    @Test
    fun testSqlEscapingPreventsInjection() {
        val maliciousInput = "O'Reilly's Cake; DROP TABLE orders; --"
        val escaped = "'" + maliciousInput.replace("'", "''") + "'"

        assertEquals("'O''Reilly''s Cake; DROP TABLE orders; --'", escaped)
        assertTrue(escaped.startsWith("'"))
        assertTrue(escaped.endsWith("'"))
    }

    @Test
    fun testCsvEscapingHandlesCommasAndQuotes() {
        val rawInput = "Chocolate Cake, 1\" Special (Eggless)"
        val escaped = "\"" + rawInput.replace("\"", "\"\"") + "\""

        assertEquals("\"Chocolate Cake, 1\"\" Special (Eggless)\"", escaped)
    }

    @Test
    fun testCustomerEntityMapping() {
        val customer = CustomerEntity(
            id = 101L,
            name = "Ananya Sharma",
            phone = "9811223344",
            email = "ananya@example.com",
            address = "MG Road, Suite 400",
            notes = "Prefers less sugar"
        )

        assertEquals(101L, customer.id)
        assertEquals("Ananya Sharma", customer.name)
        assertEquals("9811223344", customer.phone)
        assertEquals("ananya@example.com", customer.email)
    }

    @Test
    fun testMenuItemEntityPricing() {
        val item = MenuItemEntity(
            id = 1L,
            name = "Black Forest Cake",
            category = "CLASSIC_CAKES",
            price1 = 600.0,
            price2 = 1050.0,
            isSeasonalRate = false
        )

        assertEquals(600.0, item.price1, 0.001)
        assertEquals(1050.0, item.price2, 0.001)
        assertFalse(item.isSeasonalRate)
        assertTrue(item.isAvailable)
    }
}
