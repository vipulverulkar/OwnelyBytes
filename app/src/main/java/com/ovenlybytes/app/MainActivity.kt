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
import com.ovenlybytes.app.ui.theme.OvenlyBytesTheme
import com.ovenlybytes.app.ui.theme.PrimaryCocoa
import com.ovenlybytes.app.ui.theme.WarmBorder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as OvenlyBytesApp

        setContent {
            OvenlyBytesTheme {
                OvenlyAppContent(app = app)
            }
        }
    }
}

@Composable
fun OvenlyAppContent(app: OvenlyBytesApp) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // ViewModel instances backed by Room repository
    val dashboardViewModel: DashboardViewModel = viewModel(
        factory = DashboardViewModel.Factory(app.repository)
    )
    val ordersViewModel: OrdersViewModel = viewModel(
        factory = OrdersViewModel.Factory(app.repository)
    )
    val customerViewModel: CustomerViewModel = viewModel(
        factory = CustomerViewModel.Factory(app.repository)
    )
    val inventoryViewModel: InventoryViewModel = viewModel(
        factory = InventoryViewModel.Factory(app.repository)
    )
    val menuViewModel: MenuViewModel = viewModel(
        factory = MenuViewModel.Factory(app.repository)
    )

    // Show bottom bar only on primary destination screens
    val shouldShowBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Orders.route,
        Screen.Menu.route,
        Screen.Inventory.route,
        Screen.Customers.route
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (shouldShowBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
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
                                selectedIconColor = PrimaryCocoa,
                                selectedTextColor = PrimaryCocoa,
                                indicatorColor = PrimaryCocoa.copy(alpha = 0.12f),
                                unselectedIconColor = Color(0xFF7A6861),
                                unselectedTextColor = Color(0xFF7A6861)
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
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToCreateOrder = { navController.navigate(Screen.CreateOrder.route) },
                    onNavigateToOrderDetail = { orderId ->
                        navController.navigate(Screen.OrderDetail.createRoute(orderId))
                    },
                    onNavigateToOrders = { navController.navigate(Screen.Orders.route) },
                    onNavigateToInventory = { navController.navigate(Screen.Inventory.route) }
                )
            }

            // Orders List
            composable(Screen.Orders.route) {
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
                OrderDetailScreen(
                    orderId = orderId,
                    viewModel = ordersViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Menu Catalog
            composable(Screen.Menu.route) {
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
                InventoryScreen(
                    viewModel = inventoryViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Customers
            composable(Screen.Customers.route) {
                CustomerListScreen(
                    viewModel = customerViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

