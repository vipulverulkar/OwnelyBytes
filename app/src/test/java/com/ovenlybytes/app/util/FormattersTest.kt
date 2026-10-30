package com.ovenlybytes.app.util

import org.junit.Assert.*
import org.junit.Test

class FormattersTest {

    @Test
    fun testCurrencyFormatting() {
        val amount = 1500.0
        val formatted = Formatters.formatCurrency(amount)
        assertTrue(formatted.contains("1,500"))
        assertTrue(formatted.startsWith("₹"))
    }

    @Test
    fun testDeliveryBadgeTextForToday() {
        val now = System.currentTimeMillis()
        val badgeText = Formatters.getDeliveryBadgeText(now)
        assertEquals("Due Today", badgeText)
    }

    @Test
    fun testDeliveryBadgeTextForTomorrow() {
        val tomorrow = System.currentTimeMillis() + (24 * 60 * 60 * 1000L)
        val badgeText = Formatters.getDeliveryBadgeText(tomorrow)
        assertEquals("Due Tomorrow", badgeText)
    }
}
