package com.ovenlybytes.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class PortionVariant(
    val name: String,
    val price: Double
)

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
    val size3Name: String = "2 KG",
    val price3: Double = 0.0,
    val customVariantsJson: String = "",
    val isSeasonalRate: Boolean = false,
    val isAvailable: Boolean = true
)

fun MenuItemEntity.getAllVariants(): List<PortionVariant> {
    val list = mutableListOf<PortionVariant>()
    if (customVariantsJson.isNotBlank()) {
        try {
            val jsonArray = JSONArray(customVariantsJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val vName = obj.optString("name", "").trim()
                val vPrice = obj.optDouble("price", 0.0)
                if (vName.isNotBlank() && vPrice > 0) {
                    list.add(PortionVariant(vName, vPrice))
                }
            }
        } catch (_: Exception) {}
    }
    if (list.isEmpty()) {
        if (price1 > 0 || size1Name.isNotBlank()) list.add(PortionVariant(size1Name, price1))
        if (price2 > 0 || size2Name.isNotBlank()) list.add(PortionVariant(size2Name, price2))
        if (price3 > 0 || size3Name.isNotBlank()) list.add(PortionVariant(size3Name, price3))
    }
    return list.distinctBy { it.name }
}

fun createCustomVariantsJson(variants: List<PortionVariant>): String {
    val jsonArray = JSONArray()
    variants.filter { it.name.isNotBlank() && it.price > 0 }.forEach { v ->
        jsonArray.put(JSONObject().apply {
            put("name", v.name.trim())
            put("price", v.price)
        })
    }
    return jsonArray.toString()
}

