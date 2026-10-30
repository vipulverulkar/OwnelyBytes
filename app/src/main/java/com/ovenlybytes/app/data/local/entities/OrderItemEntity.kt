package com.ovenlybytes.app.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_items",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["orderId"]),
        Index(value = ["menuItemId"])
    ]
)
data class OrderItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val menuItemId: Long? = null,
    val itemName: String,
    val category: String = "",
    val variantName: String, // e.g. "½ KG", "1 KG", "250 g", "500 g"
    val unitPrice: Double,
    val quantity: Int = 1,
    val itemTotal: Double
)

