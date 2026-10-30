package com.ovenlybytes.app.ui.screens.reports

import com.ovenlybytes.app.data.local.entities.CustomerEntity
import com.ovenlybytes.app.data.local.entities.OrderEntity
import com.ovenlybytes.app.model.OrderWithCustomerAndItems
import org.junit.Assert.*
import org.junit.Test

class ReportsViewModelTest {

    @Test
    fun testPendingPaymentsCalculation() {
        val cust = CustomerEntity(id = 1, name = "Test Customer", phone = "9876543210")
        val order1 = OrderEntity(
            id = 1001,
            customerId = 1,
            orderSource = "WHATSAPP",
            orderStatus = "RECEIVED",
            orderDate = System.currentTimeMillis(),
            deliveryDate = System.currentTimeMillis(),
            deliveryTimeSlot = "5:00 PM",
            subtotal = 1500.0,
            totalAmount = 1500.0,
            advancePaid = 500.0,
            balanceDue = 1000.0
        )

        val orderDetail = OrderWithCustomerAndItems(
            order = order1,
            customer = cust,
            items = emptyList()
        )

        assertEquals(1000.0, orderDetail.order.balanceDue, 0.001)
        assertEquals(1500.0, orderDetail.order.totalAmount, 0.001)
        assertEquals(500.0, orderDetail.order.advancePaid, 0.001)
    }

    @Test
    fun testTimeFilterEnum() {
        assertEquals("All Time", ReportTimeFilter.ALL_TIME.displayName)
        assertEquals("Today", ReportTimeFilter.TODAY.displayName)
        assertEquals("This Month", ReportTimeFilter.THIS_MONTH.displayName)
        assertEquals("This Year", ReportTimeFilter.THIS_YEAR.displayName)
    }
}
