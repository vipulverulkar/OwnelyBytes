package com.ovenlybytes.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Orders : Screen("orders")
    object CreateOrder : Screen("create_order")
    object OrderDetail : Screen("order_detail/{orderId}") {
        fun createRoute(orderId: Long) = "order_detail/$orderId"
    }
    object Customers : Screen("customers")
    object Inventory : Screen("inventory")
    object Menu : Screen("menu")
    object Settings : Screen("settings")
    object Reports : Screen("reports")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Dashboard : BottomNavItem(
        route = Screen.Dashboard.route,
        title = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    )

    object Orders : BottomNavItem(
        route = Screen.Orders.route,
        title = "Orders",
        selectedIcon = Icons.Filled.ReceiptLong,
        unselectedIcon = Icons.Outlined.ReceiptLong
    )

    object Menu : BottomNavItem(
        route = Screen.Menu.route,
        title = "Menu",
        selectedIcon = Icons.Filled.Cake,
        unselectedIcon = Icons.Outlined.Cake
    )

    object Inventory : BottomNavItem(
        route = Screen.Inventory.route,
        title = "Stock",
        selectedIcon = Icons.Filled.Inventory2,
        unselectedIcon = Icons.Outlined.Inventory2
    )

    object Reports : BottomNavItem(
        route = Screen.Reports.route,
        title = "Reports",
        selectedIcon = Icons.Filled.Analytics,
        unselectedIcon = Icons.Outlined.Analytics
    )

    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Dashboard,
    BottomNavItem.Orders,
    BottomNavItem.Reports,
    BottomNavItem.Menu,
    BottomNavItem.Settings
)

