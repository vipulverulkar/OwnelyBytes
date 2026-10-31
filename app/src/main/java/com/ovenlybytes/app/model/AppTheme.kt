package com.ovenlybytes.app.model

import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundColor: Color
) {
    ARTISAN_COCOA(
        displayName = "Artisan Cocoa",
        description = "Classic warm bakery cocoa & caramel",
        primaryColor = Color(0xFF4A2518),
        secondaryColor = Color(0xFFC77826),
        backgroundColor = Color(0xFFFFFBF5)
    ),
    STRAWBERRY_SHORTCAKE(
        displayName = "Strawberry Shortcake",
        description = "Delicate rose pink & berry cream",
        primaryColor = Color(0xFFC2185B),
        secondaryColor = Color(0xFFE91E63),
        backgroundColor = Color(0xFFFFF0F5)
    ),
    ROYAL_RED_VELVET(
        displayName = "Royal Red Velvet",
        description = "Deep red velvet cocoa & cream cheese",
        primaryColor = Color(0xFF800020),
        secondaryColor = Color(0xFFB71C1C),
        backgroundColor = Color(0xFFFFFAF6)
    ),
    GOLDEN_VANILLA(
        displayName = "Golden Vanilla",
        description = "Honey, amber & golden baked pastry",
        primaryColor = Color(0xFFB45309),
        secondaryColor = Color(0xFFD97706),
        backgroundColor = Color(0xFFFFFDF5)
    ),
    SALTED_CARAMEL(
        displayName = "Salted Caramel",
        description = "Rich butterscotch & burnt caramel cream",
        primaryColor = Color(0xFF9F5113),
        secondaryColor = Color(0xFFD97706),
        backgroundColor = Color(0xFFFFF8F0)
    ),
    PISTACHIO_MATCHA(
        displayName = "Pistachio Matcha",
        description = "Fresh green tea & pistachio mint",
        primaryColor = Color(0xFF2D5A27),
        secondaryColor = Color(0xFF487B32),
        backgroundColor = Color(0xFFF4F9F4)
    ),
    EMERALD_MINT(
        displayName = "Emerald Mint",
        description = "Dark chocolate & peppermint bark",
        primaryColor = Color(0xFF1B4332),
        secondaryColor = Color(0xFF2D6A4F),
        backgroundColor = Color(0xFFF0F7F4)
    ),
    LEMON_CHIFFON(
        displayName = "Lemon Chiffon",
        description = "Sunlit citrus yellow & fresh vanilla",
        primaryColor = Color(0xFFA16207),
        secondaryColor = Color(0xFFCA8A04),
        backgroundColor = Color(0xFFFFFCF0)
    ),
    LAVENDER_EARL_GREY(
        displayName = "Lavender Earl Grey",
        description = "Boutique Earl Grey tea & lavender bloom",
        primaryColor = Color(0xFF4A3B6B),
        secondaryColor = Color(0xFF6B5B95),
        backgroundColor = Color(0xFFF7F5FA)
    ),
    BLUEBERRY_MACARON(
        displayName = "Blueberry Macaron",
        description = "Soft lavender blueberry & sweet cream",
        primaryColor = Color(0xFF5B21B6),
        secondaryColor = Color(0xFF7C3AED),
        backgroundColor = Color(0xFFF5F3FF)
    ),
    PEACH_COBBLER(
        displayName = "Peach Cobbler",
        description = "Warm apricot peach & cinnamon bake",
        primaryColor = Color(0xFFC2410C),
        secondaryColor = Color(0xFFEA580C),
        backgroundColor = Color(0xFFFFF7ED)
    ),
    MIDNIGHT_TRUFFLE(
        displayName = "Midnight Truffle",
        description = "Dark truffle & gold amber (Dark Mode)",
        primaryColor = Color(0xFFF59E0B),
        secondaryColor = Color(0xFFD97706),
        backgroundColor = Color(0xFF0F0F12)
    )
}
