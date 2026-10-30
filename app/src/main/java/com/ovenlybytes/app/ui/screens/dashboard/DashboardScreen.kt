package com.ovenlybytes.app.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovenlybytes.app.model.OrderStatus
import com.ovenlybytes.app.model.OrderWithCustomerAndItems
import com.ovenlybytes.app.ui.components.*
import com.ovenlybytes.app.ui.theme.*
import com.ovenlybytes.app.util.Formatters
import com.ovenlybytes.app.util.WhatsAppHelper

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToCreateOrder: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToInventory: () -> Unit
) {
    val context = LocalContext.current
    val stats by viewModel.stats.collectAsState()
    val allOrders by viewModel.recentOrders.collectAsState()

    // Filter pending/active orders
    val activeOrders = remember(allOrders) {
        allOrders.filter {
            it.order.orderStatus != OrderStatus.DELIVERED.name &&
            it.order.orderStatus != OrderStatus.CANCELLED.name
        }.take(5)
    }

    Scaffold(
        topBar = {
            OvenlyTopAppBar()
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateOrder,
                containerColor = PrimaryCocoa,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) },
                text = { Text("Take New Order", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmCream)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Brand Banner / Tagline Notice
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = PrimaryCocoa.copy(alpha = 0.06f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, PrimaryCocoa.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(PrimaryCocoa),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Order Tracking & Bakery POS",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCocoa
                                )
                            )
                            Text(
                                text = "Track WhatsApp & Phone calls • 100% Eggless • 2 Days Notice",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }

            // High Priority Low Stock Alert Banner (if any)
            if (stats.lowStockCount > 0) {
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = LowStockBackground,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToInventory() }
                            .border(1.dp, LowStockAlertColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = LowStockAlertColor,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Low Stock Alert! (${stats.lowStockCount} items)",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = LowStockAlertColor,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Tap to review ingredients & packaging before baking",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                tint = LowStockAlertColor
                            )
                        }
                    }
                }
            }

            // Quick Stats Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Active Orders",
                            value = "${stats.pendingOrdersCount + stats.bakingOrdersCount + stats.readyOrdersCount}",
                            subtitle = "${stats.bakingOrdersCount} baking, ${stats.readyOrdersCount} ready",
                            icon = Icons.Default.Kitchen,
                            iconTint = StatusBaking,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToOrders
                        )
                        StatCard(
                            title = "Due Today",
                            value = "${stats.dueTodayCount}",
                            subtitle = "Deliveries for today",
                            icon = Icons.Default.Today,
                            iconTint = StatusReceived,
                            modifier = Modifier.weight(1f),
                            onClick = onNavigateToOrders
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(
                            title = "Total Revenue",
                            value = Formatters.formatCurrency(stats.totalRevenue),
                            subtitle = "Pending: ${Formatters.formatCurrency(stats.totalBalancePending)}",
                            icon = Icons.Default.CurrencyRupee,
                            iconTint = EgglessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "WhatsApp / Phone",
                            value = "${stats.whatsappOrdersCount} / ${stats.phoneOrdersCount}",
                            subtitle = "Orders by channel",
                            icon = Icons.Default.Chat,
                            iconTint = WhatsAppGreenDark,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Active / Upcoming Orders Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Orders in Progress (${activeOrders.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    TextButton(onClick = onNavigateToOrders) {
                        Text("View All", color = SecondaryRose, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (activeOrders.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "All orders are fulfilled!",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            Text(
                                text = "Ready to receive new customer orders via Phone or WhatsApp.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            } else {
                items(activeOrders, key = { it.order.id }) { orderDetails ->
                    ActiveOrderCard(
                        orderDetails = orderDetails,
                        onClick = { onNavigateToOrderDetail(orderDetails.order.id) },
                        onWhatsAppClick = {
                            val msg = WhatsAppHelper.formatOrderReceiptMessage(orderDetails)
                            val phone = orderDetails.customer?.phone ?: ""
                            WhatsAppHelper.openWhatsApp(context, phone, msg)
                        },
                        onStatusChange = { newStatus ->
                            viewModel.updateOrderStatus(orderDetails.order.id, newStatus)
                        }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(72.dp)) }
        }
    }
}

@Composable
fun ActiveOrderCard(
    orderDetails: OrderWithCustomerAndItems,
    onClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onStatusChange: (OrderStatus) -> Unit
) {
    val order = orderDetails.order
    val customer = orderDetails.customer
    val items = orderDetails.items

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Order ID, Channel Badge & Status Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "#OB-${order.id}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCocoa
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OrderChannelBadge(sourceName = order.orderSource)
                }
                OrderStatusChip(statusName = order.orderStatus)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Customer Name & Phone
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = customer?.name ?: "Unknown Customer",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
                )
                if (customer?.phone?.isNotBlank() == true) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${customer.phone}",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
            }

            // Items summary
            Spacer(modifier = Modifier.height(6.dp))
            items.forEach { item ->
                Text(
                    text = "• ${item.quantity}x ${item.itemName} (${item.variantName})",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }

            // Cake Message (if any)
            if (order.cakeMessage.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PrimaryCocoa.copy(alpha = 0.05f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Message: \"${order.cakeMessage}\"",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = PrimaryCocoa
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = WarmBorder
            )

            // Delivery Date & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Delivery: ${Formatters.formatDate(order.deliveryDate)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "${Formatters.getDeliveryBadgeText(order.deliveryDate)} • ${order.deliveryTimeSlot}",
                        style = MaterialTheme.typography.labelSmall.copy(color = SecondaryRose)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // WhatsApp Quick Share
                    IconButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(WhatsAppBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Send WhatsApp Receipt",
                            tint = WhatsAppGreenDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Progress Status Button
                    when (order.orderStatus) {
                        OrderStatus.RECEIVED.name -> {
                            Button(
                                onClick = { onStatusChange(OrderStatus.BAKING) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusBaking),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Bake", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.BAKING.name -> {
                            Button(
                                onClick = { onStatusChange(OrderStatus.READY) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusReady),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Ready", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        OrderStatus.READY.name -> {
                            Button(
                                onClick = { onStatusChange(OrderStatus.DELIVERED) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Deliver", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

