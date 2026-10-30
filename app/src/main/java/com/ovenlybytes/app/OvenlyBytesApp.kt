package com.ovenlybytes.app

import android.app.Application
import com.ovenlybytes.app.data.local.OvenlyDatabase
import com.ovenlybytes.app.data.repository.BakeryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class OvenlyBytesApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy {
        OvenlyDatabase.getDatabase(this, applicationScope)
    }

    val repository by lazy {
        BakeryRepository(
            customerDao = database.customerDao(),
            menuItemDao = database.menuItemDao(),
            orderDao = database.orderDao(),
            inventoryDao = database.inventoryDao()
        )
    }
}

