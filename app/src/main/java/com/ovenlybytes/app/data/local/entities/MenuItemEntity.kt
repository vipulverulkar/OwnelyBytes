package com.ovenlybytes.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String, // Matches MenuCategory enum name
    val description: String = "",
    val isEggless: Boolean = true,
    val size1Name: String = "½ KG",
    val price1: Double = 0.0,
    val size2Name: String = "1 KG",
    val price2: Double = 0.0,
    val isSeasonalRate: Boolean = false,
    val isAvailable: Boolean = true
)

