package com.ovenlybytes.app.data.local.dao

import androidx.room.*
import com.ovenlybytes.app.data.local.entities.InventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InventoryDao {

    @Query("SELECT * FROM inventory ORDER BY category ASC, itemName ASC")
    fun getAllInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE category = :category ORDER BY itemName ASC")
    fun getInventoryByCategory(category: String): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory WHERE currentQuantity <= minThreshold ORDER BY currentQuantity ASC")
    fun getLowStockInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT COUNT(*) FROM inventory WHERE currentQuantity <= minThreshold")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT * FROM inventory WHERE id = :id")
    suspend fun getInventoryById(id: Long): InventoryEntity?

    @Query("SELECT * FROM inventory WHERE itemName LIKE '%' || :query || '%' ORDER BY itemName ASC")
    fun searchInventory(query: String): Flow<List<InventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: InventoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<InventoryEntity>)

    @Update
    suspend fun updateItem(item: InventoryEntity)

    @Query("UPDATE inventory SET currentQuantity = :newQuantity, lastUpdated = :timestamp WHERE id = :id")
    suspend fun updateQuantity(id: Long, newQuantity: Double, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteItem(item: InventoryEntity)

    @Query("SELECT COUNT(*) FROM inventory")
    suspend fun getInventoryCount(): Int

    @Query("SELECT * FROM inventory")
    suspend fun getAllInventoryDirect(): List<InventoryEntity>

    @Query("DELETE FROM inventory")
    suspend fun deleteAllInventory()
}

