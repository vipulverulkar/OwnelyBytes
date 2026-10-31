package com.ovenlybytes.app.util

import android.content.Context
import android.net.Uri
import com.ovenlybytes.app.data.local.entities.*
import com.ovenlybytes.app.data.repository.BakeryRepository
import com.ovenlybytes.app.model.ShopDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter

enum class BackupFormat {
    SQL, CSV, JSON
}

object BackupManager {

    // ==========================================
    // EXPORT DISPATCHER
    // ==========================================
    suspend fun exportBackup(
        context: Context,
        uri: Uri,
        format: BackupFormat,
        shopPreferences: ShopPreferences,
        repository: BakeryRepository
    ): Result<String> = withContext(Dispatchers.IO) {
        when (format) {
            BackupFormat.SQL -> exportSqlBackup(context, uri, shopPreferences, repository)
            BackupFormat.CSV -> exportCsvBackup(context, uri, shopPreferences, repository)
            BackupFormat.JSON -> exportJsonBackup(context, uri, shopPreferences, repository)
        }
    }

    // ==========================================
    // IMPORT DISPATCHER
    // ==========================================
    suspend fun importBackup(
        context: Context,
        uri: Uri,
        format: BackupFormat,
        shopPreferences: ShopPreferences,
        repository: BakeryRepository
    ): Result<String> = withContext(Dispatchers.IO) {
        when (format) {
            BackupFormat.SQL -> importSqlBackup(context, uri, shopPreferences, repository)
            BackupFormat.CSV -> importCsvBackup(context, uri, shopPreferences, repository)
            BackupFormat.JSON -> importJsonBackup(context, uri, shopPreferences, repository)
        }
    }

    // ==========================================
    // 1. SQL EXPORT & IMPORT
    // ==========================================
    private suspend fun exportSqlBackup(
        context: Context,
        uri: Uri,
        shopPreferences: ShopPreferences,
        repository: BakeryRepository
    ): Result<String> {
        return try {
            val shop = shopPreferences.getShopDetails()
            val customers = repository.getAllCustomersDirect()
            val menuItems = repository.getAllMenuItemsDirect()
            val inventory = repository.getAllInventoryDirect()
            val orders = repository.getAllOrdersDirect()
            val orderItems = repository.getAllOrderItemsDirect()

            val sb = StringBuilder()
            sb.append("-- OvenlyBytes SQL Backup File\n")
            sb.append("-- Exported At: ${System.currentTimeMillis()}\n\n")

            // Shop Details metadata comment
            sb.append("-- SHOP_DETAILS: ${escapeSql(shop.shopName)},${escapeSql(shop.tagline)},${escapeSql(shop.ownerName)},${escapeSql(shop.phone)},${escapeSql(shop.email)},${escapeSql(shop.address)},${escapeSql(shop.fssaiNumber)},${escapeSql(shop.gstNumber)},${shop.isEgglessOnly}\n\n")

            sb.append("DELETE FROM order_items;\n")
            sb.append("DELETE FROM orders;\n")
            sb.append("DELETE FROM inventory;\n")
            sb.append("DELETE FROM menu_items;\n")
            sb.append("DELETE FROM customers;\n\n")

            // Customers
            customers.forEach { c ->
                sb.append("INSERT INTO customers (id, name, phone, email, address, notes, createdAt) VALUES (${c.id}, ${escapeSql(c.name)}, ${escapeSql(c.phone)}, ${escapeSql(c.email)}, ${escapeSql(c.address)}, ${escapeSql(c.notes)}, ${c.createdAt});\n")
            }
            sb.append("\n")

            // Menu Items
            menuItems.forEach { m ->
                sb.append("INSERT INTO menu_items (id, name, category, description, isEggless, size1Name, price1, size2Name, price2, isSeasonalRate, isAvailable) VALUES (${m.id}, ${escapeSql(m.name)}, ${escapeSql(m.category)}, ${escapeSql(m.description)}, ${if (m.isEggless) 1 else 0}, ${escapeSql(m.size1Name)}, ${m.price1}, ${escapeSql(m.size2Name)}, ${m.price2}, ${if (m.isSeasonalRate) 1 else 0}, ${if (m.isAvailable) 1 else 0});\n")
            }
            sb.append("\n")

            // Inventory
            inventory.forEach { inv ->
                sb.append("INSERT INTO inventory (id, itemName, category, currentQuantity, unit, minThreshold, costPerUnit, lastUpdated) VALUES (${inv.id}, ${escapeSql(inv.itemName)}, ${escapeSql(inv.category)}, ${inv.currentQuantity}, ${escapeSql(inv.unit)}, ${inv.minThreshold}, ${inv.costPerUnit}, ${inv.lastUpdated});\n")
            }
            sb.append("\n")

            // Orders
            orders.forEach { o ->
                sb.append("INSERT INTO orders (id, customerId, orderSource, orderStatus, orderDate, deliveryDate, deliveryTimeSlot, cakeMessage, customizationNotes, customizationCharge, subtotal, totalAmount, advancePaid, balanceDue, paymentMode, paymentStatus, notes) VALUES (${o.id}, ${o.customerId}, ${escapeSql(o.orderSource)}, ${escapeSql(o.orderStatus)}, ${o.orderDate}, ${o.deliveryDate}, ${escapeSql(o.deliveryTimeSlot)}, ${escapeSql(o.cakeMessage)}, ${escapeSql(o.customizationNotes)}, ${o.customizationCharge}, ${o.subtotal}, ${o.totalAmount}, ${o.advancePaid}, ${o.balanceDue}, ${escapeSql(o.paymentMode)}, ${escapeSql(o.paymentStatus)}, ${escapeSql(o.notes)});\n")
            }
            sb.append("\n")

            // Order Items
            orderItems.forEach { oi ->
                sb.append("INSERT INTO order_items (id, orderId, menuItemId, itemName, category, variantName, unitPrice, quantity, itemTotal) VALUES (${oi.id}, ${oi.orderId}, ${oi.menuItemId}, ${escapeSql(oi.itemName)}, ${escapeSql(oi.category)}, ${escapeSql(oi.variantName)}, ${oi.unitPrice}, ${oi.quantity}, ${oi.itemTotal});\n")
            }

            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os, "UTF-8").use { w -> w.write(sb.toString()) }
            } ?: return Result.failure(Exception("Unable to open output file"))

