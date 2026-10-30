package com.ovenlybytes.app.model

data class ShopDetails(
    val shopName: String = "Ovenly Bites",
    val tagline: String = "Freshly Baked With Love ❤️",
    val ownerName: String = "Ananya Gupta",
    val phone: String = "9876543210",
    val email: String = "contact@ovenlybites.com",
    val address: String = "Shop #4, Baker Street, Connaught Place, New Delhi",
    val fssaiNumber: String = "10021011000123",
    val gstNumber: String = "07AAAAA0000A1Z5",
    val isEgglessOnly: Boolean = true
)
