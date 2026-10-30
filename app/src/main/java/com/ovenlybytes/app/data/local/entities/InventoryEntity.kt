package com.ovenlybytes.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val itemName: String,
    val category: String, // INGREDIENTS, DAIRY, TOPPINGS, PACKAGING
    val currentQuantity: Double,
    val unit: String, // kg, g, l, pcs, boxes
    val minThreshold: Double,
    val costPerUnit: Double = 0.0,
    val lastUpdated: Long = System.currentTimeMillis()
)

