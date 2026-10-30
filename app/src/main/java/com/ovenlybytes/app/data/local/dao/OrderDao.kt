package com.ovenlybytes.app.data.local.dao

import androidx.room.*
import com.ovenlybytes.app.data.local.entities.OrderEntity
import com.ovenlybytes.app.data.local.entities.OrderItemEntity
import com.ovenlybytes.app.model.OrderWithCustomerAndItems
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Transaction
    @Query("SELECT * FROM orders ORDER BY deliveryDate ASC, id DESC")
    fun getAllOrdersWithDetails(): Flow<List<OrderWithCustomerAndItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    fun getOrderWithDetailsById(orderId: Long): Flow<OrderWithCustomerAndItems?>

    @Transaction
    @Query("SELECT * FROM orders WHERE id = :orderId")
    suspend fun getOrderWithDetailsByIdDirect(orderId: Long): OrderWithCustomerAndItems?

    @Transaction
    @Query("SELECT * FROM orders WHERE orderStatus = :status ORDER BY deliveryDate ASC")
    fun getOrdersByStatus(status: String): Flow<List<OrderWithCustomerAndItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE orderSource = :source ORDER BY deliveryDate ASC")
    fun getOrdersBySource(source: String): Flow<List<OrderWithCustomerAndItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE deliveryDate >= :startOfDay AND deliveryDate <= :endOfDay ORDER BY deliveryDate ASC")
    fun getOrdersDueBetween(startOfDay: Long, endOfDay: Long): Flow<List<OrderWithCustomerAndItems>>

    @Transaction
    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY deliveryDate DESC")
    fun getOrdersForCustomer(customerId: Long): Flow<List<OrderWithCustomerAndItems>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderItems(items: List<OrderItemEntity>)

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderStatus = :newStatus WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, newStatus: String)

    @Query("UPDATE orders SET paymentStatus = :newPaymentStatus, advancePaid = :advancePaid, balanceDue = :balanceDue WHERE id = :orderId")
    suspend fun updateOrderPayment(orderId: Long, newPaymentStatus: String, advancePaid: Double, balanceDue: Double)

    @Delete
    suspend fun deleteOrder(order: OrderEntity)

    @Transaction
    suspend fun createOrderWithItems(order: OrderEntity, items: List<OrderItemEntity>): Long {
        val orderId = insertOrder(order)
        val itemsWithOrderId = items.map { it.copy(orderId = orderId) }
        insertOrderItems(itemsWithOrderId)
        return orderId
    }
}