            Result.success("SQL Backup exported successfully! Contains ${customers.size} customers, ${menuItems.size} menu items, ${inventory.size} inventory items, ${orders.size} orders.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun importSqlBackup(
        context: Context,
        uri: Uri,
        shopPreferences: ShopPreferences,
        repository: BakeryRepository
    ): Result<String> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { isStream ->
                BufferedReader(InputStreamReader(isStream, "UTF-8")).use { r -> r.readText() }
            } ?: return Result.failure(Exception("Unable to open input file"))

            val customersList = mutableListOf<CustomerEntity>()
            val menuList = mutableListOf<MenuItemEntity>()
            val inventoryList = mutableListOf<InventoryEntity>()
            val ordersList = mutableListOf<OrderEntity>()
            val orderItemsList = mutableListOf<OrderItemEntity>()

            content.lineSequence().forEach { line ->
                val trimmed = line.trim()
                if (trimmed.startsWith("-- SHOP_DETAILS:")) {
                    val raw = trimmed.substringAfter("-- SHOP_DETAILS:").trim()
                    val parts = raw.split(",")
                    if (parts.size >= 9) {
                        val shop = ShopDetails(
                            shopName = unquoteSql(parts[0]),
                            tagline = unquoteSql(parts[1]),
                            ownerName = unquoteSql(parts[2]),
                            phone = unquoteSql(parts[3]),
                            email = unquoteSql(parts[4]),
                            address = unquoteSql(parts[5]),
                            fssaiNumber = unquoteSql(parts[6]),
                            gstNumber = unquoteSql(parts[7]),
                            isEgglessOnly = parts[8].trim().toBoolean()
                        )
                        shopPreferences.saveShopDetails(shop)
                    }
                } else if (trimmed.startsWith("INSERT INTO customers", ignoreCase = true)) {
                    val values = extractSqlValues(trimmed)
                    if (values.size >= 7) {
                        customersList.add(
                            CustomerEntity(
                                id = values[0].toLong(),
                                name = unquoteSql(values[1]),
                                phone = unquoteSql(values[2]),
                                email = unquoteSql(values[3]),
                                address = unquoteSql(values[4]),
                                notes = unquoteSql(values[5]),
                                createdAt = values[6].toLong()
                            )
                        )
                    }
                } else if (trimmed.startsWith("INSERT INTO menu_items", ignoreCase = true)) {
                    val values = extractSqlValues(trimmed)
                    if (values.size >= 11) {
                        menuList.add(
                            MenuItemEntity(
                                id = values[0].toLong(),
                                name = unquoteSql(values[1]),
                                category = unquoteSql(values[2]),
                                description = unquoteSql(values[3]),
                                isEggless = values[4] == "1" || values[4].lowercase() == "true",
                                size1Name = unquoteSql(values[5]),
                                price1 = values[6].toDouble(),
                                size2Name = unquoteSql(values[7]),
                                price2 = values[8].toDouble(),
                                isSeasonalRate = values[9] == "1" || values[9].lowercase() == "true",
                                isAvailable = values[10] == "1" || values[10].lowercase() == "true"
                            )
                        )
                    }
                } else if (trimmed.startsWith("INSERT INTO inventory", ignoreCase = true)) {
                    val values = extractSqlValues(trimmed)
                    if (values.size >= 8) {
                        inventoryList.add(
                            InventoryEntity(
                                id = values[0].toLong(),
                                itemName = unquoteSql(values[1]),
                                category = unquoteSql(values[2]),
                                currentQuantity = values[3].toDouble(),
                                unit = unquoteSql(values[4]),
                                minThreshold = values[5].toDouble(),
                                costPerUnit = values[6].toDouble(),
                                lastUpdated = values[7].toLong()
                            )
                        )
                    }
                } else if (trimmed.startsWith("INSERT INTO orders", ignoreCase = true)) {
                    val values = extractSqlValues(trimmed)
                    if (values.size >= 17) {
                        ordersList.add(
                            OrderEntity(
                                id = values[0].toLong(),
                                customerId = values[1].toLong(),
                                orderSource = unquoteSql(values[2]),
                                orderStatus = unquoteSql(values[3]),
                                orderDate = values[4].toLong(),
                                deliveryDate = values[5].toLong(),
                                deliveryTimeSlot = unquoteSql(values[6]),
                                cakeMessage = unquoteSql(values[7]),
                                customizationNotes = unquoteSql(values[8]),
                                customizationCharge = values[9].toDouble(),
                                subtotal = values[10].toDouble(),
                                totalAmount = values[11].toDouble(),
                                advancePaid = values[12].toDouble(),
                                balanceDue = values[13].toDouble(),
                                paymentMode = unquoteSql(values[14]),
                                paymentStatus = unquoteSql(values[15]),
                                notes = unquoteSql(values[16])
                            )
                        )
                    }
                } else if (trimmed.startsWith("INSERT INTO order_items", ignoreCase = true)) {
                    val values = extractSqlValues(trimmed)
                    if (values.size >= 9) {
                        orderItemsList.add(
                            OrderItemEntity(
                                id = values[0].toLong(),
                                orderId = values[1].toLong(),
                                menuItemId = values[2].toLong(),
                                itemName = unquoteSql(values[3]),
                                category = unquoteSql(values[4]),
                                variantName = unquoteSql(values[5]),
                                unitPrice = values[6].toDouble(),
                                quantity = values[7].toInt(),
                                itemTotal = values[8].toDouble()
                            )
                        )
                    }
                }
            }

