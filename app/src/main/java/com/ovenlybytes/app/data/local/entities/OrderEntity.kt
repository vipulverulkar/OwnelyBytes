package com.ovenlybytes.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["customerId"]),
        Index(value = ["orderStatus"]),
        Index(value = ["deliveryDate"])
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val orderSource: String, // WHATSAPP, PHONE, WALK_IN
    val orderStatus: String, // RECEIVED, BAKING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    val orderDate: Long = System.currentTimeMillis(),
    val deliveryDate: Long,
    val deliveryTimeSlot: String = "Anytime",
    val cakeMessage: String = "",
    val customizationNotes: String = "",
    val customizationCharge: Double = 0.0,
    val discount: Double = 0.0,
    val subtotal: Double = 0.0,
    val totalAmount: Double = 0.0,
    val advancePaid: Double = 0.0,
    val balanceDue: Double = 0.0,
    val paymentMode: String = "UPI",
    val paymentStatus: String = "PENDING", // PENDING, PARTIAL, COMPLETED
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

