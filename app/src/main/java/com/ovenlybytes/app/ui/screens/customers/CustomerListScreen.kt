package com.ovenlybytes.app.ui.screens.customers

import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.ovenlybytes.app.ui.components.OvenlyTopAppBar
import com.ovenlybytes.app.ui.theme.*
import com.ovenlybytes.app.util.PhoneHelper
import com.ovenlybytes.app.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    viewModel: CustomerViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val customers by viewModel.filteredCustomers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var customerToEdit by remember { mutableStateOf<CustomerEntity?>(null) }
    var customerToDelete by remember { mutableStateOf<CustomerEntity?>(null) }
    var currentPage by remember { mutableStateOf(0) }

    LaunchedEffect(searchQuery) {
        currentPage = 0
    }

    Scaffold(
        topBar = {
            OvenlyTopAppBar(
                title = "Customers (${customers.size})",
                subtitle = "Client Directory & Contacts",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Customer")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Search Bar
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = { Text("Search customer name or phone...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryCocoa) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            if (customers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PeopleOutline, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No customers found", style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Add customer details to easily take orders over WhatsApp or phone.", color = TextMuted, fontSize = 12.sp)
                    }
                }
            } else {
                val pageSize = 6
                val totalPages = maxOf(1, (customers.size + pageSize - 1) / pageSize)
                val safePage = currentPage.coerceIn(0, totalPages - 1)
                val startIndex = safePage * pageSize
                val endIndex = minOf(startIndex + pageSize, customers.size)
                val paginatedCustomers = remember(customers, safePage) {
                    customers.subList(startIndex, endIndex)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(paginatedCustomers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onWhatsAppClick = {
                                WhatsAppHelper.openWhatsApp(context, customer.phone, "Hi ${customer.name}! Greetings from Ovenly Bites 🍰")
                            },
                            onCallClick = {
                                PhoneHelper.callPhone(context, customer.phone)
                            },
                            onEditClick = {
                                customerToEdit = customer
                            },
                            onDeleteClick = {
                                customerToDelete = customer
                            }
                        )
                    }

                    // Pagination Navigation Bar
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Showing ${startIndex + 1}–$endIndex of ${customers.size} clients",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedIconButton(
                                        onClick = { if (safePage > 0) currentPage = safePage - 1 },
                                        enabled = safePage > 0,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page", modifier = Modifier.size(18.dp))
                                    }

                                    Text(
                                        "${safePage + 1}/$totalPages",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    OutlinedIconButton(
                                        onClick = { if (safePage < totalPages - 1) currentPage = safePage + 1 },
                                        enabled = safePage < totalPages - 1,
                                        modifier = Modifier.size(34.dp)
                                    ) {
                                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Page", modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            }
        }
    }

    // Add / Edit Customer Dialog
    if (showAddDialog || customerToEdit != null) {
        CustomerFormDialog(
            customer = customerToEdit,
            onDismiss = {
                showAddDialog = false
                customerToEdit = null
            },
            onSave = { name, phone, email, address, notes ->
                if (customerToEdit != null) {
                    viewModel.updateCustomer(
                        customerToEdit!!.copy(
                            name = name,
                            phone = phone,
                            email = email,
                            address = address,
                            notes = notes
                        )
                    )
                } else {
                    viewModel.addCustomer(name, phone, email, address, notes)
                }
                showAddDialog = false
                customerToEdit = null
            }
        )
    }

    // Delete Confirmation
    if (customerToDelete != null) {
        AlertDialog(
            onDismissRequest = { customerToDelete = null },
            title = { Text("Delete Customer?") },
            text = { Text("Are you sure you want to delete ${customerToDelete?.name}? This will also delete their associated orders.") },
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
}

@Composable
fun CustomerCard(
    customer: CustomerEntity,
    onWhatsAppClick: () -> Unit,
    onCallClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(PrimaryCocoa.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = customer.name.firstOrNull()?.uppercase() ?: "C",
                            fontWeight = FontWeight.Bold,
                            color = PrimaryCocoa,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text(customer.phone, color = TextSecondary, fontSize = 12.sp)
                    }
                }

                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = BalanceDueColor, modifier = Modifier.size(16.dp))
                    }
                }
            }

            if (customer.address.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = TextMuted, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(customer.address, fontSize = 12.sp, color = TextSecondary)
                }
            }

            if (customer.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Note: ${customer.notes}", fontSize = 11.sp, color = SecondaryRose, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
            }

            Divider(modifier = Modifier.padding(vertical = 10.dp), color = WarmBorder)

            // Contact Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onWhatsAppClick,
                    colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onCallClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PhoneBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Phone", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CustomerFormDialog(
    customer: CustomerEntity?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(customer?.name ?: "") }
    var phone by remember { mutableStateOf(customer?.phone ?: "") }
    var email by remember { mutableStateOf(customer?.email ?: "") }
    var address by remember { mutableStateOf(customer?.address ?: "") }
    var notes by remember { mutableStateOf(customer?.notes ?: "") }

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
                    null, null, null
                )

                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val idIndex = c.getColumnIndex(ContactsContract.Contacts._ID)
                        val nameIndex = c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME)
                        val hasPhoneIndex = c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)

                        val contactId = if (idIndex != -1) c.getString(idIndex) else ""
                        val contactName = if (nameIndex != -1) c.getString(nameIndex) else ""
                        val hasPhone = if (hasPhoneIndex != -1) c.getInt(hasPhoneIndex) > 0 else false

                        if (contactName.isNotBlank()) {
                            name = contactName
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
                                            phone = cleanPhone
                                        }
                                    }
                                }
                            }
                        }
                        Toast.makeText(context, "Contact imported: $contactName", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to read contact: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
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
                Text(if (customer != null) "Edit Customer" else "Add New Customer", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                TextButton(
                    onClick = { pickContactLauncher.launch(null) },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(Icons.Default.Contacts, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Import Phonebook", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    trailingIcon = {
                        IconButton(onClick = { pickContactLauncher.launch(null) }) {
                            Icon(Icons.Default.Contacts, contentDescription = "Pick Contact", tint = MaterialTheme.colorScheme.primary)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number * (for WhatsApp)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Delivery Address") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Preferences / Notes") },
                    placeholder = { Text("e.g. Likes extra chocolate, allergic to nuts") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, phone, email, address, notes) },
                enabled = name.isNotBlank() && phone.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa)
            ) {
                Text("Save Customer")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

