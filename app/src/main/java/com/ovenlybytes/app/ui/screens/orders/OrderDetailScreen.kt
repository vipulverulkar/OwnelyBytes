package com.ovenlybytes.app.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.ovenlybytes.app.ui.components.OrderChannelBadge
import com.ovenlybytes.app.ui.components.OrderStatusChip
import com.ovenlybytes.app.ui.components.OvenlyTopAppBar
import com.ovenlybytes.app.ui.theme.*
import com.ovenlybytes.app.util.Formatters
import com.ovenlybytes.app.util.PhoneHelper
import com.ovenlybytes.app.util.WhatsAppHelper

@Composable
fun OrderDetailScreen(
    orderId: Long,
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val orderDetails by viewModel.getOrderDetails(orderId).collectAsState(initial = null)

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    if (orderDetails == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryCocoa)
        }
        return
    }

    val order = orderDetails!!.order
    val customer = orderDetails!!.customer
    val items = orderDetails!!.items

    Scaffold(
        topBar = {
            OvenlyTopAppBar(
                title = "Order #OB-${order.id}",
                subtitle = "Channel: ${order.orderSource}",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = Color.White)
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmCream)
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // 1. ORDER STATUS PROGRESS TRACKER
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Order Status Tracking",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryCocoa)
                            )
                            OrderStatusChip(statusName = order.orderStatus)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Status Progression Stepper
                        val currentStatus = OrderStatus.values().find { it.name == order.orderStatus } ?: OrderStatus.RECEIVED
                        val steps = listOf(OrderStatus.RECEIVED, OrderStatus.BAKING, OrderStatus.READY, OrderStatus.DELIVERED)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            steps.forEachIndexed { index, step ->
                                val isDone = currentStatus.stepIndex >= step.stepIndex
                                val isCurrent = currentStatus == step

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isCurrent -> StatusBaking
                                                    isDone -> PrimaryCocoa
                                                    else -> WarmBorder
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isDone) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        } else {
                                            Text(
                                                text = "${index + 1}",
                                                color = TextSecondary,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = when (step) {
                                            OrderStatus.RECEIVED -> "Received"
                                            OrderStatus.BAKING -> "Baking"
                                            OrderStatus.READY -> "Ready"
                                            OrderStatus.DELIVERED -> "Delivered"
                                            else -> step.name
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCurrent) PrimaryCocoa else TextSecondary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Status Update Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (order.orderStatus != OrderStatus.BAKING.name && order.orderStatus != OrderStatus.READY.name && order.orderStatus != OrderStatus.DELIVERED.name) {
                                Button(
                                    onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.BAKING) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusBaking),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Mark Baking", fontSize = 12.sp)
                                }
                            }

                            if (order.orderStatus == OrderStatus.BAKING.name) {
                                Button(
                                    onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.READY) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusReady),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Mark Ready", fontSize = 12.sp)
                                }
                            }

                            if (order.orderStatus == OrderStatus.READY.name) {
                                Button(
                                    onClick = { viewModel.updateOrderStatus(order.id, OrderStatus.DELIVERED) },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusDelivered),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Mark Delivered", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 2. WHATSAPP & PHONE DIRECT COMMUNICATION ACTIONS
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Customer WhatsApp & Call Actions",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryCocoa)
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Action 1: Full Receipt / Confirmation via WhatsApp
                        Button(
                            onClick = {
                                val msg = WhatsAppHelper.formatOrderReceiptMessage(orderDetails!!)
                                val phone = customer?.phone ?: ""
                                WhatsAppHelper.openWhatsApp(context, phone, msg)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send WhatsApp Receipt / Invoice", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action 2: "Baking Now" WhatsApp Update
                        OutlinedButton(
                            onClick = {
                                val msg = WhatsAppHelper.formatBakingProgressMessage(orderDetails!!)
                                val phone = customer?.phone ?: ""
                                WhatsAppHelper.openWhatsApp(context, phone, msg)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Kitchen, contentDescription = null, tint = StatusBaking, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send 'Cake is Baking!' Update", color = PrimaryCocoa)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action 3: "Order Ready" WhatsApp Alert
                        OutlinedButton(
                            onClick = {
                                val msg = WhatsAppHelper.formatOrderReadyMessage(orderDetails!!)
                                val phone = customer?.phone ?: ""
                                WhatsAppHelper.openWhatsApp(context, phone, msg)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusReady, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send 'Order Ready for Pickup' Alert", color = PrimaryCocoa)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Action 4: Direct Phone Call
                        Button(
                            onClick = {
                                val phone = customer?.phone ?: ""
                                PhoneHelper.callPhone(context, phone)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PhoneBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Call Customer (${customer?.phone ?: ""})", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. CUSTOMER DETAILS CARD
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Customer Information",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryCocoa)
                            )
                            OrderChannelBadge(sourceName = order.orderSource)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(customer?.name ?: "Customer", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("Phone: ${customer?.phone ?: "N/A"}", color = TextSecondary, fontSize = 13.sp)
                        if (customer?.address?.isNotBlank() == true) {
                            Text("Delivery Address: ${customer.address}", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            }

            // 4. ITEMS ORDERED & CUSTOMIZATION
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Items Ordered (${items.size})",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryCocoa)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.itemName, fontWeight = FontWeight.Bold)
                                    Text("${item.variantName} • ₹${item.unitPrice.toInt()} x ${item.quantity}", color = TextSecondary, fontSize = 12.sp)
                                }
                                Text("₹${item.itemTotal.toInt()}", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (order.cakeMessage.isNotBlank()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PrimaryCocoa.copy(alpha = 0.06f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("Message on Cake:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryCocoa)
                                    Text("\"${order.cakeMessage}\"", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (order.customizationNotes.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Design Notes: ${order.customizationNotes}",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }
                    }
                }
            }

            // 5. DELIVERY SCHEDULE & FINANCIALS
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Delivery & Payment Summary",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = PrimaryCocoa)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Delivery Date:", color = TextSecondary)
                            Text("${Formatters.formatDate(order.deliveryDate)} (${order.deliveryTimeSlot})", fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Items Subtotal:", color = TextSecondary)
                            Text(Formatters.formatCurrency(order.subtotal))
                        }

                        if (order.customizationCharge > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Customization Fee:", color = TextSecondary)
                                Text("+${Formatters.formatCurrency(order.customizationCharge)}")
                            }
                        }

                        if (order.discount > 0) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Discount:", color = TextSecondary)
                                Text("-${Formatters.formatCurrency(order.discount)}", color = EgglessGreen)
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = WarmBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Amount:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(Formatters.formatCurrency(order.totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryCocoa)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Advance Paid:", color = AdvancePaidColor)
                            Text(Formatters.formatCurrency(order.advancePaid), color = AdvancePaidColor, fontWeight = FontWeight.SemiBold)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Balance Due:", fontWeight = FontWeight.Bold, color = BalanceDueColor)
                            Text(Formatters.formatCurrency(order.balanceDue), fontWeight = FontWeight.Bold, color = BalanceDueColor)
                        }

                        if (order.balanceDue > 0) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { viewModel.markOrderPaid(order.id, order.totalAmount) },
                                colors = ButtonDefaults.buttonColors(containerColor = EgglessGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark Balance as Paid (₹${order.balanceDue.toInt()})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(48.dp)) }
        }
    }

    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Order #OB-${order.id}?") },
            text = { Text("Are you sure you want to delete this order? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteOrder(order)
                        showDeleteConfirmDialog = false
                        onNavigateBack()
                    }
                ) {
                    Text("Delete", color = BalanceDueColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

