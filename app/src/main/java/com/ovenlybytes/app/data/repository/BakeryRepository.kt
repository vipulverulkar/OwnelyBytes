package com.ovenlybytes.app.data.repository

import com.ovenlybytes.app.data.local.dao.CustomerDao
import com.ovenlybytes.app.data.local.dao.InventoryDao
import com.ovenlybytes.app.data.local.dao.MenuItemDao
import com.ovenlybytes.app.data.local.dao.OrderDao
import com.ovenlybytes.app.data.local.entities.CustomerEntity
import com.ovenlybytes.app.data.local.entities.InventoryEntity
import com.ovenlybytes.app.data.local.entities.MenuItemEntity
import com.ovenlybytes.app.data.local.entities.OrderEntity
import com.ovenlybytes.app.data.local.entities.OrderItemEntity
import com.ovenlybytes.app.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.Calendar

class BakeryRepository(
    private val customerDao: CustomerDao,
    private val menuItemDao: MenuItemDao,
    private val orderDao: OrderDao,
    private val inventoryDao: InventoryDao
) {
    // Orders
    val allOrdersWithDetails: Flow<List<OrderWithCustomerAndItems>> = orderDao.getAllOrdersWithDetails()

    fun getOrderWithDetails(orderId: Long): Flow<OrderWithCustomerAndItems?> =
        orderDao.getOrderWithDetailsById(orderId)

    suspend fun getOrderWithDetailsDirect(orderId: Long): OrderWithCustomerAndItems? =
        orderDao.getOrderWithDetailsByIdDirect(orderId)

    fun getOrdersByStatus(status: OrderStatus): Flow<List<OrderWithCustomerAndItems>> =
        orderDao.getOrdersByStatus(status.name)

    fun getOrdersBySource(source: OrderSource): Flow<List<OrderWithCustomerAndItems>> =
        orderDao.getOrdersBySource(source.name)

    suspend fun createOrder(order: OrderEntity, items: List<OrderItemEntity>): Long =
        orderDao.createOrderWithItems(order, items)

    suspend fun updateOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>) =
        orderDao.updateOrderWithItems(order, items)

    suspend fun updateOrderStatus(orderId: Long, status: OrderStatus) =
        orderDao.updateOrderStatus(orderId, status.name)

    suspend fun updateOrderPayment(orderId: Long, paymentStatus: PaymentStatus, advancePaid: Double, balanceDue: Double) =
        orderDao.updateOrderPayment(orderId, paymentStatus.name, advancePaid, balanceDue)

    suspend fun deleteOrder(order: OrderEntity) =
        orderDao.deleteOrder(order)

    // Dashboard Statistics Flow
    val dashboardStats: Flow<DashboardStats> = combine(
        orderDao.getAllOrdersWithDetails(),
        inventoryDao.getLowStockCount()
    ) { orders, lowStockCount ->
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfToday = cal.timeInMillis

        var pendingCount = 0
        var bakingCount = 0
        var readyCount = 0
        var deliveredCount = 0
        var dueToday = 0
        var revenue = 0.0
        var balancePending = 0.0
        var whatsappOrders = 0
        var phoneOrders = 0

        orders.forEach { item ->
            val order = item.order
            when (order.orderStatus) {
                OrderStatus.RECEIVED.name -> pendingCount++
                OrderStatus.BAKING.name -> bakingCount++
                OrderStatus.READY.name -> readyCount++
                OrderStatus.DELIVERED.name -> deliveredCount++
            }

            if (order.orderSource == OrderSource.WHATSAPP.name) whatsappOrders++
            if (order.orderSource == OrderSource.PHONE.name) phoneOrders++

            if (order.orderStatus != OrderStatus.CANCELLED.name) {
                revenue += order.totalAmount
                balancePending += order.balanceDue
            }

            if (order.deliveryDate in startOfToday..endOfToday && order.orderStatus != OrderStatus.DELIVERED.name && order.orderStatus != OrderStatus.CANCELLED.name) {
                dueToday++
            }
        }

        DashboardStats(
            totalOrdersCount = orders.size,
            pendingOrdersCount = pendingCount,
            bakingOrdersCount = bakingCount,
            readyOrdersCount = readyCount,
            deliveredOrdersCount = deliveredCount,
            dueTodayCount = dueToday,
            totalRevenue = revenue,
            totalBalancePending = balancePending,
            lowStockCount = lowStockCount,
            whatsappOrdersCount = whatsappOrders,
            phoneOrdersCount = phoneOrders
        )
    }

    // Customers
    val allCustomers: Flow<List<CustomerEntity>> = customerDao.getAllCustomers()

    fun searchCustomers(query: String): Flow<List<CustomerEntity>> =
        customerDao.searchCustomers(query)

    suspend fun getCustomerById(id: Long): CustomerEntity? =
        customerDao.getCustomerById(id)

    fun getCustomerWithOrders(id: Long): Flow<CustomerWithOrders?> =
        customerDao.getCustomerWithOrders(id)

    suspend fun insertCustomer(customer: CustomerEntity): Long =
        customerDao.insertCustomer(customer)

    suspend fun updateCustomer(customer: CustomerEntity) =
        customerDao.updateCustomer(customer)

    suspend fun deleteCustomer(customer: CustomerEntity) =
        customerDao.deleteCustomer(customer)

    // Menu Items
    val allMenuItems: Flow<List<MenuItemEntity>> = menuItemDao.getAllMenuItems()

    fun getMenuItemsByCategory(category: MenuCategory): Flow<List<MenuItemEntity>> =
        menuItemDao.getMenuItemsByCategory(category.name)

    fun searchMenuItems(query: String): Flow<List<MenuItemEntity>> =
        menuItemDao.searchMenuItems(query)

    suspend fun insertMenuItem(item: MenuItemEntity): Long =
        menuItemDao.insertMenuItem(item)

    suspend fun updateMenuItem(item: MenuItemEntity) =
        menuItemDao.updateMenuItem(item)

    suspend fun deleteMenuItem(item: MenuItemEntity) =
        menuItemDao.deleteMenuItem(item)

    suspend fun setMenuItemAvailability(id: Long, isAvailable: Boolean) =
        menuItemDao.setAvailability(id, isAvailable)

    // Inventory
    val allInventory: Flow<List<InventoryEntity>> = inventoryDao.getAllInventory()
    val lowStockInventory: Flow<List<InventoryEntity>> = inventoryDao.getLowStockInventory()

    fun getInventoryByCategory(category: String): Flow<List<InventoryEntity>> =
        inventoryDao.getInventoryByCategory(category)

    fun searchInventory(query: String): Flow<List<InventoryEntity>> =
        inventoryDao.searchInventory(query)

    suspend fun insertInventoryItem(item: InventoryEntity): Long =
        inventoryDao.insertItem(item)

    suspend fun updateInventoryItem(item: InventoryEntity) =
        inventoryDao.updateItem(item)

    suspend fun updateInventoryQuantity(id: Long, newQuantity: Double) =
        inventoryDao.updateQuantity(id, newQuantity)

    suspend fun deleteInventoryItem(item: InventoryEntity) =
        inventoryDao.deleteItem(item)

    // Direct Methods for Backup & Restore
    suspend fun getAllCustomersDirect(): List<CustomerEntity> =
        customerDao.getAllCustomersDirect()

    suspend fun getAllMenuItemsDirect(): List<MenuItemEntity> =
        menuItemDao.getAllMenuItemsDirect()

    suspend fun getAllInventoryDirect(): List<InventoryEntity> =
        inventoryDao.getAllInventoryDirect()

    suspend fun getAllOrdersDirect(): List<OrderEntity> =
        orderDao.getAllOrdersDirect()

    suspend fun getAllOrderItemsDirect(): List<OrderItemEntity> =
        orderDao.getAllOrderItemsDirect()

    suspend fun clearAllOrdersAndTransactions() {
        orderDao.deleteAllOrderItems()
        orderDao.deleteAllOrders()
    }

    suspend fun restoreFullBackup(
        customers: List<CustomerEntity>,
        menuItems: List<MenuItemEntity>,
        inventory: List<InventoryEntity>,
        orders: List<OrderEntity>,
        orderItems: List<OrderItemEntity>
    ) {
        orderDao.deleteAllOrderItems()
        orderDao.deleteAllOrders()
        customerDao.deleteAllCustomers()
        menuItemDao.deleteAllMenuItems()
        inventoryDao.deleteAllInventory()

        customerDao.insertCustomers(customers)
        menuItemDao.insertMenuItems(menuItems)
        inventoryDao.insertItems(inventory)
        orderDao.insertOrders(orders)

        val validOrderIds = orders.map { it.id }.toSet()
        val validOrderItems = orderItems.filter { it.orderId in validOrderIds }
        orderDao.insertOrderItems(validOrderItems)
    }
}

