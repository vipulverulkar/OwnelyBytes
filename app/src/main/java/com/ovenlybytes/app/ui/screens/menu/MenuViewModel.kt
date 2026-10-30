package com.ovenlybytes.app.ui.screens.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ovenlybytes.app.data.local.entities.MenuItemEntity
import com.ovenlybytes.app.data.repository.BakeryRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MenuViewModel(
    private val repository: BakeryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    val filteredMenuItems: StateFlow<List<MenuItemEntity>> = combine(
        repository.allMenuItems,
        _searchQuery,
        _selectedCategory
    ) { items, query, category ->
        items.filter { item ->
            val matchesQuery = query.isBlank() ||
                    item.name.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)

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

    fun toggleAvailability(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.setMenuItemAvailability(item.id, !item.isAvailable)
        }
    }

    fun updateMenuItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.updateMenuItem(item)
        }
    }

    fun addMenuItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.insertMenuItem(item)
        }
    }

    fun deleteMenuItem(item: MenuItemEntity) {
        viewModelScope.launch {
            repository.deleteMenuItem(item)
        }
    }

    class Factory(private val repository: BakeryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MenuViewModel(repository) as T
        }
    }
}

