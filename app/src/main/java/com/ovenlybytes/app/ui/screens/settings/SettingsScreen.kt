package com.ovenlybytes.app.ui.screens.settings

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovenlybytes.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToCustomers: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {}
) {
    val context = LocalContext.current
    val shopDetails by viewModel.shopDetails.collectAsState()
    val backupUiState by viewModel.backupUiState.collectAsState()

    var showRestoreConfirmDialog by remember { mutableStateOf(false) }
    var showClearOrdersDialog by remember { mutableStateOf(false) }
    var pendingRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val dateStr = remember { SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date()) }

    // Launchers for EXPORT
    val exportSqlLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        uri?.let { viewModel.exportBackup(context, it, com.ovenlybytes.app.util.BackupFormat.SQL) }
    }

    val exportCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { viewModel.exportBackup(context, it, com.ovenlybytes.app.util.BackupFormat.CSV) }
    }

    val exportJsonLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportBackup(context, it, com.ovenlybytes.app.util.BackupFormat.JSON) }
    }

    // Launcher for IMPORT
    val importBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            pendingRestoreUri = it
            showRestoreConfirmDialog = true
        }
    }

    // React to UI state changes for Toasts / Alerts
    LaunchedEffect(backupUiState) {
        when (val state = backupUiState) {
            is BackupUiState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                viewModel.resetBackupUiState()
            }
            is BackupUiState.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                viewModel.resetBackupUiState()
            }
            else -> {}
        }
    }

    // Confirmation Dialog before Restoring Backup
    if (showRestoreConfirmDialog && pendingRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = StatusBaking) },
            title = { Text("Restore Database Backup?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action will import data from the selected backup file and update your shop details, customers, menu catalog, inventory, and orders.\n\nAre you sure you want to proceed?"
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = pendingRestoreUri
                        showRestoreConfirmDialog = false
                        if (uri != null) {
                            viewModel.importBackupAuto(context, uri)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa)
                ) {
                    Text("Restore Now")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Clear Orders Confirmation Dialog
    if (showClearOrdersDialog) {
        AlertDialog(
            onDismissRequest = { showClearOrdersDialog = false },
            icon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = BalanceDueColor) },
            title = { Text("Clear Orders & Transactions?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action will remove all sample and live order records, order items, and sales financial statistics.\n\nYour shop profile, customer directory, and menu catalog will remain intact."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showClearOrdersDialog = false
                        viewModel.clearAllOrdersAndTransactions()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BalanceDueColor)
                ) {
                    Text("Clear Transactions")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showClearOrdersDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Settings & Backup", fontWeight = FontWeight.Bold)
                        Text(
                            "Shop details, branding & data management",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // SECTION 1: QUICK MANAGEMENT DIRECTORY
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Bakery Directory & Stock Hub",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Clients Directory Card
                            Surface(
                                onClick = onNavigateToCustomers,
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Client CRM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Customer Directory", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }

                            // Inventory Stock Card
                            Surface(
                                onClick = onNavigateToInventory,
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                modifier = Modifier
                                    .weight(1f)
                                    .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Inventory2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Bakery Stock", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text("Ingredients & Boxes", fontSize = 11.sp, color = TextSecondary)
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 2: APP THEMES
                val currentTheme by viewModel.appTheme.collectAsState()

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Palette, contentDescription = null, tint = PrimaryCocoa)
                            Text(
                                text = "App Color Themes",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        HorizontalDivider(color = WarmBorder)

                        Text(
                            "Choose your favorite bakery theme palette:",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        com.ovenlybytes.app.model.AppTheme.entries.forEach { theme ->
                            val isSelected = currentTheme == theme
                            Surface(
                                onClick = { viewModel.selectAppTheme(theme) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) PrimaryCocoa.copy(alpha = 0.08f) else Color(0xFFFAFAFA),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryCocoa else WarmBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        // 3-Color Swatch Preview
                                        Row(horizontalArrangement = Arrangement.spacedBy((-8).dp)) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.primaryColor)
                                                    .border(1.5.dp, Color.White, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                }
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.secondaryColor)
                                                    .border(1.5.dp, Color.White, CircleShape)
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(theme.backgroundColor)
                                                    .border(1.5.dp, WarmBorder, CircleShape)
                                            )
                                        }

                                        Column {
                                            Text(
                                                text = theme.displayName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                )
                                            )
                                            Text(
                                                text = theme.description,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = TextSecondary,
                                                    fontSize = 11.sp
                                                )
                                            )
                                        }
                                    }

                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.selectAppTheme(theme) },
                                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryCocoa)
                                    )
                                }
                            }
                        }
                    }
                }

                // SECTION 2: SHOP DETAILS & BRANDING
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.Store, contentDescription = null, tint = PrimaryCocoa)
                            Text(
                                text = "Shop Details & Branding",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        HorizontalDivider(color = WarmBorder)

                        // Shop Name
                        OutlinedTextField(
                            value = shopDetails.shopName,
                            onValueChange = { viewModel.updateShopDetails(shopDetails.copy(shopName = it)) },
                            label = { Text("Bakery / Shop Name") },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Tagline
                        OutlinedTextField(
                            value = shopDetails.tagline,
                            onValueChange = { viewModel.updateShopDetails(shopDetails.copy(tagline = it)) },
                            label = { Text("Tagline / Slogan") },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Owner Name
                        OutlinedTextField(
                            value = shopDetails.ownerName,
                            onValueChange = { viewModel.updateShopDetails(shopDetails.copy(ownerName = it)) },
                            label = { Text("Owner / Baker Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Phone Number
                        OutlinedTextField(
                            value = shopDetails.phone,
                            onValueChange = { viewModel.updateShopDetails(shopDetails.copy(phone = it)) },
                            label = { Text("WhatsApp / Phone Number") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Email
                        OutlinedTextField(
                            value = shopDetails.email,
                            onValueChange = { viewModel.updateShopDetails(shopDetails.copy(email = it)) },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Address
                        OutlinedTextField(
                            value = shopDetails.address,
                            onValueChange = { viewModel.updateShopDetails(shopDetails.copy(address = it)) },
                            label = { Text("Full Shop Address") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2,
                            maxLines = 3
                        )

                        // FSSAI License Number
                        OutlinedTextField(
                            value = shopDetails.fssaiNumber,
                            onValueChange = { viewModel.updateShopDetails(shopDetails.copy(fssaiNumber = it)) },
                            label = { Text("FSSAI License No.") },
                            leadingIcon = { Icon(Icons.Default.Verified, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // GSTIN Number
                        OutlinedTextField(
                            value = shopDetails.gstNumber,
                            onValueChange = { viewModel.updateShopDetails(shopDetails.copy(gstNumber = it)) },
                            label = { Text("GSTIN Number (Optional)") },
                            leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        // Eggless Badge Toggle
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "100% Eggless Bakery Badge",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                                Text(
                                    text = "Display green eggless badge on top app bar & receipts",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 11.sp)
                                )
                            }
                            Switch(
                                checked = shopDetails.isEgglessOnly,
                                onCheckedChange = { viewModel.updateShopDetails(shopDetails.copy(isEgglessOnly = it)) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = EgglessGreen
                                )
                            )
                        }

                        // Save Shop Profile Button
                        Button(
                            onClick = { viewModel.saveShopDetails() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Shop Details", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }

                // SECTION 2: DATA BACKUP & RESTORE
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.CloudSync, contentDescription = null, tint = PrimaryCocoa)
                            Text(
                                text = "Database Backup & Restore",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        // Export Backup Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PrimaryCocoa.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Download,
                                        contentDescription = null,
                                        tint = PrimaryCocoa,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Export Database Backup",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = PrimaryCocoa)
                                    )
                                }
                                Text(
                                    "Creates a backup file containing all shop settings, customers, menu items, stock inventory, and full order history.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Export SQL Button
                                    Button(
                                        onClick = {
                                            exportSqlLauncher.launch("OvenlyBytes_Backup_$dateStr.sql")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCocoa),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Export SQL Script (.sql)", fontWeight = FontWeight.SemiBold)
                                    }

                                    // Export CSV Button
                                    OutlinedButton(
                                        onClick = {
                                            exportCsvLauncher.launch("OvenlyBytes_Backup_$dateStr.csv")
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Export CSV Spreadsheet (.csv)", fontWeight = FontWeight.SemiBold)
                                    }

                                    // Export JSON Button
                                    TextButton(
                                        onClick = {
                                            exportJsonLauncher.launch("OvenlyBytes_Backup_$dateStr.json")
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.DataObject, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Export JSON Document (.json)", fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // Restore Backup Box
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = StatusBaking.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Upload,
                                        contentDescription = null,
                                        tint = StatusBaking,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Restore From Backup",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = StatusBaking)
                                    )
                                }
                                Text(
                                    "Import records from a previously saved SQL (.sql), CSV (.csv), or JSON (.json) backup file. This will update your database records.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontSize = 12.sp)
                                )
                                Button(
                                    onClick = {
                                        importBackupLauncher.launch("*/*")
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusBaking),
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Select Backup File (.sql / .csv / .json)")
                                }
                            }
                        }
                    }
                }

                // SECTION: CLEAR DATA & RESET
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = BalanceDueColor)
                            Text(
                                text = "Clear Data & Reset",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        HorizontalDivider(color = WarmBorder)

                        Text(
                            "Remove sample/demo records or wipe database before live bakery operations:",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                        )

                        // Clear Orders & Transactions Button
                        OutlinedButton(
                            onClick = { showClearOrdersDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = BalanceDueColor, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Clear All Orders & Transactions", color = BalanceDueColor, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                // SECTION 3: APP INFO
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Ovenly Bytes Bakery POS", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("Version 1.0.0 • Offline Local Database", style = MaterialTheme.typography.bodySmall.copy(color = TextMuted))
                        }
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = null,
                            tint = PrimaryCocoa,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Loading overlay during Backup / Restore
            if (backupUiState is BackupUiState.Loading) {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(color = PrimaryCocoa)
                                Text("Processing Backup / Restore...", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}
