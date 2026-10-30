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
    onNavigateToInventory: () -> Unit,
    onNavigateToSettings: () -> Unit
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
            OvenlyTopAppBar(
                onSettingsClick = onNavigateToSettings
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToCreateOrder,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.AddShoppingCart, contentDescription = null) },
                text = { Text("Take New Order", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

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

            // Quick KPI Overview Bar
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Active Orders
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToOrders() }
                        ) {
                            Text("Active Orders", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${stats.pendingOrdersCount + stats.bakingOrdersCount + stats.readyOrdersCount}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Text("${stats.bakingOrdersCount} baking", fontSize = 10.sp, color = TextMuted)
                        }

                        VerticalDivider(modifier = Modifier.height(36.dp), color = WarmBorder)

                        // Due Today
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onNavigateToOrders() }
                        ) {
                            Text("Due Today", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = "${stats.dueTodayCount}",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = StatusReceived)
                            )
                            Text("Deliveries", fontSize = 10.sp, color = TextMuted)
                        }

                        VerticalDivider(modifier = Modifier.height(36.dp), color = WarmBorder)

                        // Total Revenue
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                            Text("Total Revenue", fontSize = 11.sp, color = TextSecondary)
                            Text(
                                text = Formatters.formatCurrency(stats.totalRevenue),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StatusDelivered)
                            )
                            Text("Pending: ${Formatters.formatCurrency(stats.totalBalancePending)}", fontSize = 10.sp, color = TextMuted)
                        }
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

