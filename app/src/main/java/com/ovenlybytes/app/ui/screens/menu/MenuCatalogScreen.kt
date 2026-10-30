package com.ovenlybytes.app.ui.screens.menu

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
import com.ovenlybytes.app.data.local.entities.MenuItemEntity
import com.ovenlybytes.app.model.MenuCategory
import com.ovenlybytes.app.ui.components.OvenlyTopAppBar
import com.ovenlybytes.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuCatalogScreen(
    viewModel: MenuViewModel,
    onNavigateBack: () -> Unit,
    onTakeOrderWithItem: (Long) -> Unit
) {
    val items by viewModel.filteredMenuItems.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    var itemToEdit by remember { mutableStateOf<MenuItemEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            OvenlyTopAppBar(
                title = "Ovenly Bites Menu",
                subtitle = "100% Eggless Artisan Bakery",
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
                containerColor = PrimaryCocoa,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(WarmCream)
                .padding(innerPadding)
        ) {
            // Search Input
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChanged,
                    placeholder = { Text("Search cakes, brownies, tea cakes, cookies...") },
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

            // Category Filter Bar (Classic Cakes, Premium Collection, Brownies, Tea Cakes, Cookies)
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
                    label = { Text("All Items") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryCocoa,
                        selectedLabelColor = Color.White
                    )
                )
                MenuCategory.values().forEach { cat ->
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
                    Text("No items found matching your search", color = TextSecondary)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        MenuItemCard(
                            item = item,
                            onEditClick = { itemToEdit = item },
                            onToggleAvailability = { viewModel.toggleAvailability(item) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            }
        }
    }

    // Edit Item Price Dialog
    if (itemToEdit != null) {
        EditMenuPriceDialog(
            item = itemToEdit!!,
            onDismiss = { itemToEdit = null },
            onSave = { updated ->
                viewModel.updateMenuItem(updated)
                itemToEdit = null
            }
        )
    }

    // Add New Menu Item Dialog
    if (showAddDialog) {
        AddNewMenuItemDialog(
            onDismiss = { showAddDialog = false },
            onSave = { newItem ->
                viewModel.addMenuItem(newItem)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MenuItemCard(
    item: MenuItemEntity,
    onEditClick: () -> Unit,
    onToggleAvailability: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.isAvailable) Color.White else Color(0xFFF5F5F5)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isAvailable) 2.dp else 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // 100% Eggless Green Dot Badge
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .border(1.2.dp, EgglessGreen, RoundedCornerShape(3.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(EgglessGreen)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (item.isAvailable) PrimaryCocoa else TextMuted
                        )
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Price", tint = TextSecondary, modifier = Modifier.size(16.dp))
                    }
                    Switch(
                        checked = item.isAvailable,
                        onCheckedChange = { onToggleAvailability() },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCocoa, checkedTrackColor = WarmBorder)
                    )
                }
            }

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                    maxLines = 2
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Pricing Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (item.isSeasonalRate) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFF3E0),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AccessTime, contentDescription = null, tint = StatusReceived, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Seasonal Rate: As per current fruit rate (Customizable in order)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = StatusReceived
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarmCream,
                        modifier = Modifier
                            .weight(1f)
                            .border(0.8.dp, WarmBorder, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.size1Name, fontSize = 12.sp, color = TextSecondary)
                            Text("₹${item.price1.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryCocoa)
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = WarmCream,
                        modifier = Modifier
                            .weight(1f)
                            .border(0.8.dp, WarmBorder, RoundedCornerShape(8.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.size2Name, fontSize = 12.sp, color = TextSecondary)
                            Text("₹${item.price2.toInt()}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryCocoa)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditMenuPriceDialog(
    item: MenuItemEntity,
    onDismiss: () -> Unit,
    onSave: (MenuItemEntity) -> Unit
) {
    var price1Text by remember { mutableStateOf(item.price1.toInt().toString()) }
    var price2Text by remember { mutableStateOf(item.price2.toInt().toString()) }
    var isSeasonal by remember { mutableStateOf(item.isSeasonalRate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Pricing: ${item.name}", fontWeight = FontWeight.Bold, color = PrimaryCocoa) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Seasonal Rate (As per fruit market):", fontSize = 12.sp)
                    Switch(checked = isSeasonal, onCheckedChange = { isSeasonal = it })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price1Text,
                        onValueChange = { price1Text = it },
                        label = { Text("Price ${item.size1Name} (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = price2Text,
                        onValueChange = { price2Text = it },
                        label = { Text("Price ${item.size2Name} (₹)") },
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
                        item.copy(
                            price1 = price1Text.toDoubleOrNull() ?: item.price1,
                            price2 = price2Text.toDoubleOrNull() ?: item.price2,
                            isSeasonalRate = isSeasonal
                        )
                    )
                },
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
fun AddNewMenuItemDialog(
    onDismiss: () -> Unit,
    onSave: (MenuItemEntity) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(MenuCategory.CLASSIC_CAKES) }
    var description by remember { mutableStateOf("") }
    var size1Name by remember { mutableStateOf(category.defaultSizes.first) }
    var size2Name by remember { mutableStateOf(category.defaultSizes.second) }
    var price1Text by remember { mutableStateOf("") }
    var price2Text by remember { mutableStateOf("") }
    var isSeasonal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Menu Item", fontWeight = FontWeight.Bold, color = PrimaryCocoa) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    MenuCategory.values().forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                size1Name = cat.defaultSizes.first
                                size2Name = cat.defaultSizes.second
                            },
                            label = { Text(cat.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = price1Text,
                        onValueChange = { price1Text = it },
                        label = { Text("Price $size1Name (₹)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = price2Text,
                        onValueChange = { price2Text = it },
                        label = { Text("Price $size2Name (₹)") },
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
                        MenuItemEntity(
                            name = name.trim(),
                            category = category.name,
                            description = description.trim(),
                            size1Name = size1Name,
                            price1 = price1Text.toDoubleOrNull() ?: 0.0,
                            size2Name = size2Name,
                            price2 = price2Text.toDoubleOrNull() ?: 0.0,
                            isSeasonalRate = isSeasonal
                        )
                    )
                },
                enabled = name.isNotBlank(),
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

