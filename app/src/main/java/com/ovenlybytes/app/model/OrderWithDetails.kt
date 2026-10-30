package com.ovenlybytes.app.model

import androidx.room.Embedded
import androidx.room.Relation
import com.ovenlybytes.app.data.local.entities.CustomerEntity
import com.ovenlybytes.app.data.local.entities.OrderEntity
import com.ovenlybytes.app.data.local.entities.OrderItemEntity

data class OrderWithCustomerAndItems(
    @Embedded
    val order: OrderEntity,

    @Relation(
        parentColumn = "customerId",
        entityColumn = "id"
    )
    val customer: CustomerEntity?,

    @Relation(
        parentColumn = "id",
        entityColumn = "orderId"
    )
    val items: List<OrderItemEntity>
)

data class CustomerWithOrders(
    @Embedded
    val customer: CustomerEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "customerId"
    )
    val orders: List<OrderEntity>
)

data class DashboardStats(
    val totalOrdersCount: Int = 0,
    val pendingOrdersCount: Int = 0,
    val bakingOrdersCount: Int = 0,
    val readyOrdersCount: Int = 0,
    val deliveredOrdersCount: Int = 0,
    val dueTodayCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalBalancePending: Double = 0.0,
    val lowStockCount: Int = 0,
    val whatsappOrdersCount: Int = 0,
    val phoneOrdersCount: Int = 0
)

