package com.ovenlybytes.app.util

import org.junit.Assert.*
import org.junit.Test

class MenuItemIconHelperTest {

    @Test
    fun testBrownieIconConfig() {
        val config = MenuItemIconHelper.getIconConfig("BROWNIES", "Classic Fudgy Brownie")
        assertNotNull(config.icon)
        assertNotNull(config.backgroundColor)
        assertNotNull(config.iconTint)
    }

    @Test
    fun testCakeIconConfig() {
        val config = MenuItemIconHelper.getIconConfig("CLASSIC_CAKES", "Pineapple Cake")
        assertNotNull(config.icon)
    }

    @Test
    fun testTeaCakeIconConfig() {
        val config = MenuItemIconHelper.getIconConfig("TEA_CAKES", "Honey Oats Tea Cake")
        assertNotNull(config.icon)
    }

    @Test
    fun testCookieIconConfig() {
        val config = MenuItemIconHelper.getIconConfig("COOKIES", "Oats & Almond Cookies")
        assertNotNull(config.icon)
    }

    @Test
    fun testPremiumIconConfig() {
        val config = MenuItemIconHelper.getIconConfig("PREMIUM_COLLECTION", "Rasmalai Cake")
        assertNotNull(config.icon)
    }
}
