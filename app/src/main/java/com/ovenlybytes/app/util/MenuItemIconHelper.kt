package com.ovenlybytes.app.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.ovenlybytes.app.model.MenuCategory

data class MenuItemIconConfig(
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconTint: Color
)

object MenuItemIconHelper {

    fun getIconConfig(categoryName: String, itemName: String = ""): MenuItemIconConfig {
        val lowerItem = itemName.lowercase()
        val cat = try { MenuCategory.valueOf(categoryName) } catch (e: Exception) { null }

        return when {
            // Specific Item / Category match: Brownies
            cat == MenuCategory.BROWNIES || lowerItem.contains("brownie") -> {
                MenuItemIconConfig(
                    icon = Icons.Default.BakeryDining,
                    backgroundColor = Color(0xFFEFEBE9),
                    iconTint = Color(0xFF5D4037)
                )
            }
            // Tea Cakes
            cat == MenuCategory.TEA_CAKES || lowerItem.contains("tea cake") || lowerItem.contains("loaf") -> {
                MenuItemIconConfig(
                    icon = Icons.Default.Coffee,
                    backgroundColor = Color(0xFFFFF3E0),
                    iconTint = Color(0xFFE65100)
                )
            }
            // Cookies & Biscuits
            cat == MenuCategory.COOKIES || lowerItem.contains("cookie") || lowerItem.contains("biscuit") -> {
                MenuItemIconConfig(
                    icon = Icons.Default.Cookie,
                    backgroundColor = Color(0xFFFBE9E7),
                    iconTint = Color(0xFFD84315)
                )
            }
            // Premium Collection & Luxury Fusion
            cat == MenuCategory.PREMIUM_COLLECTION || lowerItem.contains("truffle") || lowerItem.contains("biscoff") || lowerItem.contains("nutella") || lowerItem.contains("rasmalai") || lowerItem.contains("tiramisu") -> {
                MenuItemIconConfig(
                    icon = Icons.Default.AutoAwesome,
                    backgroundColor = Color(0xFFFFF8E1),
                    iconTint = Color(0xFFF57F17)
                )
            }
            // Classic Cakes
            cat == MenuCategory.CLASSIC_CAKES || lowerItem.contains("cake") -> {
                MenuItemIconConfig(
                    icon = Icons.Default.Cake,
                    backgroundColor = Color(0xFFFCE4EC),
                    iconTint = Color(0xFFC2185B)
                )
            }
            // Custom / Other
            else -> {
                MenuItemIconConfig(
                    icon = Icons.Default.CardGiftcard,
                    backgroundColor = Color(0xFFF3E5F5),
                    iconTint = Color(0xFF7B1FA2)
                )
            }
        }
    }
}
