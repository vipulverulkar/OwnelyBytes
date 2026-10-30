package com.ovenlybytes.app.model

enum class OrderSource(val displayName: String, val iconName: String) {
    WHATSAPP("WhatsApp", "chat"),
    PHONE("Phone Call", "call"),
    WALK_IN("In-Person", "storefront")
}

enum class OrderStatus(val displayName: String, val stepIndex: Int) {
    RECEIVED("Received", 0),
    BAKING("Baking & Preparing", 1),
    READY("Ready for Pickup", 2),
    OUT_FOR_DELIVERY("Out for Delivery", 3),
    DELIVERED("Delivered", 4),
    CANCELLED("Cancelled", -1)
}

enum class PaymentStatus(val displayName: String) {
    PENDING("Pending"),
    PARTIAL("Partial / Advance"),
    COMPLETED("Paid in Full")
}

enum class PaymentMode(val displayName: String) {
    UPI("UPI / GPay / PhonePe"),
    CASH("Cash"),
    CARD("Card"),
    NET_BANKING("Net Banking")
}

enum class MenuCategory(val displayName: String, val defaultSizes: Pair<String, String>) {
    CLASSIC_CAKES("Classic Cakes", Pair("½ KG", "1 KG")),
    PREMIUM_COLLECTION("Premium Collection", Pair("½ KG", "1 KG")),
    BROWNIES("Brownies", Pair("250 g", "500 g")),
    TEA_CAKES("Tea Cakes", Pair("250 g", "500 g")),
    COOKIES("Cookies", Pair("250 g", "500 g")),
    CUSTOM("Custom / Seasonal", Pair("Standard", "Large"))
}

enum class InventoryCategory(val displayName: String) {
    INGREDIENTS("Ingredients"),
    DAIRY("Dairy & Creams"),
    TOPPINGS("Flavours & Spreads"),
    PACKAGING("Boxes & Ribbons")
}

