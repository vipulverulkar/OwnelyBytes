package com.ovenlybytes.app.model

import com.ovenlybytes.app.util.AppConstants

enum class OrderSource(val displayName: String, val iconName: String) {
    WHATSAPP("WhatsApp", "chat"),
    PHONE("Phone Call", "call"),
    WALK_IN("In-Person", "storefront")
}

enum class OrderStatus(val displayName: String, val stepIndex: Int) {
    RECEIVED("Received", 0),
    BAKING("Preparing / Baking", 1),
    READY("Ready to Deliver", 2),
    DELIVERED("Delivered", 3),
    CANCELLED("Cancelled", -1)
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    PARTIAL("Partial / Advance"),
    COMPLETED("Paid in Full")
}

enum class PaymentMode(val displayName: String) {
    UPI("UPI"),
    CASH("Cash"),
    CARD("Card"),
    NET_BANKING("Net Banking")
}



enum class MenuCategory(val displayName: String, val defaultSizes: Pair<String, String>) {
    CLASSIC_CAKES("Classic Cakes", Pair(AppConstants.PORTION_HALF_KG, AppConstants.PORTION_1KG)),
    PREMIUM_COLLECTION("Premium Collection", Pair(AppConstants.PORTION_HALF_KG, AppConstants.PORTION_1KG)),
    BROWNIES("Brownies", Pair(AppConstants.PORTION_250G, AppConstants.PORTION_500G)),
    TEA_CAKES("Tea Cakes", Pair(AppConstants.PORTION_250G, AppConstants.PORTION_500G)),
    COOKIES("Cookies", Pair(AppConstants.PORTION_250G, AppConstants.PORTION_500G)),
    CUSTOM("Custom / Seasonal", Pair("Standard", "Large"))
}

enum class InventoryCategory(val displayName: String) {
    INGREDIENTS("Ingredients"),
    DAIRY("Dairy & Creams"),
    TOPPINGS("Flavours & Spreads"),
    PACKAGING("Boxes & Ribbons")
}

