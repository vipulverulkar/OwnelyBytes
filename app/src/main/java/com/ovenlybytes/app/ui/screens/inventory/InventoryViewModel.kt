package com.ovenlybytes.app.ui.screens.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ovenlybytes.app.data.local.entities.InventoryEntity
import com.ovenlybytes.app.data.repository.BakeryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class InventoryViewModel(
    private val repository: BakeryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredInventory: StateFlow<List<InventoryEntity>> = combine(
        repository.allInventory,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            val matchesQuery = query.isBlank() || item.itemName.contains(query, ignoreCase = true)
            val matchesCategory = category == "ALL" || item.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onCategoryChanged(category: String) {
        _selectedCategory.value = category
    }

    fun restock(id: Long, currentQty: Double, addedQty: Double) {
        viewModelScope.launch {
            val newQty = (currentQty + addedQty).coerceAtLeast(0.0)
            repository.updateInventoryQuantity(id, newQty)
        }
    }

    fun addInventoryItem(name: String, category: String, qty: Double, unit: String, threshold: Double, cost: Double) {
        viewModelScope.launch {
            repository.insertInventoryItem(
                InventoryEntity(
                    itemName = name.trim(),
                    category = category,
                    currentQuantity = qty,
                    unit = unit.trim(),
                    minThreshold = threshold,
                    costPerUnit = cost
                )
            )
        }
    }

    fun updateInventoryItem(item: InventoryEntity) {
        viewModelScope.launch {
            repository.updateInventoryItem(item)
        }
    }

    fun deleteInventoryItem(item: InventoryEntity) {
        viewModelScope.launch {
            repository.deleteInventoryItem(item)
        }
    }

    class Factory(private val repository: BakeryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InventoryViewModel(repository) as T
        }
    }
}