            repository.restoreFullBackup(
                customers = customersList,
                menuItems = menuList,
                inventory = inventoryList,
                orders = ordersList,
                orderItems = orderItemsList
            )

            Result.success("SQL Restore successful! Imported ${customersList.size} customers, ${menuList.size} menu items, ${inventoryList.size} inventory items, ${ordersList.size} orders.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 2. CSV EXPORT & IMPORT
    // ==========================================
    private suspend fun exportCsvBackup(
        context: Context,
        uri: Uri,
        shopPreferences: ShopPreferences,
        repository: BakeryRepository
    ): Result<String> {
        return try {
            val shop = shopPreferences.getShopDetails()
            val customers = repository.getAllCustomersDirect()
            val menuItems = repository.getAllMenuItemsDirect()
            val inventory = repository.getAllInventoryDirect()
            val orders = repository.getAllOrdersDirect()
            val orderItems = repository.getAllOrderItemsDirect()

            val sb = StringBuilder()
            sb.append("# OVENLYBYTES_CSV_BACKUP_V1\n")

            // Shop Details Section
            sb.append("# SECTION:SHOP_DETAILS\n")
            sb.append("ShopName,Tagline,OwnerName,Phone,Email,Address,FSSAINumber,GSTNumber,IsEggless\n")
            sb.append("${escapeCsv(shop.shopName)},${escapeCsv(shop.tagline)},${escapeCsv(shop.ownerName)},${escapeCsv(shop.phone)},${escapeCsv(shop.email)},${escapeCsv(shop.address)},${escapeCsv(shop.fssaiNumber)},${escapeCsv(shop.gstNumber)},${shop.isEgglessOnly}\n\n")

            // Customers Section
            sb.append("# SECTION:CUSTOMERS\n")
            sb.append("Id,Name,Phone,Email,Address,Notes,CreatedAt\n")
            customers.forEach { c ->
                sb.append("${c.id},${escapeCsv(c.name)},${escapeCsv(c.phone)},${escapeCsv(c.email)},${escapeCsv(c.address)},${escapeCsv(c.notes)},${c.createdAt}\n")
            }
            sb.append("\n")

            // Menu Items Section
            sb.append("# SECTION:MENU_ITEMS\n")
            sb.append("Id,Name,Category,Description,IsEggless,Size1Name,Price1,Size2Name,Price2,IsSeasonalRate,IsAvailable\n")
            menuItems.forEach { m ->
                sb.append("${m.id},${escapeCsv(m.name)},${escapeCsv(m.category)},${escapeCsv(m.description)},${m.isEggless},${escapeCsv(m.size1Name)},${m.price1},${escapeCsv(m.size2Name)},${m.price2},${m.isSeasonalRate},${m.isAvailable}\n")
            }
            sb.append("\n")

            // Inventory Section
            sb.append("# SECTION:INVENTORY\n")
            sb.append("Id,ItemName,Category,CurrentQuantity,Unit,MinThreshold,CostPerUnit,LastUpdated\n")
            inventory.forEach { inv ->
                sb.append("${inv.id},${escapeCsv(inv.itemName)},${escapeCsv(inv.category)},${inv.currentQuantity},${escapeCsv(inv.unit)},${inv.minThreshold},${inv.costPerUnit},${inv.lastUpdated}\n")
            }
            sb.append("\n")

            // Orders Section
            sb.append("# SECTION:ORDERS\n")
            sb.append("Id,CustomerId,OrderSource,OrderStatus,OrderDate,DeliveryDate,DeliveryTimeSlot,CakeMessage,CustomizationNotes,CustomizationCharge,Subtotal,TotalAmount,AdvancePaid,BalanceDue,PaymentMode,PaymentStatus,Notes\n")
            orders.forEach { o ->
                sb.append("${o.id},${o.customerId},${escapeCsv(o.orderSource)},${escapeCsv(o.orderStatus)},${o.orderDate},${o.deliveryDate},${escapeCsv(o.deliveryTimeSlot)},${escapeCsv(o.cakeMessage)},${escapeCsv(o.customizationNotes)},${o.customizationCharge},${o.subtotal},${o.totalAmount},${o.advancePaid},${o.balanceDue},${escapeCsv(o.paymentMode)},${escapeCsv(o.paymentStatus)},${escapeCsv(o.notes)}\n")
            }
            sb.append("\n")

            // Order Items Section
            sb.append("# SECTION:ORDER_ITEMS\n")
            sb.append("Id,OrderId,MenuItemId,ItemName,Category,VariantName,UnitPrice,Quantity,ItemTotal\n")
            orderItems.forEach { oi ->
                sb.append("${oi.id},${oi.orderId},${oi.menuItemId},${escapeCsv(oi.itemName)},${escapeCsv(oi.category)},${escapeCsv(oi.variantName)},${oi.unitPrice},${oi.quantity},${oi.itemTotal}\n")
            }

            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os, "UTF-8").use { w -> w.write(sb.toString()) }
            } ?: return Result.failure(Exception("Unable to open output file"))

            Result.success("CSV Backup exported successfully! Contains ${customers.size} customers, ${menuItems.size} menu items, ${inventory.size} inventory items, ${orders.size} orders.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun importCsvBackup(
        context: Context,
        uri: Uri,
        shopPreferences: ShopPreferences,
        repository: BakeryRepository
    ): Result<String> {
        return try {
            val lines = context.contentResolver.openInputStream(uri)?.use { isStream ->
                BufferedReader(InputStreamReader(isStream, "UTF-8")).use { r -> r.readLines() }
            } ?: return Result.failure(Exception("Unable to open CSV backup file"))

            var currentSection = ""
            val customersList = mutableListOf<CustomerEntity>()
            val menuList = mutableListOf<MenuItemEntity>()
            val inventoryList = mutableListOf<InventoryEntity>()
            val ordersList = mutableListOf<OrderEntity>()
            val orderItemsList = mutableListOf<OrderItemEntity>()

            for (line in lines) {
                val trimmed = line.trim()
                if (trimmed.isEmpty()) continue

                if (trimmed.startsWith("# SECTION:")) {
                    currentSection = trimmed.substringAfter("# SECTION:").trim()
                    continue
                }

                if (trimmed.startsWith("ShopName,") || trimmed.startsWith("Id,")) {
                    // Header row
                    continue
                }

                val row = parseCsvRow(trimmed)

                when (currentSection) {
                    "SHOP_DETAILS" -> {
                        if (row.size >= 9) {
                            shopPreferences.saveShopDetails(
                                ShopDetails(
                                    shopName = unescapeCsv(row[0]),
                                    tagline = unescapeCsv(row[1]),
                                    ownerName = unescapeCsv(row[2]),
                                    phone = unescapeCsv(row[3]),
                                    email = unescapeCsv(row[4]),
                                    address = unescapeCsv(row[5]),
                                    fssaiNumber = unescapeCsv(row[6]),
                                    gstNumber = unescapeCsv(row[7]),
                                    isEgglessOnly = row[8].toBoolean()
                                )
                            )
                        }
                    }
                    "CUSTOMERS" -> {
                        if (row.size >= 7) {
                            customersList.add(
                                CustomerEntity(
                                    id = row[0].toLong(),
                                    name = unescapeCsv(row[1]),
                                    phone = unescapeCsv(row[2]),
                                    email = unescapeCsv(row[3]),
                                    address = unescapeCsv(row[4]),
                                    notes = unescapeCsv(row[5]),
                                    createdAt = row[6].toLong()
                                )
                            )
                        }
                    }
                    "MENU_ITEMS" -> {
                        if (row.size >= 11) {
                            menuList.add(
                                MenuItemEntity(
                                    id = row[0].toLong(),
                                    name = unescapeCsv(row[1]),
                                    category = unescapeCsv(row[2]),
                                    description = unescapeCsv(row[3]),
                                    isEggless = row[4].toBoolean(),
                                    size1Name = unescapeCsv(row[5]),
                                    price1 = row[6].toDouble(),
                                    size2Name = unescapeCsv(row[7]),
                                    price2 = row[8].toDouble(),
                                    isSeasonalRate = row[9].toBoolean(),
                                    isAvailable = row[10].toBoolean()
                                )
                            )
                        }
                    }
                    "INVENTORY" -> {
                        if (row.size >= 8) {
                            inventoryList.add(
                                InventoryEntity(
                                    id = row[0].toLong(),
                                    itemName = unescapeCsv(row[1]),
                                    category = unescapeCsv(row[2]),
                                    currentQuantity = row[3].toDouble(),
                                    unit = unescapeCsv(row[4]),
                                    minThreshold = row[5].toDouble(),
                                    costPerUnit = row[6].toDouble(),
                                    lastUpdated = row[7].toLong()
                                )
                            )
                        }
                    }
                    "ORDERS" -> {
                        if (row.size >= 17) {
                            ordersList.add(
                                OrderEntity(
                                    id = row[0].toLong(),
                                    customerId = row[1].toLong(),
                                    orderSource = unescapeCsv(row[2]),
                                    orderStatus = unescapeCsv(row[3]),
                                    orderDate = row[4].toLong(),
                                    deliveryDate = row[5].toLong(),
                                    deliveryTimeSlot = unescapeCsv(row[6]),
                                    cakeMessage = unescapeCsv(row[7]),
                                    customizationNotes = unescapeCsv(row[8]),
                                    customizationCharge = row[9].toDouble(),
                                    subtotal = row[10].toDouble(),
                                    totalAmount = row[11].toDouble(),
                                    advancePaid = row[12].toDouble(),
                                    balanceDue = row[13].toDouble(),
                                    paymentMode = unescapeCsv(row[14]),
                                    paymentStatus = unescapeCsv(row[15]),
                                    notes = unescapeCsv(row[16])
                                )
                            )
                        }
                    }
                    "ORDER_ITEMS" -> {
                        if (row.size >= 9) {
                            orderItemsList.add(
                                OrderItemEntity(
                                    id = row[0].toLong(),
                                    orderId = row[1].toLong(),
                                    menuItemId = row[2].toLong(),
                                    itemName = unescapeCsv(row[3]),
                                    category = unescapeCsv(row[4]),
                                    variantName = unescapeCsv(row[5]),
                                    unitPrice = row[6].toDouble(),
                                    quantity = row[7].toInt(),
                                    itemTotal = row[8].toDouble()
                                )
                            )
                        }
                    }
                }
            }

            repository.restoreFullBackup(
                customers = customersList,
                menuItems = menuList,
                inventory = inventoryList,
                orders = ordersList,
                orderItems = orderItemsList
            )

            Result.success("CSV Restore successful! Imported ${customersList.size} customers, ${menuList.size} menu items, ${inventoryList.size} inventory items, ${ordersList.size} orders.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // 3. JSON EXPORT & IMPORT
    // ==========================================
    private suspend fun exportJsonBackup(
        context: Context,
        uri: Uri,
        shopPreferences: ShopPreferences,
        repository: BakeryRepository
    ): Result<String> {
        return try {
            val root = JSONObject()
            root.put("version", 1)
            root.put("appName", "OvenlyBytes")
            root.put("timestamp", System.currentTimeMillis())

            val shopDetails = shopPreferences.getShopDetails()
            val shopJson = JSONObject().apply {
                put("shopName", shopDetails.shopName)
                put("tagline", shopDetails.tagline)
                put("ownerName", shopDetails.ownerName)
                put("phone", shopDetails.phone)
                put("email", shopDetails.email)
                put("address", shopDetails.address)
                put("fssaiNumber", shopDetails.fssaiNumber)
                put("gstNumber", shopDetails.gstNumber)
                put("isEgglessOnly", shopDetails.isEgglessOnly)
            }
            root.put("shopDetails", shopJson)

            val customers = repository.getAllCustomersDirect()
            val customersArray = JSONArray()
            customers.forEach { c ->
                customersArray.put(JSONObject().apply {
                    put("id", c.id)
                    put("name", c.name)
                    put("phone", c.phone)
                    put("email", c.email)
                    put("address", c.address)
                    put("notes", c.notes)
                    put("createdAt", c.createdAt)
                })
            }
            root.put("customers", customersArray)

            val menuItems = repository.getAllMenuItemsDirect()
            val menuArray = JSONArray()
            menuItems.forEach { m ->
                menuArray.put(JSONObject().apply {
                    put("id", m.id)
                    put("name", m.name)
                    put("category", m.category)
                    put("description", m.description)
                    put("isEggless", m.isEggless)
                    put("size1Name", m.size1Name)
                    put("price1", m.price1)
                    put("size2Name", m.size2Name)
                    put("price2", m.price2)
                    put("size3Name", m.size3Name)
                    put("price3", m.price3)
                    put("isSeasonalRate", m.isSeasonalRate)
                    put("isAvailable", m.isAvailable)
                })
            }
            root.put("menuItems", menuArray)

            val inventory = repository.getAllInventoryDirect()
            val inventoryArray = JSONArray()
            inventory.forEach { inv ->
                inventoryArray.put(JSONObject().apply {
                    put("id", inv.id)
                    put("itemName", inv.itemName)
                    put("category", inv.category)
                    put("currentQuantity", inv.currentQuantity)
                    put("unit", inv.unit)
                    put("minThreshold", inv.minThreshold)
                    put("costPerUnit", inv.costPerUnit)
                    put("lastUpdated", inv.lastUpdated)
                })
            }
            root.put("inventory", inventoryArray)

            val orders = repository.getAllOrdersDirect()
            val ordersArray = JSONArray()
            orders.forEach { o ->
                ordersArray.put(JSONObject().apply {
                    put("id", o.id)
                    put("customerId", o.customerId)
                    put("orderSource", o.orderSource)
                    put("orderStatus", o.orderStatus)
                    put("orderDate", o.orderDate)
                    put("deliveryDate", o.deliveryDate)
                    put("deliveryTimeSlot", o.deliveryTimeSlot)
                    put("cakeMessage", o.cakeMessage)
                    put("customizationNotes", o.customizationNotes)
                    put("customizationCharge", o.customizationCharge)
                    put("subtotal", o.subtotal)
                    put("totalAmount", o.totalAmount)
                    put("advancePaid", o.advancePaid)
                    put("balanceDue", o.balanceDue)
                    put("paymentMode", o.paymentMode)
                    put("paymentStatus", o.paymentStatus)
                    put("notes", o.notes)
                })
            }
            root.put("orders", ordersArray)

            val orderItems = repository.getAllOrderItemsDirect()
            val orderItemsArray = JSONArray()
            orderItems.forEach { oi ->
                orderItemsArray.put(JSONObject().apply {
                    put("id", oi.id)
                    put("orderId", oi.orderId)
                    put("menuItemId", oi.menuItemId)
                    put("itemName", oi.itemName)
                    put("category", oi.category)
                    put("variantName", oi.variantName)
                    put("unitPrice", oi.unitPrice)
                    put("quantity", oi.quantity)
                    put("itemTotal", oi.itemTotal)
                })
            }
            root.put("orderItems", orderItemsArray)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, "UTF-8").use { writer ->
                    writer.write(root.toString(2))
                }
            } ?: return Result.failure(Exception("Unable to open output stream"))

            Result.success("JSON Backup exported successfully! Contains ${customers.size} customers, ${menuItems.size} menu items, ${inventory.size} inventory items, ${orders.size} orders.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun importJsonBackup(
        context: Context,
        uri: Uri,
        shopPreferences: ShopPreferences,
        repository: BakeryRepository
    ): Result<String> {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, "UTF-8")).use { reader ->
                    reader.readText()
                }
            } ?: return Result.failure(Exception("Unable to open input file"))

            val root = JSONObject(jsonString)

            if (!root.has("appName") || root.getString("appName") != "OvenlyBytes") {
                return Result.failure(Exception("Invalid backup file format. Expected OvenlyBytes backup."))
            }

            if (root.has("shopDetails")) {
                val shopJson = root.getJSONObject("shopDetails")
                val details = ShopDetails(
                    shopName = shopJson.optString("shopName", "Ovenly Bites"),
                    tagline = shopJson.optString("tagline", "Freshly Baked With Love ❤️"),
                    ownerName = shopJson.optString("ownerName", ""),
                    phone = shopJson.optString("phone", ""),
                    email = shopJson.optString("email", ""),
                    address = shopJson.optString("address", ""),
                    fssaiNumber = shopJson.optString("fssaiNumber", ""),
                    gstNumber = shopJson.optString("gstNumber", ""),
                    isEgglessOnly = shopJson.optBoolean("isEgglessOnly", true)
                )
                shopPreferences.saveShopDetails(details)
            }

            val customersList = mutableListOf<CustomerEntity>()
            if (root.has("customers")) {
                val arr = root.getJSONArray("customers")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    customersList.add(
                        CustomerEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            phone = obj.getString("phone"),
                            email = obj.optString("email", ""),
                            address = obj.optString("address", ""),
                            notes = obj.optString("notes", ""),
                            createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                        )
                    )
                }
            }

