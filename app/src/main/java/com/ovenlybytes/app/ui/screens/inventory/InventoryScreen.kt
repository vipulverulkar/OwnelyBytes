package com.ovenlybytes.app.ui.screens.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovenlybytes.app.data.local.entities.InventoryEntity
import com.ovenlybytes.app.model.InventoryCategory
import com.ovenlybytes.app.ui.components.OvenlyTopAppBar
import com.ovenlybytes.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateBack: () -> Unit
) {
    val items by viewModel.filteredInventory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var itemToRestock by remember { mutableStateOf<InventoryEntity?>(null) }
    var itemToDelete by remember { mutableStateOf<InventoryEntity?>(null) }
    var currentPage by remember { mutableStateOf(0) }

    LaunchedEffect(searchQuery, selectedCategory) {
        currentPage = 0
    }

    val lowStockCount = items.count { it.currentQuantity <= it.minThreshold }

    Scaffold(
        topBar = {
            OvenlyTopAppBar(
                title = "Bakery Inventory",
                subtitle = "Stock & Ingredients Management",
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
                Icon(Icons.Default.Add, contentDescription = "Add Item")
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
                    placeholder = { Text("Search ingredient, box, chocolate...") },
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
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Low Stock Warning Banner
            if (lowStockCount > 0) {
                Surface(
                    color = LowStockBackground,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = LowStockAlertColor, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$lowStockCount item(s) are below minimum reorder threshold!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = LowStockAlertColor
                        )
                    }
                }
            }

            // Category Filter Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == "ALL",
                    onClick = { viewModel.onCategoryChanged("ALL") },
                    label = { Text("All Supplies") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryCocoa,
                        selectedLabelColor = Color.White
                    )
                )
                InventoryCategory.values().forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat.name,
                        onClick = { viewModel.onCategoryChanged(cat.name) },
                        label = { Text(cat.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryCocoa,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Divider(color = WarmBorder)

            // Items List
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory2, contentDescription = null, tint = TextMuted, modifier = Modifier.size(64.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No stock items found", style = MaterialTheme.typography.titleMedium.copy(color = TextSecondary))
                    }
                }
            } else {
                val pageSize = 6
                val totalPages = maxOf(1, (items.size + pageSize - 1) / pageSize)
                val safePage = currentPage.coerceIn(0, totalPages - 1)
                val startIndex = safePage * pageSize
                val endIndex = minOf(startIndex + pageSize, items.size)
                val paginatedItems = remember(items, safePage) {
                    items.subList(startIndex, endIndex)
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(paginatedItems, key = { it.id }) { item ->
                        InventoryRowCard(
                            item = item,
                            onRestockClick = { itemToRestock = item },
                            onDeleteClick = { itemToDelete = item }
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
                                    "Showing ${startIndex + 1}–$endIndex of ${items.size} supplies",
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

    // Restock Dialog
    if (itemToRestock != null) {
        RestockDialog(
            item = itemToRestock!!,
            onDismiss = { itemToRestock = null },
            onConfirm = { addedQty ->
                viewModel.restock(itemToRestock!!.id, itemToRestock!!.currentQuantity, addedQty)
                itemToRestock = null
            }
        )
    }

    // Add Item Dialog
    if (showAddDialog) {
        AddInventoryDialog(
            onDismiss = { showAddDialog = false },
            onSave = { name, cat, qty, unit, threshold, cost ->
                viewModel.addInventoryItem(name, cat, qty, unit, threshold, cost)
                showAddDialog = false
            }
        )
    }

    // Delete Confirmation
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete ${itemToDelete?.itemName}?") },
            text = { Text("Are you sure you want to remove this inventory item?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteInventoryItem(itemToDelete!!)
                        itemToDelete = null
                    }
                ) {
                    Text("Delete", color = BalanceDueColor, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
fun InventoryRowCard(
    item: InventoryEntity,
    onRestockClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isLowStock = item.currentQuantity <= item.minThreshold

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.itemName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    if (isLowStock) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = LowStockBackground
                        ) {
                            Text(
                                text = "LOW STOCK",
                                color = LowStockAlertColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Category: ${item.category} • Min threshold: ${item.minThreshold} ${item.unit}",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${item.currentQuantity} ${item.unit}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isLowStock) LowStockAlertColor else PrimaryCocoa
                    )
                    Text(
                        text = "Cost: ₹${item.costPerUnit.toInt()}/${item.unit}",
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Restock Button
                Button(
                    onClick = onRestockClick,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Stock +/-", fontSize = 11.sp)
                }

                IconButton(onClick = onDeleteClick, modifier = Modifier.size(28.dp)) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                }
            }
        }
    }
}

@Composable
fun RestockDialog(
    item: InventoryEntity,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var deltaText by remember { mutableStateOf("") }
    var isAdding by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Stock: ${item.itemName}", fontWeight = FontWeight.Bold, color = PrimaryCocoa) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Current Available: ${item.currentQuantity} ${item.unit}", fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = isAdding,
                        onClick = { isAdding = true },
                        label = { Text("+ Restock (Add)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isAdding,
                        onClick = { isAdding = false },
                        label = { Text("- Consumed (Use)") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = deltaText,
                    onValueChange = { deltaText = it },
                    label = { Text("Quantity (${item.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = deltaText.toDoubleOrNull() ?: 0.0
                    onConfirm(if (isAdding) amount else -amount)
                },
                enabled = deltaText.toDoubleOrNull() != null && (deltaText.toDoubleOrNull() ?: 0.0) > 0.0,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa)
            ) {
                Text("Save")
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
fun AddInventoryDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, Double, String, Double, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(InventoryCategory.INGREDIENTS.name) }
    var qtyText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("kg") }
    var thresholdText by remember { mutableStateOf("") }
    var costText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Bakery Supply / Item", fontWeight = FontWeight.Bold, color = PrimaryCocoa) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g. Belgian Chocolate, Cake Box") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Category Picker
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    InventoryCategory.values().forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat.name,
                            onClick = { selectedCategory = cat.name },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = qtyText,
                        onValueChange = { qtyText = it },
                        label = { Text("Current Stock *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("Unit (kg, pcs...)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = thresholdText,
                        onValueChange = { thresholdText = it },
                        label = { Text("Min Threshold") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = costText,
                        onValueChange = { costText = it },
                        label = { Text("Cost / Unit (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        name,
                        selectedCategory,
                        qtyText.toDoubleOrNull() ?: 0.0,
                        unit,
                        thresholdText.toDoubleOrNull() ?: 0.0,
                        costText.toDoubleOrNull() ?: 0.0
                    )
                },
                enabled = name.isNotBlank() && qtyText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa)
            ) {
                Text("Add Item")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

