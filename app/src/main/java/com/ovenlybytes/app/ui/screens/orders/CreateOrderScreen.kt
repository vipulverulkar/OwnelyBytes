package com.ovenlybytes.app.ui.screens.orders

import android.Manifest
import android.app.DatePickerDialog
import android.content.pm.PackageManager
import android.provider.ContactsContract
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.ovenlybytes.app.data.local.entities.CustomerEntity
import com.ovenlybytes.app.data.local.entities.MenuItemEntity
import com.ovenlybytes.app.data.local.entities.OrderEntity
import com.ovenlybytes.app.data.local.entities.OrderItemEntity
import com.ovenlybytes.app.model.*
import com.ovenlybytes.app.ui.components.OvenlyTopAppBar
import com.ovenlybytes.app.ui.theme.*
import com.ovenlybytes.app.util.Formatters
import com.ovenlybytes.app.util.WhatsAppHelper
import kotlinx.coroutines.launch
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrderScreen(
    viewModel: OrdersViewModel,
    onNavigateBack: () -> Unit,
    onOrderCreated: (Long) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val existingCustomers by viewModel.customers.collectAsState()
    val allMenuItems by viewModel.menuItems.collectAsState()

    // 1. Order Source / Channel
    var selectedChannel by remember { mutableStateOf(OrderSource.WHATSAPP) }

    // 2. Customer State
    var isNewCustomer by remember { mutableStateOf(true) }
    var selectedExistingCustomer by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerName by remember { mutableStateOf("") }
    var customerPhone by remember { mutableStateOf("") }
    var customerAddress by remember { mutableStateOf("") }
    var customerSearchQuery by remember { mutableStateOf("") }
    var customerPage by remember { mutableStateOf(0) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }

    // Contact Book Picker Launcher
    val pickContactLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { contactUri ->
        if (contactUri != null) {
            try {
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(
                    contactUri,
                    arrayOf(
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.HAS_PHONE_NUMBER
                    ),
                    null,
                    null,
                    null
                )

                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val hasPhoneIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                        val contactId = if (idIndex != -1) c.getString(idIndex) else ""
                        val name = if (nameIndex != -1) c.getString(nameIndex) else ""
                        val hasPhone = if (hasPhoneIndex != -1) c.getInt(hasPhoneIndex) > 0 else false

                        if (name.isNotBlank()) {
                            customerName = name
                        }

                        if (hasPhone && contactId.isNotBlank()) {
                            val phoneCursor = contentResolver.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
                                "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
                                arrayOf(contactId),
                                null
                            )

                            phoneCursor?.use { pc ->
                                if (pc.moveToFirst()) {
                                    val phoneNumIndex = pc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                                    if (phoneNumIndex != -1) {
                                        val rawPhone = pc.getString(phoneNumIndex) ?: ""
                                        val cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
                                        if (cleanPhone.isNotBlank()) {
                                            customerPhone = cleanPhone
                                        }
                                    }
                                }
                            }
                        }
                        Toast.makeText(context, "Contact imported: $name", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read contact: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val requestContactsPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                pickContactLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Could not launch contact picker", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "Contacts permission is required to import from phonebook", Toast.LENGTH_LONG).show()
        }
    }

    fun launchContactPicker() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                pickContactLauncher.launch(null)
            } catch (e: Exception) {
                Toast.makeText(context, "Unable to launch contact picker", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
        }
    }

    // 3. Cart Items
    var cartItems by remember { mutableStateOf(listOf<OrderItemEntity>()) }
    var showAddItemDialog by remember { mutableStateOf(false) }
    var editingCartItemIndex by remember { mutableStateOf<Int?>(null) }

    // 4. Cake Customization
    var cakeMessage by remember { mutableStateOf("") }
    var customizationNotes by remember { mutableStateOf("") }
    var customizationChargeText by remember { mutableStateOf("") }

    // 5. Delivery Details
    val calendar = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) } } // Default 2 days ahead
    var deliveryTimestamp by remember { mutableStateOf(calendar.timeInMillis) }
    var deliveryTimeSlot by remember { mutableStateOf("5:00 PM - 7:00 PM") }

    // 6. Financials
    var discountText by remember { mutableStateOf("") }
    var advancePaidText by remember { mutableStateOf("") }
    var paymentMode by remember { mutableStateOf(PaymentMode.UPI) }

    val customizationCharge = customizationChargeText.toDoubleOrNull() ?: 0.0
    val discount = discountText.toDoubleOrNull() ?: 0.0
    val subtotal = cartItems.sumOf { it.itemTotal }
    val totalAmount = (subtotal + customizationCharge - discount).coerceAtLeast(0.0)
    val advancePaid = advancePaidText.toDoubleOrNull() ?: 0.0
    val balanceDue = (totalAmount - advancePaid).coerceAtLeast(0.0)

    val paymentStatus = when {
        balanceDue <= 0.0 && totalAmount > 0.0 -> PaymentStatus.COMPLETED
        advancePaid > 0.0 -> PaymentStatus.PARTIAL
        else -> PaymentStatus.PENDING
    }

    // 2-day notice validation check
    val daysUntilDelivery = Formatters.getDaysDifference(deliveryTimestamp)
    val isShortNotice = daysUntilDelivery < 2

    // Date Picker Dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 12, 0, 0)
            deliveryTimestamp = cal.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun submitOrder(openWhatsAppReceipt: Boolean) {
        if (cartItems.isEmpty()) return
        val finalCustName = if (isNewCustomer) customerName.trim() else selectedExistingCustomer?.name ?: ""
        val finalCustPhone = if (isNewCustomer) customerPhone.trim() else selectedExistingCustomer?.phone ?: ""

        if (finalCustName.isBlank() || finalCustPhone.isBlank()) return

        val customer = if (isNewCustomer) {
            CustomerEntity(
                name = finalCustName,
                phone = finalCustPhone,
                address = customerAddress.trim()
            )
        } else {
            selectedExistingCustomer!!
        }

        val order = OrderEntity(
            customerId = customer.id,
            orderSource = selectedChannel.name,
            orderStatus = OrderStatus.RECEIVED.name,
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
            paymentStatus = paymentStatus.name
        )

        coroutineScope.launch {
            val orderId = viewModel.createOrder(customer, isNewCustomer, order, cartItems)
            if (openWhatsAppReceipt) {
                val fullOrderDetails = OrderWithCustomerAndItems(
                    order = order.copy(id = orderId),
                    customer = customer,
                    items = cartItems
                )
                val receiptMsg = WhatsAppHelper.formatOrderReceiptMessage(fullOrderDetails)
                WhatsAppHelper.openWhatsApp(context, finalCustPhone, receiptMsg)
            }
            onOrderCreated(orderId)
        }
    }

    Scaffold(
        topBar = {
            OvenlyTopAppBar(
                title = "New Customer Order",
                subtitle = "Take order from WhatsApp or Phone",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
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

            // STEP 1: ORDER SOURCE (WhatsApp / Phone / Walk-in)
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "1. Order Received Channel",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCocoa
                            )
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // WhatsApp Option
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedChannel == OrderSource.WHATSAPP) WhatsAppBackground else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedChannel = OrderSource.WHATSAPP }
                                    .border(
                                        width = if (selectedChannel == OrderSource.WHATSAPP) 2.dp else 1.dp,
                                        color = if (selectedChannel == OrderSource.WHATSAPP) WhatsAppGreenDark else WarmBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Chat,
                                        contentDescription = null,
                                        tint = WhatsAppGreenDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "WhatsApp",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = WhatsAppGreenDark
                                    )
                                }
                            }

                            // Phone Call Option
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedChannel == OrderSource.PHONE) PhoneBlueBackground else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedChannel = OrderSource.PHONE }
                                    .border(
                                        width = if (selectedChannel == OrderSource.PHONE) 2.dp else 1.dp,
                                        color = if (selectedChannel == OrderSource.PHONE) PhoneBlue else WarmBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = null,
                                        tint = PhoneBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Phone Call",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = PhoneBlue
                                    )
                                }
                            }

                            // Walk-in Option
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedChannel == OrderSource.WALK_IN) WalkInPurpleBackground else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedChannel = OrderSource.WALK_IN }
                                    .border(
                                        width = if (selectedChannel == OrderSource.WALK_IN) 2.dp else 1.dp,
                                        color = if (selectedChannel == OrderSource.WALK_IN) WalkInPurple else WarmBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = WalkInPurple,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Walk-in",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = WalkInPurple
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // STEP 2: CUSTOMER DETAILS
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
                                text = "2. Customer Details",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCocoa
                                )
                            )
                            Row {
                                FilterChip(
                                    selected = isNewCustomer,
                                    onClick = { isNewCustomer = true },
                                    label = { Text("New", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                FilterChip(
                                    selected = !isNewCustomer,
                                    onClick = { isNewCustomer = false },
                                    label = { Text("Saved", fontSize = 11.sp) },
                                    modifier = Modifier.height(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isNewCustomer) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { launchContactPicker() },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        Icons.Default.ContactPage,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Pick from Phonebook",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = customerName,
                                onValueChange = { customerName = it },
                                label = { Text("Customer Name *") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(onClick = { launchContactPicker() }) {
                                        Icon(
                                            Icons.Default.Contacts,
                                            contentDescription = "Pick Contact",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customerPhone,
                                onValueChange = { customerPhone = it },
                                label = { Text("Phone Number * (for WhatsApp)") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = customerAddress,
                                onValueChange = { customerAddress = it },
                                label = { Text("Delivery Address / Landmark") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 2
                            )
                        } else {
                            // Searchable & Paginated Saved Customer Directory
                            if (existingCustomers.isEmpty()) {
                                Text("No saved customers yet. Create a new one above.", color = TextMuted)
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Search input for saved customers
                                    OutlinedTextField(
                                        value = customerSearchQuery,
                                        onValueChange = {
                                            customerSearchQuery = it
                                            customerPage = 0 // Reset to first page on search
                                        },
                                        placeholder = { Text("Search client by name, phone or address...") },
                                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                        trailingIcon = {
                                            if (customerSearchQuery.isNotBlank()) {
                                                IconButton(onClick = {
                                                    customerSearchQuery = ""
                                                    customerPage = 0
                                                }) {
                                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                                }
                                            }
                                        },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    // Filtering & Pagination calculations
                                    val filteredSavedCustomers = remember(existingCustomers, customerSearchQuery) {
                                        if (customerSearchQuery.isBlank()) existingCustomers
                                        else existingCustomers.filter {
                                            it.name.contains(customerSearchQuery, ignoreCase = true) ||
                                            it.phone.contains(customerSearchQuery, ignoreCase = true) ||
                                            it.address.contains(customerSearchQuery, ignoreCase = true)
                                        }
                                    }

                                    val pageSize = 5
                                    val totalPages = maxOf(1, (filteredSavedCustomers.size + pageSize - 1) / pageSize)
                                    val currentPage = customerPage.coerceIn(0, totalPages - 1)

                                    val startIndex = currentPage * pageSize
                                    val endIndex = minOf(startIndex + pageSize, filteredSavedCustomers.size)

                                    val paginatedCustomers = remember(filteredSavedCustomers, currentPage) {
                                        if (startIndex < filteredSavedCustomers.size) {
                                            filteredSavedCustomers.subList(startIndex, endIndex)
                                        } else emptyList()
                                    }

                                    if (filteredSavedCustomers.isEmpty()) {
                                        Text(
                                            "No saved clients match '$customerSearchQuery'",
                                            color = TextMuted,
                                            fontSize = 12.sp,
                                            modifier = Modifier.padding(vertical = 8.dp)
                                        )
                                    } else {
                                        // Customer Cards List (5 items per page)
                                        paginatedCustomers.forEach { cust ->
                                            val isSelected = selectedExistingCustomer?.id == cust.id
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedExistingCustomer = cust }
                                                    .border(
                                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else WarmBorder,
                                                        shape = RoundedCornerShape(10.dp)
                                                    )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        RadioButton(
                                                            selected = isSelected,
                                                            onClick = { selectedExistingCustomer = cust }
                                                        )
                                                        Spacer(modifier = Modifier.width(6.dp))
                                                        Column {
                                                            Text(cust.name, fontWeight = FontWeight.Bold)
                                                            Text("${cust.phone} • ${cust.address.take(30)}", fontSize = 11.sp, color = TextSecondary)
                                                        }
                                                    }

                                                    Row {
                                                        IconButton(onClick = { customerToEdit = cust }, modifier = Modifier.size(28.dp)) {
                                                            Icon(Icons.Default.Edit, contentDescription = "Edit Customer", tint = TextSecondary, modifier = Modifier.size(16.dp))
                                                        }
                                                        IconButton(onClick = { customerToDelete = cust }, modifier = Modifier.size(28.dp)) {
                                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Customer", tint = BalanceDueColor, modifier = Modifier.size(16.dp))
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        // Pagination Navigation Bar
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(top = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                "Showing ${startIndex + 1}–$endIndex of ${filteredSavedCustomers.size}",
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                OutlinedIconButton(
                                                    onClick = { if (currentPage > 0) customerPage = currentPage - 1 },
                                                    enabled = currentPage > 0,
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.ChevronLeft,
                                                        contentDescription = "Previous Page",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }

                                                Text(
                                                    "${currentPage + 1}/$totalPages",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp)
                                                )

                                                OutlinedIconButton(
                                                    onClick = { if (currentPage < totalPages - 1) customerPage = currentPage + 1 },
                                                    enabled = currentPage < totalPages - 1,
                                                    modifier = Modifier.size(32.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.ChevronRight,
                                                        contentDescription = "Next Page",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // STEP 3: ITEMS FROM OVENLY BITES MENU
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
                                text = "3. Menu Items Ordered",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCocoa
                                )
                            )
                            Button(
                                onClick = { showAddItemDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Item", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (cartItems.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = WarmCream,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showAddItemDialog = true }
                                    .padding(vertical = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.Cake, contentDescription = null, tint = TextMuted, modifier = Modifier.size(32.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("No items added yet", fontWeight = FontWeight.SemiBold, color = TextSecondary)
                                    Text("Tap '+ Add Item' to choose from Ovenly Bites menu", fontSize = 11.sp, color = TextMuted)
                                }
                            }
                        } else {
                            cartItems.forEachIndexed { index, item ->
                                val iconConfig = remember(item.category, item.itemName) {
                                    com.ovenlybytes.app.util.MenuItemIconHelper.getIconConfig(item.category, item.itemName)
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
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

                                            Column {
                                                Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text(
                                                    "${item.variantName} • ₹${item.unitPrice.toInt()} x ${item.quantity} = ₹${item.itemTotal.toInt()}",
                                                    fontSize = 12.sp,
                                                    color = TextSecondary
                                                )
                                            }
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
                                            Text("${item.quantity}", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp))
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
                                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = PrimaryCocoa)
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                Text(
                                    "Items Subtotal: ₹${subtotal.toInt()}",
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCocoa
                                )
                            }
                        }
                    }
                }
            }

            // STEP 4: CAKE CUSTOMIZATION & MESSAGE
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = SecondaryRose)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "4. Cake Customization & Notes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCocoa
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = cakeMessage,
                            onValueChange = { cakeMessage = it },
                            label = { Text("Message to pipe on Cake") },
                            placeholder = { Text("e.g. Happy 25th Birthday Ananya ❤️") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customizationNotes,
                            onValueChange = { customizationNotes = it },
                            label = { Text("Design / Theme / Flavor notes") },
                            placeholder = { Text("e.g. Gold flakes, chocolate drip, less sweet") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = customizationChargeText,
                            onValueChange = { customizationChargeText = it },
                            label = { Text("Extra charge for customization (₹)") },
                            placeholder = { Text("0") },
                            supportingText = { Text("As per menu: Extra charge applies for custom cakes") },
                            leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }

            // STEP 5: DELIVERY SCHEDULE & 2-DAY POLICY CHECK
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = PrimaryCocoa)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "5. Delivery Schedule",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryCocoa
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Notice reminder
                        if (isShortNotice) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFFF3E0),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = StatusReceived, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Notice: Ovenly Bites policy is 'Order 2 days before'. Delivery is set for less than 2 days.",
                                        fontSize = 11.sp,
                                        color = StatusReceived,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        // Date Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = WarmCream,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { datePickerDialog.show() }
                                .border(1.dp, WarmBorder, RoundedCornerShape(12.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Event, contentDescription = null, tint = PrimaryCocoa)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text("Delivery Date", fontSize = 11.sp, color = TextSecondary)
                                        Text(
                                            Formatters.formatDate(deliveryTimestamp),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isShortNotice) Color(0xFFFFEBEE) else EgglessGreenLight
                                ) {
                                    Text(
                                        text = Formatters.getDeliveryBadgeText(deliveryTimestamp),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isShortNotice) BalanceDueColor else EgglessGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Time Slot Selector
                        Text("Delivery Time Slot", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "10:00 AM - 12:00 PM",
                                "1:00 PM - 3:00 PM",
                                "5:00 PM - 7:00 PM",
                                "7:00 PM - 9:00 PM",
                                "Anytime"
                            ).forEach { slot ->
                                FilterChip(
                                    selected = deliveryTimeSlot == slot,
                                    onClick = { deliveryTimeSlot = slot },
                                    label = { Text(slot, fontSize = 11.sp) }
                                )
                            }
                        }
                    }
                }
            }

            // STEP 6: BILLING & ADVANCE PAYMENT
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "6. Payment & Summary",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = PrimaryCocoa
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = advancePaidText,
                                onValueChange = { advancePaidText = it },
                                label = { Text("Advance Paid (₹)") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = discountText,
                                onValueChange = { discountText = it },
                                label = { Text("Discount (₹)") },
                                placeholder = { Text("0") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Payment Mode Selector
                        Text("Payment Mode", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            PaymentMode.values().forEach { mode ->
                                FilterChip(
                                    selected = paymentMode == mode,
                                    onClick = { paymentMode = mode },
                                    label = { Text(mode.name, fontSize = 11.sp) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp), color = WarmBorder)

                        // Totals Summary Box
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = WarmCream,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Items Subtotal:", color = TextSecondary)
                                    Text(Formatters.formatCurrency(subtotal), fontWeight = FontWeight.SemiBold)
                                }
                                if (customizationCharge > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Customization Extra:", color = TextSecondary)
                                        Text("+${Formatters.formatCurrency(customizationCharge)}", fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                if (discount > 0) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Discount:", color = TextSecondary)
                                        Text("-${Formatters.formatCurrency(discount)}", color = EgglessGreen, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                                Divider(modifier = Modifier.padding(vertical = 6.dp), color = WarmBorder)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Total Amount:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text(Formatters.formatCurrency(totalAmount), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryCocoa)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Advance Paid:", color = AdvancePaidColor)
                                    Text(Formatters.formatCurrency(advancePaid), color = AdvancePaidColor, fontWeight = FontWeight.SemiBold)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Balance Due on Delivery:", fontWeight = FontWeight.Bold, color = BalanceDueColor)
                                    Text(Formatters.formatCurrency(balanceDue), fontWeight = FontWeight.Bold, color = BalanceDueColor)
                                }
                            }
                        }
                    }
                }
            }

            // ACTION BUTTONS: SAVE & WHATSAPP
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = { submitOrder(openWhatsAppReceipt = true) },
                        enabled = cartItems.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.Chat, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save & Send WhatsApp Receipt", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    OutlinedButton(
                        onClick = { submitOrder(openWhatsAppReceipt = false) },
                        enabled = cartItems.isNotEmpty(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryCocoa),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("Save Order Only", fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }

    // Customer Edit Dialog
    if (customerToEdit != null) {
        com.ovenlybytes.app.ui.screens.customers.CustomerFormDialog(
            customer = customerToEdit,
            onDismiss = { customerToEdit = null },
            onSave = { name, phone, email, address, notes ->
                viewModel.updateCustomer(
                    customerToEdit!!.copy(
                        name = name,
                        phone = phone,
                        email = email,
                        address = address,
                        notes = notes
                    )
                )
                customerToEdit = null
            }
        )
    }

    // Customer Delete Confirmation
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Saved Customer?") },
            text = { Text("Are you sure you want to delete ${customerToDelete?.name}? This will also delete their associated order records.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCustomer(customerToDelete!!)
                        customerToDelete = null
                    }
                ) {
                    Text("Delete", color = BalanceDueColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { customerToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
    // DIALOG: EDIT CART ITEM
    if (editingCartItemIndex != null && editingCartItemIndex!! in cartItems.indices) {
        EditCartItemDialog(
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

    // DIALOG: ADD MENU ITEM (Directly lists items from the Ovenly Bites Menu or Custom Item)
    if (showAddItemDialog) {
        AddMenuItemDialog(
            menuItems = allMenuItems,
            onDismiss = { showAddItemDialog = false },
            onItemAdded = { newItem ->
                cartItems = cartItems + newItem
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun EditCartItemDialog(
    item: OrderItemEntity,
    onDismiss: () -> Unit,
    onSave: (OrderItemEntity) -> Unit
) {
    var itemName by remember { mutableStateOf(item.itemName) }
    var variantName by remember { mutableStateOf(item.variantName) }
    var unitPriceText by remember { mutableStateOf(if (item.unitPrice > 0) item.unitPrice.toInt().toString() else "") }
    var quantity by remember { mutableStateOf(item.quantity) }

    val unitPrice = unitPriceText.toDoubleOrNull() ?: item.unitPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Cart Item Details", fontWeight = FontWeight.Bold, color = PrimaryCocoa) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    label = { Text("Item Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = variantName,
                    onValueChange = { variantName = it },
                    label = { Text("Portion / Variant Name") },
                    placeholder = { Text("e.g. 1 KG, Custom Pack") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = unitPriceText,
                    onValueChange = { unitPriceText = it },
                    label = { Text("Unit Price (₹) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Quantity", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (quantity > 1) quantity-- },
                            enabled = quantity > 1
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                        }
                        Text("$quantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                        IconButton(onClick = { quantity++ }) {
                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = PrimaryCocoa)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        item.copy(
                            itemName = itemName.trim().ifBlank { item.itemName },
                            variantName = variantName.trim().ifBlank { "Standard" },
                            unitPrice = unitPrice,
                            quantity = quantity,
                            itemTotal = unitPrice * quantity
                        )
                    )
                },
                enabled = itemName.isNotBlank() && unitPrice > 0,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa)
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AddMenuItemDialog(
    menuItems: List<MenuItemEntity>,
    onDismiss: () -> Unit,
    onItemAdded: (OrderItemEntity) -> Unit
) {
    var isCustomTab by remember { mutableStateOf(false) }

    // States for Menu Selection Tab
    var selectedCategory by remember { mutableStateOf<MenuCategory?>(null) }
    var selectedMenuItem by remember { mutableStateOf<MenuItemEntity?>(null) }
    var selectedVariant by remember { mutableStateOf("½ KG") }
    var customVariantText by remember { mutableStateOf("") }
    var customPriceText by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf(1) }

    // States for Custom Item Tab
    var customItemName by remember { mutableStateOf("") }
    var customItemCategory by remember { mutableStateOf(MenuCategory.CUSTOM) }
    var customItemVariant by remember { mutableStateOf("Custom Portion") }
    var customItemUnitPriceText by remember { mutableStateOf("") }
    var customItemQuantity by remember { mutableStateOf(1) }

    val categories = MenuCategory.values().toList()

    val filteredItems = remember(selectedCategory, menuItems) {
        if (selectedCategory == null) menuItems else menuItems.filter { it.category == selectedCategory!!.name }
    }

    val unitPrice = remember(selectedMenuItem, selectedVariant, customPriceText) {
        if (selectedMenuItem == null) 0.0
        else if (selectedMenuItem!!.isSeasonalRate) {
            customPriceText.toDoubleOrNull() ?: 0.0
        } else if (selectedVariant == "+ Custom Size / Price") {
            customPriceText.toDoubleOrNull() ?: 0.0
        } else {
            val item = selectedMenuItem!!
            val base1KgPrice = if (item.price2 > 0) item.price2 else item.price1 * 2.0
            val baseHalfKgPrice = if (item.price1 > 0) item.price1 else item.price2 / 2.0
            val base2KgPrice = if (item.price3 > 0) item.price3 else base1KgPrice * 2.0

            when (selectedVariant) {
                item.size1Name, "½ KG", "250 g" -> baseHalfKgPrice
                item.size2Name, "1 KG", "500 g" -> base1KgPrice
                "1.5 KG" -> base1KgPrice * 1.5
                item.size3Name, "2 KG" -> if (item.price3 > 0) item.price3 else base2KgPrice
                "2.5 KG" -> base1KgPrice * 2.5
                "3 KG" -> base1KgPrice * 3.0
                else -> item.price1
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text("Add Item to Order", fontWeight = FontWeight.Bold, color = PrimaryCocoa)
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = !isCustomTab,
                        onClick = { isCustomTab = false },
                        label = { Text("From Menu Items", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = isCustomTab,
                        onClick = { isCustomTab = true },
                        label = { Text("+ Custom Item", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        text = {
            if (!isCustomTab) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 380.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Category Pills
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("All", fontSize = 11.sp) }
                            )
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = selectedCategory == cat,
                                    onClick = { selectedCategory = cat },
                                    label = { Text(cat.displayName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    // Items list
                    items(filteredItems) { item ->
                        val isSelected = selectedMenuItem?.id == item.id
                        val iconConfig = remember(item.category, item.name) {
                            com.ovenlybytes.app.util.MenuItemIconHelper.getIconConfig(item.category, item.name)
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.background,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMenuItem = item
                                    selectedVariant = item.size1Name
                                    customVariantText = ""
                                    if (item.isSeasonalRate) customPriceText = item.price1.toInt().toString() else customPriceText = ""
                                }
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else WarmBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(iconConfig.backgroundColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = iconConfig.icon,
                                                contentDescription = item.name,
                                                tint = iconConfig.iconTint,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(item.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }

                                    if (item.isSeasonalRate) {
                                        Text("Per fruit rate", fontSize = 11.sp, color = SecondaryRose, fontWeight = FontWeight.Bold)
                                    }
                                }
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        val variantsList = buildList {
                                            if (item.size1Name.isNotBlank()) add(item.size1Name)
                                            if (item.size2Name.isNotBlank()) add(item.size2Name)
                                            if (item.size3Name.isNotBlank()) add(item.size3Name)
                                            add("250 g")
                                            add("500 g")
                                            add("1 KG")
                                            add("1.5 KG")
                                            add("2 KG")
                                            add("+ Custom Size / Price")
                                        }.distinct()

                                        val base1KgPrice = if (item.price2 > 0) item.price2 else item.price1 * 2.0
                                        val baseHalfKgPrice = if (item.price1 > 0) item.price1 else item.price2 / 2.0

                                        variantsList.forEach { variantLabel ->
                                            val vPrice = when (variantLabel) {
                                                item.size1Name -> item.price1
                                                item.size2Name -> item.price2
                                                item.size3Name -> if (item.price3 > 0) item.price3 else base1KgPrice * 2.0
                                                "250 g" -> baseHalfKgPrice / 2.0
                                                "500 g", "½ KG" -> baseHalfKgPrice
                                                "1 KG" -> base1KgPrice
                                                "1.5 KG" -> base1KgPrice * 1.5
                                                "2 KG" -> if (item.price3 > 0) item.price3 else base1KgPrice * 2.0
                                                "+ Custom Size / Price" -> customPriceText.toDoubleOrNull() ?: 0.0
                                                else -> item.price1
                                            }
                                            val chipLabel = if (variantLabel == "+ Custom Size / Price") {
                                                "+ Custom"
                                            } else {
                                                "$variantLabel (₹${vPrice.toInt()})"
                                            }
                                            FilterChip(
                                                selected = selectedVariant == variantLabel,
                                                onClick = {
                                                    selectedVariant = variantLabel
                                                    if (variantLabel == "+ Custom Size / Price" && customPriceText.isBlank()) {
                                                        customPriceText = item.price1.toInt().toString()
                                                    }
                                                },
                                                label = { Text(chipLabel, fontSize = 11.sp) }
                                            )
                                        }
                                    }
                                    if (selectedVariant == "+ Custom Size / Price") {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(
                                                value = customVariantText,
                                                onValueChange = { customVariantText = it },
                                                label = { Text("Custom Size / Portion Name") },
                                                placeholder = { Text("e.g. 4 KG, Custom Pack, Box of 12") },
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            OutlinedTextField(
                                                value = customPriceText,
                                                onValueChange = { customPriceText = it },
                                                label = { Text("Custom Unit Price (₹)") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                singleLine = true,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    } else if (item.isSeasonalRate) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        OutlinedTextField(
                                            value = customPriceText,
                                            onValueChange = { customPriceText = it },
                                            label = { Text("Seasonal Price for this order (₹)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Custom Item Tab Form
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = customItemName,
                        onValueChange = { customItemName = it },
                        label = { Text("Item Name *") },
                        placeholder = { Text("e.g. Custom Theme Photo Cake") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Category", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            categories.forEach { cat ->
                                FilterChip(
                                    selected = customItemCategory == cat,
                                    onClick = { customItemCategory = cat },
                                    label = { Text(cat.displayName, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customItemVariant,
                        onValueChange = { customItemVariant = it },
                        label = { Text("Portion / Variant Name") },
                        placeholder = { Text("e.g. 2 KG / Custom Box") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = customItemUnitPriceText,
                        onValueChange = { customItemUnitPriceText = it },
                        label = { Text("Unit Price (₹) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Quantity", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { if (customItemQuantity > 1) customItemQuantity-- },
                                enabled = customItemQuantity > 1
                            ) {
                                Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Decrease")
                            }
                            Text("$customItemQuantity", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp))
                            IconButton(onClick = { customItemQuantity++ }) {
                                Icon(Icons.Default.AddCircleOutline, contentDescription = "Increase", tint = PrimaryCocoa)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isCustomTab) {
                Button(
                    onClick = {
                        if (selectedMenuItem != null) {
                            val finalVariantName = if (selectedVariant == "+ Custom Size / Price") {
                                customVariantText.ifBlank { "Custom Portion" }
                            } else {
                                selectedVariant
                            }
                            onItemAdded(
                                OrderItemEntity(
                                    orderId = 0,
                                    menuItemId = selectedMenuItem!!.id,
                                    itemName = selectedMenuItem!!.name,
                                    category = selectedMenuItem!!.category,
                                    variantName = finalVariantName,
                                    unitPrice = unitPrice,
                                    quantity = quantity,
                                    itemTotal = unitPrice * quantity
                                )
                            )
                        }
                    },
                    enabled = selectedMenuItem != null && unitPrice > 0.0,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa)
                ) {
                    Text("Add to Order (₹${(unitPrice * quantity).toInt()})")
                }
            } else {
                val price = customItemUnitPriceText.toDoubleOrNull() ?: 0.0
                Button(
                    onClick = {
                        onItemAdded(
                            OrderItemEntity(
                                orderId = 0,
                                menuItemId = null,
                                itemName = customItemName.trim(),
                                category = customItemCategory.name,
                                variantName = customItemVariant.trim().ifBlank { "Custom" },
                                unitPrice = price,
                                quantity = customItemQuantity,
                                itemTotal = price * customItemQuantity
                            )
                        )
                    },
                    enabled = customItemName.isNotBlank() && price > 0.0,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa)
                ) {
                    Text("Add Custom Item (₹${(price * customItemQuantity).toInt()})")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

