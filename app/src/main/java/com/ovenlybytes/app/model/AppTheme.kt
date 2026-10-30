package com.ovenlybytes.app.model

import androidx.compose.ui.graphics.Color

enum class AppTheme(
    val displayName: String,
    val description: String,
    val primaryColor: Color,
    val backgroundColor: Color
) {
    ARTISAN_COCOA(
        displayName = "Artisan Cocoa",
        description = "Classic warm bakery cocoa & caramel",
        primaryColor = Color(0xFF4A2518),
        backgroundColor = Color(0xFFFFFBF5)
    ),
    STRAWBERRY_SHORTCAKE(
        displayName = "Strawberry Shortcake",
        description = "Delicate rose pink & berry cream",
        primaryColor = Color(0xFFC2185B),
        backgroundColor = Color(0xFFFFF0F5)
    ),
    HONEY_VANILLA(
        displayName = "Golden Vanilla",
        description = "Honey, amber & golden baked pastry",
        primaryColor = Color(0xFFB45309),
        backgroundColor = Color(0xFFFFFDF5)
    ),
    PISTACHIO_MATCHA(
        displayName = "Pistachio Matcha",
        description = "Fresh green tea & pistachio mint",
        primaryColor = Color(0xFF2D5A27),
        backgroundColor = Color(0xFFF4F9F4)
    ),
    LEMON_CHIFFON(
        displayName = "Lemon Chiffon",
        description = "Sunlit citrus yellow & fresh vanilla",
        primaryColor = Color(0xFFA16207),
        backgroundColor = Color(0xFFFFFCF0)
    ),
    BLUEBERRY_MACARON(
        displayName = "Blueberry Macaron",
        description = "Soft lavender blueberry & sweet cream",
        primaryColor = Color(0xFF5B21B6),
        backgroundColor = Color(0xFFF5F3FF)
    ),
    PEACH_COBBLER(
        displayName = "Peach Cobbler",
        description = "Warm apricot peach & cinnamon bake",
        primaryColor = Color(0xFFC2410C),
        backgroundColor = Color(0xFFFFF7ED)
    ),
    DARK_MOCHA(
        displayName = "Dark Mocha",
        description = "Dark chocolate & espresso dark mode",
        primaryColor = Color(0xFF3E2723),
        backgroundColor = Color(0xFF121212)
    )
}
