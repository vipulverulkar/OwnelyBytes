package com.ovenlybytes.app

import android.app.Application
import com.ovenlybytes.app.data.local.OvenlyDatabase
import com.ovenlybytes.app.data.repository.BakeryRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class OvenlyBytesApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        try {
            System.loadLibrary("sqlcipher")
        } catch (_: Throwable) {}
    }

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

    val shopPreferences by lazy {
        com.ovenlybytes.app.util.ShopPreferences(this)
    }
}