            val menuList = mutableListOf<MenuItemEntity>()
            if (root.has("menuItems")) {
                val arr = root.getJSONArray("menuItems")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    menuList.add(
                        MenuItemEntity(
                            id = obj.getLong("id"),
                            name = obj.getString("name"),
                            category = obj.getString("category"),
                            description = obj.optString("description", ""),
                            isEggless = obj.optBoolean("isEggless", true),
                            size1Name = obj.optString("size1Name", "½ KG"),
                            price1 = obj.getDouble("price1"),
                            size2Name = obj.optString("size2Name", "1 KG"),
                            price2 = obj.optDouble("price2", 0.0),
                            size3Name = obj.optString("size3Name", "2 KG"),
                            price3 = obj.optDouble("price3", 0.0),
                            isSeasonalRate = obj.optBoolean("isSeasonalRate", false),
                            isAvailable = obj.optBoolean("isAvailable", true)
                        )
                    )
                }
            }

            val inventoryList = mutableListOf<InventoryEntity>()
            if (root.has("inventory")) {
                val arr = root.getJSONArray("inventory")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    inventoryList.add(
                        InventoryEntity(
                            id = obj.getLong("id"),
                            itemName = obj.getString("itemName"),
                            category = obj.getString("category"),
                            currentQuantity = obj.getDouble("currentQuantity"),
                            unit = obj.getString("unit"),
                            minThreshold = obj.getDouble("minThreshold"),
                            costPerUnit = obj.getDouble("costPerUnit"),
                            lastUpdated = obj.optLong("lastUpdated", System.currentTimeMillis())
                        )
                    )
                }
            }

            val ordersList = mutableListOf<OrderEntity>()
            if (root.has("orders")) {
                val arr = root.getJSONArray("orders")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    ordersList.add(
                        OrderEntity(
                            id = obj.getLong("id"),
                            customerId = obj.getLong("customerId"),
                            orderSource = obj.getString("orderSource"),
                            orderStatus = obj.getString("orderStatus"),
                            orderDate = obj.getLong("orderDate"),
                            deliveryDate = obj.getLong("deliveryDate"),
                            deliveryTimeSlot = obj.getString("deliveryTimeSlot"),
                            cakeMessage = obj.optString("cakeMessage", ""),
                            customizationNotes = obj.optString("customizationNotes", ""),
                            customizationCharge = obj.optDouble("customizationCharge", 0.0),
                            subtotal = obj.getDouble("subtotal"),
                            totalAmount = obj.getDouble("totalAmount"),
                            advancePaid = obj.optDouble("advancePaid", 0.0),
                            balanceDue = obj.optDouble("balanceDue", 0.0),
                            paymentMode = obj.optString("paymentMode", "UPI"),
                            paymentStatus = obj.optString("paymentStatus", "UNPAID"),
                            notes = obj.optString("notes", "")
                        )
                    )
                }
            }

            val orderItemsList = mutableListOf<OrderItemEntity>()
            if (root.has("orderItems")) {
                val arr = root.getJSONArray("orderItems")
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    orderItemsList.add(
                        OrderItemEntity(
                            id = obj.getLong("id"),
                            orderId = obj.getLong("orderId"),
                            menuItemId = obj.getLong("menuItemId"),
                            itemName = obj.getString("itemName"),
                            category = obj.getString("category"),
                            variantName = obj.getString("variantName"),
                            unitPrice = obj.getDouble("unitPrice"),
                            quantity = obj.getInt("quantity"),
                            itemTotal = obj.getDouble("itemTotal")
                        )
                    )
                }
            }

            repository.restoreFullBackup(
                customers = customersList,
                menuItems = menuList,
                inventory = inventoryList,
                orders = ordersList,
                orderItems = orderItemsList
            )

            Result.success("JSON Restore successful! Imported ${customersList.size} customers, ${menuList.size} menu items, ${inventoryList.size} inventory items, ${ordersList.size} orders.")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ==========================================
    // HELPERS FOR SQL & CSV
    // ==========================================
    private fun escapeSql(value: String): String {
        return "'" + value.replace("'", "''") + "'"
    }

    private fun unquoteSql(value: String): String {
        val trimmed = value.trim()
        if (trimmed.startsWith("'") && trimmed.endsWith("'") && trimmed.length >= 2) {
            return trimmed.substring(1, trimmed.length - 1).replace("''", "'")
        }
        return trimmed
    }

    private fun extractSqlValues(sqlLine: String): List<String> {
        val rawValues = sqlLine.substringAfter("VALUES").trim()
            .trimStart('(').trimEnd(')', ';')
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < rawValues.length) {
            val c = rawValues[i]
            if (c == '\'') {
                if (inQuotes && i + 1 < rawValues.length && rawValues[i + 1] == '\'') {
                    current.append('\'')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString().trim())
                current.clear()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString().trim())
        return result
    }

    private fun escapeCsv(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    private fun unescapeCsv(value: String): String {
        var trimmed = value.trim()
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"") && trimmed.length >= 2) {
            trimmed = trimmed.substring(1, trimmed.length - 1)
        }
        return trimmed.replace("\"\"", "\"")
    }

    private fun parseCsvRow(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                    current.append('"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString())
                current.clear()
            } else {
                current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }
}
