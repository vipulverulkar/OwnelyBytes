package com.ovenlybytes.app.ui.screens.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
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
    var itemToDelete by remember { mutableStateOf<MenuItemEntity?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }
    var currentPage by remember { mutableStateOf(0) }

    LaunchedEffect(searchQuery, selectedCategory) {
        currentPage = 0
    }

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
                MenuCategory.entries.forEach { cat ->
                    val catIconConfig = remember(cat.name) {
                        com.ovenlybytes.app.util.MenuItemIconHelper.getIconConfig(cat.name)
                    }
                    FilterChip(
                        selected = selectedCategory == cat.name,
                        onClick = { viewModel.onCategoryChanged(cat.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = catIconConfig.icon,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text(cat.displayName) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryCocoa,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            HorizontalDivider(color = WarmBorder)

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
                        MenuItemCard(
                            item = item,
                            onEditClick = { itemToEdit = item },
                            onDeleteClick = { itemToDelete = item },
                            onToggleAvailability = { viewModel.toggleAvailability(item) }
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
                                    "Showing ${startIndex + 1}–$endIndex of ${items.size} items",
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

    // Delete Item Confirmation Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Master Menu Item?") },
            text = { Text("Are you sure you want to delete '${itemToDelete?.name}' from the master menu catalog?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteMenuItem(itemToDelete!!)
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
    onDeleteClick: () -> Unit,
    onToggleAvailability: () -> Unit
) {
    val iconConfig = remember(item.category, item.name) {
        com.ovenlybytes.app.util.MenuItemIconHelper.getIconConfig(item.category, item.name)
    }

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
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Item Category Icon Avatar
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(iconConfig.backgroundColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = iconConfig.icon,
                            contentDescription = item.name,
                            tint = iconConfig.iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // 100% Eggless Green Dot Badge
                            if (item.isEggless) {
                                Box(
                                    modifier = Modifier
                                        .size(15.dp)
                                        .border(1.2.dp, EgglessGreen, RoundedCornerShape(3.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .clip(CircleShape)
                                            .background(EgglessGreen)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.isAvailable) PrimaryCocoa else TextMuted
                                )
                            )
                        }

                        if (item.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 2
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit Item",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.DeleteOutline,
                            contentDescription = "Delete Item",
                            tint = BalanceDueColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Switch(
                        checked = item.isAvailable,
                        onCheckedChange = { onToggleAvailability() },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryCocoa, checkedTrackColor = WarmBorder)
                    )
                }
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
                                text = "Seasonal Rate: As per market fruit rate",
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
                            Text(item.size1Name, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text("₹${item.price1.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryCocoa)
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
                            Text(item.size2Name, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                            Text("₹${item.price2.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryCocoa)
                        }
                    }

                    if (item.price3 > 0) {
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
                                Text(item.size3Name, fontSize = 11.sp, color = TextSecondary, fontWeight = FontWeight.Medium)
                                Text("₹${item.price3.toInt()}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryCocoa)
                            }
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
    var name by remember { mutableStateOf(item.name) }
    var category by remember {
        mutableStateOf(
            runCatching { MenuCategory.valueOf(item.category) }.getOrDefault(MenuCategory.CLASSIC_CAKES)
        )
    }
    var description by remember { mutableStateOf(item.description) }
    var isEggless by remember { mutableStateOf(item.isEggless) }
    var size1Name by remember { mutableStateOf(item.size1Name) }
    var price1Text by remember { mutableStateOf(if (item.price1 > 0) item.price1.toInt().toString() else "") }
    var size2Name by remember { mutableStateOf(item.size2Name) }
    var price2Text by remember { mutableStateOf(if (item.price2 > 0) item.price2.toInt().toString() else "") }
    var size3Name by remember { mutableStateOf(item.size3Name) }
    var price3Text by remember { mutableStateOf(if (item.price3 > 0) item.price3.toInt().toString() else "") }
    var isSeasonal by remember { mutableStateOf(item.isSeasonalRate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Item Details & Prices", fontWeight = FontWeight.Bold, color = PrimaryCocoa) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Item Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Category *", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MenuCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryCocoa,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Flavor Notes") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Eggless Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("100% Eggless", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isEggless,
                        onCheckedChange = { isEggless = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EgglessGreen)
                    )
                }

                HorizontalDivider(color = WarmBorder)

                // Seasonal Rate Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Seasonal Fruit Rate", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        Text("Rate varies based on fruit market", fontSize = 11.sp, color = TextSecondary)
                    }
                    Switch(checked = isSeasonal, onCheckedChange = { isSeasonal = it })
                }

                if (!isSeasonal) {
                    Text("Pricing & Portion Sizes (Custom Values)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = PrimaryCocoa))

                    // Preset chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("250 g", "500 g", "1 KG", "1.5 KG", "2 KG").forEach { preset ->
                            SuggestionChip(
                                onClick = {
                                    if (size1Name.isBlank() || size1Name == "½ KG" || size1Name == "500 g") size1Name = preset
                                    else if (size2Name.isBlank() || size2Name == "1 KG") size2Name = preset
                                    else size3Name = preset
                                },
                                label = { Text(preset, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Variant 1
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = size1Name,
                            onValueChange = { size1Name = it },
                            label = { Text("Size / Variant 1") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = price1Text,
                            onValueChange = { price1Text = it },
                            label = { Text("Price 1 (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Variant 2
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = size2Name,
                            onValueChange = { size2Name = it },
                            label = { Text("Size / Variant 2") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = price2Text,
                            onValueChange = { price2Text = it },
                            label = { Text("Price 2 (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Variant 3 / Custom Portion
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = size3Name,
                            onValueChange = { size3Name = it },
                            label = { Text("Size / Variant 3 (Custom)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = price3Text,
                            onValueChange = { price3Text = it },
                            label = { Text("Price 3 (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        item.copy(
                            name = name.trim().ifBlank { item.name },
                            category = category.name,
                            description = description.trim(),
                            isEggless = isEggless,
                            size1Name = size1Name.ifBlank { item.size1Name },
                            price1 = price1Text.toDoubleOrNull() ?: item.price1,
                            size2Name = size2Name.ifBlank { item.size2Name },
                            price2 = price2Text.toDoubleOrNull() ?: item.price2,
                            size3Name = size3Name.ifBlank { "2 KG" },
                            price3 = price3Text.toDoubleOrNull() ?: 0.0,
                            isSeasonalRate = isSeasonal
                        )
                    )
                },
                enabled = name.isNotBlank(),
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
    var size1Name by remember { mutableStateOf("500 g") }
    var size2Name by remember { mutableStateOf("1 KG") }
    var size3Name by remember { mutableStateOf("2 KG") }
    var price1Text by remember { mutableStateOf("") }
    var price2Text by remember { mutableStateOf("") }
    var price3Text by remember { mutableStateOf("") }
    var isEggless by remember { mutableStateOf(true) }
    var isSeasonal by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AddCircle, contentDescription = null, tint = PrimaryCocoa)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Menu Item", fontWeight = FontWeight.Bold, color = PrimaryCocoa)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Item Name *") },
                    placeholder = { Text("e.g., Belgian Truffle Cake") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Selection
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Category *", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = TextSecondary))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        MenuCategory.entries.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = {
                                    category = cat
                                    if (cat == MenuCategory.BROWNIES || cat == MenuCategory.TEA_CAKES || cat == MenuCategory.COOKIES) {
                                        size1Name = "250 g"
                                        size2Name = "500 g"
                                        size3Name = "1 KG"
                                    } else {
                                        size1Name = "500 g"
                                        size2Name = "1 KG"
                                        size3Name = "2 KG"
                                    }
                                },
                                label = { Text(cat.displayName, fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PrimaryCocoa,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Flavor Notes") },
                    placeholder = { Text("e.g., Rich Belgian chocolate layers") },
                    minLines = 2,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                // Eggless Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("100% Eggless", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Switch(
                        checked = isEggless,
                        onCheckedChange = { isEggless = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = EgglessGreen)
                    )
                }

                // Seasonal Rate Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Seasonal Fruit Rate", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Switch(checked = isSeasonal, onCheckedChange = { isSeasonal = it })
                }

                if (!isSeasonal) {
                    HorizontalDivider(color = WarmBorder)
                    Text("Pricing & Portion Sizes (Custom Values)", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = PrimaryCocoa))

                    // Portion Preset Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("250 g", "500 g", "1 KG", "1.5 KG", "2 KG").forEach { preset ->
                            SuggestionChip(
                                onClick = {
                                    if (size1Name.isBlank()) size1Name = preset
                                    else if (size2Name.isBlank()) size2Name = preset
                                    else size3Name = preset
                                },
                                label = { Text(preset, fontSize = 11.sp) }
                            )
                        }
                    }

                    // Variant 1 Name & Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = size1Name,
                            onValueChange = { size1Name = it },
                            label = { Text("Size 1 (e.g. 500 g / ½ KG)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = price1Text,
                            onValueChange = { price1Text = it },
                            label = { Text("Price 1 (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Variant 2 Name & Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = size2Name,
                            onValueChange = { size2Name = it },
                            label = { Text("Size 2 (e.g. 1 KG)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = price2Text,
                            onValueChange = { price2Text = it },
                            label = { Text("Price 2 (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    // Variant 3 / Custom Portion Name & Price
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = size3Name,
                            onValueChange = { size3Name = it },
                            label = { Text("Size 3 (e.g. 2 KG / Custom)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = price3Text,
                            onValueChange = { price3Text = it },
                            label = { Text("Price 3 (₹)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
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
                            isEggless = isEggless,
                            size1Name = size1Name.ifBlank { "500 g" },
                            price1 = price1Text.toDoubleOrNull() ?: 0.0,
                            size2Name = size2Name.ifBlank { "1 KG" },
                            price2 = price2Text.toDoubleOrNull() ?: 0.0,
                            size3Name = size3Name.ifBlank { "2 KG" },
                            price3 = price3Text.toDoubleOrNull() ?: 0.0,
                            isSeasonalRate = isSeasonal
                        )
                    )
                },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa)
            ) {
                Text("Add Item", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

