package com.ovenlybytes.app.ui.screens.orders

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovenlybytes.app.data.local.entities.MenuItemEntity
import com.ovenlybytes.app.data.local.entities.OrderEntity
import com.ovenlybytes.app.data.local.entities.OrderItemEntity
import com.ovenlybytes.app.model.OrderSource
import com.ovenlybytes.app.model.OrderStatus
import com.ovenlybytes.app.model.OrderWithCustomerAndItems
import com.ovenlybytes.app.model.PaymentMode
import com.ovenlybytes.app.model.PaymentStatus
import com.ovenlybytes.app.ui.components.OrderChannelBadge
import com.ovenlybytes.app.ui.components.OrderStatusChip
import com.ovenlybytes.app.ui.components.OvenlyTopAppBar
import com.ovenlybytes.app.ui.theme.*
import com.ovenlybytes.app.util.Formatters
import com.ovenlybytes.app.util.PhoneHelper
import com.ovenlybytes.app.util.WhatsAppHelper
import kotlinx.coroutines.launch
import java.util.Calendar

@Composable
fun OrderDetailScreen(
    orderId: Long,
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val orderDetails by viewModel.getOrderDetails(orderId).collectAsState(initial = null)
    val allMenuItems by viewModel.menuItems.collectAsState()

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showEditOrderDialog by remember { mutableStateOf(false) }
    var showEditCustomerDialog by remember { mutableStateOf(false) }

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
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                actions = {
                    IconButton(onClick = { showEditOrderDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Order", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = { showDeleteConfirmDialog = true }) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onPrimary)
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

                        // Action 4: "Payment Received" Thank You Alert
                        if (order.paymentStatus == com.ovenlybytes.app.model.PaymentStatus.COMPLETED.name) {
                            OutlinedButton(
                                onClick = {
                                    val msg = WhatsAppHelper.formatPaymentReceivedMessage(orderDetails!!)
                                    val phone = customer?.phone ?: ""
                                    WhatsAppHelper.openWhatsApp(context, phone, msg)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.VolunteerActivism, contentDescription = null, tint = StatusReady, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Send 'Payment Received' Thank You", color = PrimaryCocoa)
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        // Action 5: Direct Phone Call
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                OrderChannelBadge(sourceName = order.orderSource)
                                if (customer != null) {
                                    IconButton(
                                        onClick = { showEditCustomerDialog = true },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit Customer",
                                            tint = PrimaryCocoa,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
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
                            val iconConfig = remember(item.category, item.itemName) {
                                com.ovenlybytes.app.util.MenuItemIconHelper.getIconConfig(item.category, item.itemName)
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(iconConfig.backgroundColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = iconConfig.icon,
                                        contentDescription = item.itemName,
                                        tint = iconConfig.iconTint,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.itemName, fontWeight = FontWeight.Bold)
                                    Text("${item.variantName} • ₹${item.unitPrice.toInt()} x ${item.quantity}", color = TextSecondary, fontSize = 12.sp)
                                }

                                Text("₹${item.itemTotal.toInt()}", fontWeight = FontWeight.Bold, color = PrimaryCocoa)
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Booking Date:", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary))
                            Text(Formatters.formatDate(order.orderDate), style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Delivery Date:", style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold))
                            Text("${Formatters.formatDate(order.deliveryDate)} (${order.deliveryTimeSlot})", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
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

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Advance Paid:", color = AdvancePaidColor)
                            Text(Formatters.formatCurrency(order.advancePaid), color = AdvancePaidColor, fontWeight = FontWeight.SemiBold)
                        }

                        val paymentModeEnum = remember(order.paymentMode) {
                            com.ovenlybytes.app.model.PaymentMode.entries.find {
                                it.name.equals(order.paymentMode, ignoreCase = true) ||
                                it.displayName.equals(order.paymentMode, ignoreCase = true)
                            }
                        }
                        val paymentModeDisplay = paymentModeEnum?.displayName ?: order.paymentMode.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Payment Mode:", color = TextSecondary)
                            Text(paymentModeDisplay, color = TextSecondary, fontWeight = FontWeight.SemiBold)
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

    if (showEditCustomerDialog && customer != null) {
        com.ovenlybytes.app.ui.screens.customers.CustomerFormDialog(
            customer = customer,
            onDismiss = { showEditCustomerDialog = false },
            onSave = { name, phone, email, address, notes ->
                viewModel.updateCustomer(
                    customer.copy(
                        name = name,
                        phone = phone,
                        email = email,
                        address = address,
                        notes = notes
                    )
                )
                showEditCustomerDialog = false
            }
        )
    }

    if (showEditOrderDialog) {
        EditOrderDialog(
            orderWithDetails = orderDetails!!,
            menuItems = allMenuItems,
            onDismiss = { showEditOrderDialog = false },
            onSave = { updatedOrder, updatedItems ->
                coroutineScope.launch {
                    viewModel.updateOrderWithItems(updatedOrder, updatedItems)
                    showEditOrderDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderDialog(
    orderWithDetails: OrderWithCustomerAndItems,
    menuItems: List<MenuItemEntity>,
    onDismiss: () -> Unit,
    onSave: (OrderEntity, List<OrderItemEntity>) -> Unit
) {
    val order = orderWithDetails.order
    val context = LocalContext.current

    var selectedChannel by remember { mutableStateOf(OrderSource.entries.find { it.name.equals(order.orderSource, ignoreCase = true) } ?: OrderSource.WHATSAPP) }
    var deliveryTimestamp by remember { mutableStateOf(order.deliveryDate) }
    var deliveryTimeSlot by remember { mutableStateOf(order.deliveryTimeSlot) }
    var cakeMessage by remember { mutableStateOf(order.cakeMessage) }
    var customizationNotes by remember { mutableStateOf(order.customizationNotes) }
    var customizationChargeText by remember { mutableStateOf(if (order.customizationCharge > 0) order.customizationCharge.toInt().toString() else "") }
    var discountText by remember { mutableStateOf(if (order.discount > 0) order.discount.toInt().toString() else "") }
    var advancePaidText by remember { mutableStateOf(if (order.advancePaid > 0) order.advancePaid.toInt().toString() else "") }
    var paymentMode by remember { mutableStateOf(PaymentMode.entries.find { it.name.equals(order.paymentMode, ignoreCase = true) } ?: PaymentMode.UPI) }
    var notes by remember { mutableStateOf(order.notes) }

    var cartItems by remember { mutableStateOf(orderWithDetails.items) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingCartItemIndex by remember { mutableStateOf<Int?>(null) }

    val customizationCharge = customizationChargeText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val subtotal = cartItems.sumOf { it.itemTotal }
    val totalAmount = (subtotal + customizationCharge - discount).coerceAtLeast(0.0)
    val advancePaid = advancePaidText.toDoubleOrNull() ?: 0.0
    val balanceDue = (totalAmount - advancePaid).coerceAtLeast(0.0)

    val calendar = remember { Calendar.getInstance().apply { timeInMillis = deliveryTimestamp } }
    val datePickerDialog = remember(order.orderDate) {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                val cal = Calendar.getInstance()
                cal.set(year, month, dayOfMonth, 12, 0, 0)
                deliveryTimestamp = cal.timeInMillis
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.minDate = order.orderDate
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Edit Order #${order.id}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Channel Selector
                Text("Channel Source", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OrderSource.entries.forEach { src ->
                        FilterChip(
                            selected = selectedChannel == src,
                            onClick = { selectedChannel = src },
                            label = { Text(src.displayName, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                HorizontalDivider(color = WarmBorder)

                // Delivery Date & Time Slot
                Text("Delivery Date & Time Slot", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = { datePickerDialog.show() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(Formatters.formatDate(deliveryTimestamp), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedTextField(
                        value = deliveryTimeSlot,
                        onValueChange = { deliveryTimeSlot = it },
                        label = { Text("Time Slot") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider(color = WarmBorder)

                // Cart Items Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Order Items (${cartItems.size})", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    TextButton(onClick = { showAddItemDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Item", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                cartItems.forEachIndexed { index, item ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${item.variantName} • ₹${item.unitPrice.toInt()} x ${item.quantity} = ₹${item.itemTotal.toInt()}", fontSize = 11.sp, color = TextSecondary)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { editingCartItemIndex = index },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit Item Details", tint = TextSecondary)
                                }
                                IconButton(
                                    onClick = {
                                        val updated = cartItems.toMutableList()
                                        if (item.quantity > 1) {
                                            updated[index] = item.copy(
                                                quantity = item.quantity - 1,
                                                itemTotal = item.unitPrice * (item.quantity - 1)
                                            )
                                        } else {
                                            updated.removeAt(index)
                                        }
                                        cartItems = updated
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease", tint = TextSecondary)
                                }
                                Text("${item.quantity}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                                IconButton(
                                    onClick = {
                                        val updated = cartItems.toMutableList()
                                        updated[index] = item.copy(
                                            quantity = item.quantity + 1,
                                            itemTotal = item.unitPrice * (item.quantity + 1)
                                        )
                                        cartItems = updated
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = WarmBorder)

                // Cake Message & Customization Notes
                OutlinedTextField(
                    value = cakeMessage,
                    onValueChange = { cakeMessage = it },
                    label = { Text("Cake Message (Piped on cake)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customizationNotes,
                    onValueChange = { customizationNotes = it },
                    label = { Text("Design / Customization Notes") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                // Financials
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customizationChargeText,
                        onValueChange = { customizationChargeText = it },
                        label = { Text("Extra Charge (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = discountText,
                        onValueChange = { discountText = it },
                        label = { Text("Discount (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = advancePaidText,
                        onValueChange = { advancePaidText = it },
                        label = { Text("Advance Paid (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Balance Due", fontSize = 10.sp, color = TextSecondary)
                            Text(Formatters.formatCurrency(balanceDue), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = BalanceDueColor)
                        }
                    }
                }

                // Payment Mode Selector in Edit Dialog
                Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentMode.entries.forEach { mode ->
                        FilterChip(
                            selected = paymentMode == mode,
                            onClick = { paymentMode = mode },
                            label = { Text(mode.displayName, fontSize = 11.sp, fontWeight = if (paymentMode == mode) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updatedPaymentStatus = when {
                        balanceDue <= 0.0 && totalAmount > 0.0 -> PaymentStatus.COMPLETED.name
                        advancePaid > 0.0 -> PaymentStatus.PARTIAL.name
                        else -> PaymentStatus.PENDING.name
                    }

                    val updatedOrder = order.copy(
                        orderSource = selectedChannel.name,
                        deliveryDate = deliveryTimestamp,
                        deliveryTimeSlot = deliveryTimeSlot,
                        cakeMessage = cakeMessage.trim(),
                        customizationNotes = customizationNotes.trim(),
                        customizationCharge = customizationCharge,
                        discount = discount,
                        subtotal = subtotal,
                        totalAmount = totalAmount,
                        advancePaid = advancePaid,
                        balanceDue = balanceDue,
                        paymentMode = paymentMode.name,
                        paymentStatus = updatedPaymentStatus,
                        notes = notes.trim()
                    )

                    onSave(updatedOrder, cartItems)
                },
                enabled = cartItems.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save Order Changes", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )

    if (editingCartItemIndex != null && editingCartItemIndex!! in cartItems.indices) {
        com.ovenlybytes.app.ui.screens.orders.EditCartItemDialog(
            item = cartItems[editingCartItemIndex!!],
            onDismiss = { editingCartItemIndex = null },
            onSave = { updatedItem ->
                val updatedList = cartItems.toMutableList()
                updatedList[editingCartItemIndex!!] = updatedItem
                cartItems = updatedList
                editingCartItemIndex = null
            }
        )
    }

    if (showAddItemDialog) {
        AddMenuItemDialog(
            menuItems = menuItems,
            onDismiss = { showAddItemDialog = false },
            onItemAdded = { newItem ->
                cartItems = cartItems + newItem
                showAddItemDialog = false
            }
        )
    }
}

