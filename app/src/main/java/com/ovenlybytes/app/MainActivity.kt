package com.ovenlybytes.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.ovenlybytes.app.ui.navigation.BottomNavItem
import com.ovenlybytes.app.ui.navigation.Screen
import com.ovenlybytes.app.ui.navigation.bottomNavItems
import com.ovenlybytes.app.ui.screens.customers.CustomerListScreen
import com.ovenlybytes.app.ui.screens.customers.CustomerViewModel
import com.ovenlybytes.app.ui.screens.dashboard.DashboardScreen
import com.ovenlybytes.app.ui.screens.dashboard.DashboardViewModel
import com.ovenlybytes.app.ui.screens.inventory.InventoryScreen
import com.ovenlybytes.app.ui.screens.inventory.InventoryViewModel
import com.ovenlybytes.app.ui.screens.menu.MenuCatalogScreen
import com.ovenlybytes.app.ui.screens.menu.MenuViewModel
import com.ovenlybytes.app.ui.screens.orders.CreateOrderScreen
import com.ovenlybytes.app.ui.screens.orders.OrderDetailScreen
import com.ovenlybytes.app.ui.screens.orders.OrdersListScreen
import com.ovenlybytes.app.ui.screens.orders.OrdersViewModel
import com.ovenlybytes.app.ui.screens.settings.SettingsScreen
import com.ovenlybytes.app.ui.screens.settings.SettingsViewModel
import com.ovenlybytes.app.ui.theme.OvenlyBytesTheme
import com.ovenlybytes.app.ui.theme.PrimaryCocoa
import com.ovenlybytes.app.ui.theme.WarmBorder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as OvenlyBytesApp

        setContent {
            OvenlyAppContent(app = app)
        }
    }
}

@Composable
fun OvenlyAppContent(app: OvenlyBytesApp) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Theme ViewModel
    val settingsViewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(app.repository, app.shopPreferences)
    )
    val appTheme by settingsViewModel.appTheme.collectAsState()

    OvenlyBytesTheme(appTheme = appTheme) {
        // Show bottom bar only on primary destination screens
        val shouldShowBottomBar = currentRoute in listOf(
            Screen.Dashboard.route,
            Screen.Orders.route,
            Screen.Reports.route,
            Screen.Menu.route,
            Screen.Settings.route
        )

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                if (shouldShowBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        bottomNavItems.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    if (currentRoute != item.route) {
                                        navController.navigate(item.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                        contentDescription = item.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = item.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Dashboard.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                // Dashboard
                composable(Screen.Dashboard.route) {
                    val dashboardViewModel: DashboardViewModel = viewModel(
                        factory = DashboardViewModel.Factory(app.repository)
                    )
                    DashboardScreen(
                        viewModel = dashboardViewModel,
                        onNavigateToCreateOrder = { navController.navigate(Screen.CreateOrder.route) },
                        onNavigateToOrderDetail = { orderId ->
                            navController.navigate(Screen.OrderDetail.createRoute(orderId))
                        },
                        onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                        onNavigateToInventory = { navController.navigate(Screen.Inventory.route) },
                        onNavigateToSettings = { navController.navigate(Screen.Settings.route) }
                    )
                }

                // Orders List
                composable(Screen.Orders.route) {
                    val ordersViewModel: OrdersViewModel = viewModel(
                        factory = OrdersViewModel.Factory(app.repository)
                    )
                    OrdersListScreen(
                        viewModel = ordersViewModel,
                        onNavigateToCreateOrder = { navController.navigate(Screen.CreateOrder.route) },
                        onNavigateToOrderDetail = { orderId ->
                            navController.navigate(Screen.OrderDetail.createRoute(orderId))
                        }
                    )
                }

                // Create Order
                composable(Screen.CreateOrder.route) {
                    val ordersViewModel: OrdersViewModel = viewModel(
                        factory = OrdersViewModel.Factory(app.repository)
                    )
                    CreateOrderScreen(
                        viewModel = ordersViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onOrderCreated = { newOrderId ->
                            navController.popBackStack()
                            navController.navigate(Screen.OrderDetail.createRoute(newOrderId))
                        }
                    )
                }

                // Order Detail
                composable(
                    route = Screen.OrderDetail.route,
                    arguments = listOf(navArgument("orderId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
                    val ordersViewModel: OrdersViewModel = viewModel(
                        factory = OrdersViewModel.Factory(app.repository)
                    )
                    OrderDetailScreen(
                        orderId = orderId,
                        viewModel = ordersViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Menu Catalog
                composable(Screen.Menu.route) {
                    val menuViewModel: MenuViewModel = viewModel(
                        factory = MenuViewModel.Factory(app.repository)
                    )
                    MenuCatalogScreen(
                        viewModel = menuViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onTakeOrderWithItem = {
                            navController.navigate(Screen.CreateOrder.route)
                        }
                    )
                }

                // Inventory
                composable(Screen.Inventory.route) {
                    val inventoryViewModel: InventoryViewModel = viewModel(
                        factory = InventoryViewModel.Factory(app.repository)
                    )
                    InventoryScreen(
                        viewModel = inventoryViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Customers
                composable(Screen.Customers.route) {
                    val customerViewModel: CustomerViewModel = viewModel(
                        factory = CustomerViewModel.Factory(app.repository)
                    )
                    CustomerListScreen(
                        viewModel = customerViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

                // Settings & Hub
                composable(Screen.Settings.route) {
                    SettingsScreen(
                        viewModel = settingsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToCustomers = { navController.navigate(Screen.Customers.route) },
                        onNavigateToInventory = { navController.navigate(Screen.Inventory.route) }
                    )
                }

                // Reports & Financial Analytics
                composable(Screen.Reports.route) {
                    val reportsViewModel: com.ovenlybytes.app.ui.screens.reports.ReportsViewModel = viewModel(
                        factory = com.ovenlybytes.app.ui.screens.reports.ReportsViewModel.Factory(app.repository)
                    )
                    com.ovenlybytes.app.ui.screens.reports.ReportsScreen(
                        viewModel = reportsViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onNavigateToOrderDetail = { orderId ->
                            navController.navigate(Screen.OrderDetail.createRoute(orderId))
                        }
                    )
                }
            }
        }
    }
}

