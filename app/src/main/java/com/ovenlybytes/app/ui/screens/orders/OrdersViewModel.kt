package com.ovenlybytes.app.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ovenlybytes.app.data.local.entities.CustomerEntity
import com.ovenlybytes.app.data.local.entities.MenuItemEntity
import com.ovenlybytes.app.data.local.entities.OrderEntity
import com.ovenlybytes.app.data.local.entities.OrderItemEntity
import com.ovenlybytes.app.data.repository.BakeryRepository
import com.ovenlybytes.app.model.OrderStatus
import com.ovenlybytes.app.model.OrderWithCustomerAndItems
import com.ovenlybytes.app.model.PaymentStatus
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class OrdersViewModel(
    private val repository: BakeryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedChannelFilter = MutableStateFlow("ALL") // ALL, WHATSAPP, PHONE, WALK_IN
    val selectedChannelFilter: StateFlow<String> = _selectedChannelFilter.asStateFlow()

    private val _selectedStatusFilter = MutableStateFlow("ALL") // ALL, RECEIVED, BAKING, READY, DELIVERED
    val selectedStatusFilter: StateFlow<String> = _selectedStatusFilter.asStateFlow()

    val customers: StateFlow<List<CustomerEntity>> = repository.allCustomers.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val menuItems: StateFlow<List<MenuItemEntity>> = repository.allMenuItems.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val filteredOrders: StateFlow<List<OrderWithCustomerAndItems>> = combine(
        repository.allOrdersWithDetails,
        _searchQuery,
        _selectedChannelFilter,
        _selectedStatusFilter
    ) { orders, query, channel, status ->
        orders.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.customer?.name?.contains(query, ignoreCase = true) == true ||
                    item.customer?.phone?.contains(query, ignoreCase = true) == true ||
                    item.order.id.toString().contains(query) ||
                    item.items.any { it.itemName.contains(query, ignoreCase = true) }

            val matchesChannel = channel == "ALL" || item.order.orderSource.equals(channel, ignoreCase = true)
            val matchesStatus = status == "ALL" || item.order.orderStatus.equals(status, ignoreCase = true)

            matchesQuery && matchesChannel && matchesStatus
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onChannelFilterChanged(channel: String) {
        _selectedChannelFilter.value = channel
    }

    fun onStatusFilterChanged(status: String) {
        _selectedStatusFilter.value = status
    }

    fun updateOrderStatus(orderId: Long, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
        }
    }

    fun markOrderPaid(orderId: Long, totalAmount: Double) {
        viewModelScope.launch {
            repository.updateOrderPayment(
                orderId = orderId,
                paymentStatus = PaymentStatus.COMPLETED,
                advancePaid = totalAmount,
                balanceDue = 0.0
            )
        }
    }

    fun deleteOrder(order: OrderEntity) {
        viewModelScope.launch {
            repository.deleteOrder(order)
        }
    }

    suspend fun createOrder(
        customer: CustomerEntity,
        isNewCustomer: Boolean,
        order: OrderEntity,
        items: List<OrderItemEntity>
    ): Long {
        val finalCustomerId = if (isNewCustomer || customer.id == 0L) {
            repository.insertCustomer(customer)
        } else {
            customer.id
        }
        val orderToInsert = order.copy(customerId = finalCustomerId)
        return repository.createOrder(orderToInsert, items)
    }

    fun getOrderDetails(orderId: Long): Flow<OrderWithCustomerAndItems?> {
        return repository.getOrderWithDetails(orderId)
    }

    class Factory(private val repository: BakeryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return OrdersViewModel(repository) as T
        }
    }
}

