package com.ovenlybytes.app.ui.screens.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ovenlybytes.app.data.local.entities.CustomerEntity
import com.ovenlybytes.app.data.repository.BakeryRepository
import com.ovenlybytes.app.model.CustomerWithOrders
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CustomerViewModel(
    private val repository: BakeryRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val filteredCustomers: StateFlow<List<CustomerEntity>> = _searchQuery
        .debounce(200)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.allCustomers
            } else {
                repository.searchCustomers(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun addCustomer(name: String, phone: String, email: String, address: String, notes: String) {
        viewModelScope.launch {
            repository.insertCustomer(
                CustomerEntity(
                    name = name.trim(),
                    phone = phone.trim(),
                    email = email.trim(),
                    address = address.trim(),
                    notes = notes.trim()
                )
            )
        }
    }

    fun updateCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.updateCustomer(customer)
        }
    }

    fun deleteCustomer(customer: CustomerEntity) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun getCustomerWithOrders(customerId: Long): Flow<CustomerWithOrders?> {
        return repository.getCustomerWithOrders(customerId)
    }

    class Factory(private val repository: BakeryRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CustomerViewModel(repository) as T
        }
    }
}

