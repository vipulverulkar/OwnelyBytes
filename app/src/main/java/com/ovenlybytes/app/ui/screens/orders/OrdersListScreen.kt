package com.ovenlybytes.app.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import com.ovenlybytes.app.model.OrderSource
import com.ovenlybytes.app.model.OrderStatus
import com.ovenlybytes.app.model.OrderWithCustomerAndItems
import com.ovenlybytes.app.ui.components.OrderChannelBadge
import com.ovenlybytes.app.ui.components.OrderStatusChip
import com.ovenlybytes.app.ui.theme.*
import com.ovenlybytes.app.util.Formatters
import com.ovenlybytes.app.util.PhoneHelper
import com.ovenlybytes.app.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersListScreen(
    viewModel: OrdersViewModel,
    onNavigateToCreateOrder: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val orders by viewModel.filteredOrders.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedChannel by viewModel.selectedChannelFilter.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryCocoa,
                    titleContentColor = Color.White,
                    actionIconContentColor = Color.White
                ),
                title = {
                    if (isSearchExpanded) {
                        TextField(
                            value = searchQuery,
                            onValueChange = viewModel::onSearchQueryChanged,
                            placeholder = { Text("Search by customer, phone, item...", color = Color.White.copy(0.7f)) },
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "Orders (${orders.size})",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isSearchExpanded = !isSearchExpanded
                        if (!isSearchExpanded) viewModel.onSearchQueryChanged("")
                    }) {
                        Icon(
                            imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreateOrder,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Order")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Channel Filter Chips (All, WhatsApp, Phone, Walk-in)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedChannel == "ALL",
                    onClick = { viewModel.onChannelFilterChanged("ALL") },
                    label = { Text("All Channels") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryCocoa,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedChannel == OrderSource.WHATSAPP.name,
                    onClick = { viewModel.onChannelFilterChanged(OrderSource.WHATSAPP.name) },
                    label = { Text("WhatsApp 🟢") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WhatsAppGreenDark,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedChannel == OrderSource.PHONE.name,
                    onClick = { viewModel.onChannelFilterChanged(OrderSource.PHONE.name) },
                    label = { Text("Phone Calls 🔵") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PhoneBlue,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedChannel == OrderSource.WALK_IN.name,
                    onClick = { viewModel.onChannelFilterChanged(OrderSource.WALK_IN.name) },
                    label = { Text("Walk-in") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WalkInPurple,
                        selectedLabelColor = Color.White
                    )
                )
            }

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "All Status",
                    OrderStatus.RECEIVED.name to "Received",
                    OrderStatus.BAKING.name to "Baking",
                    OrderStatus.READY.name to "Ready",
                    OrderStatus.DELIVERED.name to "Delivered"
                ).forEach { (statusKey, label) ->
                    AssistChip(
                        onClick = { viewModel.onStatusFilterChanged(statusKey) },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selectedStatus == statusKey) PrimaryCocoa.copy(alpha = 0.12f) else WarmCream,
                            labelColor = if (selectedStatus == statusKey) PrimaryCocoa else TextSecondary
                        ),
                        border = if (selectedStatus == statusKey) {
                            androidx.compose.foundation.BorderStroke(1.dp, PrimaryCocoa)
                        } else null
                    )
                }
            }

            Divider(color = WarmBorder)

            // Orders List
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No orders match your filter",
                            style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Take a new order from WhatsApp or Phone call anytime",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMuted)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders, key = { it.order.id }) { orderDetails ->
                        OrderRowCard(
                            orderDetails = orderDetails,
                            onClick = { onNavigateToOrderDetail(orderDetails.order.id) },
                            onWhatsAppClick = {
                                val msg = WhatsAppHelper.formatOrderReceiptMessage(orderDetails)
                                val phone = orderDetails.customer?.phone ?: ""
                                WhatsAppHelper.openWhatsApp(context, phone, msg)
                            },
                            onCallClick = {
                                val phone = orderDetails.customer?.phone ?: ""
                                PhoneHelper.callPhone(context, phone)
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            }
        }
    }
}

@Composable
fun OrderRowCard(
    orderDetails: OrderWithCustomerAndItems,
    onClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onCallClick: () -> Unit
) {
    val order = orderDetails.order
    val customer = orderDetails.customer
    val items = orderDetails.items

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Order ID, Channel Badge & Status
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
            Text(
                text = customer?.name ?: "Customer",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            // Items summary
            val itemsSummary = items.joinToString(", ") { "${it.quantity}x ${it.itemName} (${it.variantName})" }
            Text(
                text = itemsSummary,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                maxLines = 2
            )

            if (order.cakeMessage.isNotBlank()) {
                Text(
                    text = "Cake message: \"${order.cakeMessage}\"",
                    style = MaterialTheme.typography.bodySmall.copy(color = PrimaryCocoa, fontWeight = FontWeight.Medium),
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = WarmBorder)
            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Row: Date/Slot, Financials, Contact Action Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${Formatters.formatDate(order.deliveryDate)} • ${order.deliveryTimeSlot}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total: ${Formatters.formatCurrency(order.totalAmount)}",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (order.balanceDue > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "(Bal: ${Formatters.formatCurrency(order.balanceDue)})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = BalanceDueColor,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Call Customer Shortcut
                    IconButton(
                        onClick = onCallClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(PhoneBlueBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Call Customer",
                            tint = PhoneBlue,
                            modifier = Modifier.size(17.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // WhatsApp Shortcut
                    IconButton(
                        onClick = onWhatsAppClick,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(WhatsAppBackground)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "WhatsApp Customer",
                            tint = WhatsAppGreenDark,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}

