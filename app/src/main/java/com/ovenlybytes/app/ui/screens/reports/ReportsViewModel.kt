package com.ovenlybytes.app.ui.screens.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ovenlybytes.app.data.repository.BakeryRepository
import com.ovenlybytes.app.model.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

enum class ReportTimeFilter(val displayName: String) {
    ALL_TIME("All Time"),
    TODAY("Today"),
    THIS_MONTH("This Month"),
    THIS_YEAR("This Year")
}

class ReportsViewModel(
    private val repository: BakeryRepository
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow(ReportTimeFilter.ALL_TIME)
    val selectedFilter: StateFlow<ReportTimeFilter> = _selectedFilter.asStateFlow()

    val reportStats: StateFlow<FinancialReportStats> = combine(
        repository.allOrdersWithDetails,
        _selectedFilter
    ) { orders, filter ->
        calculateStats(orders, filter)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = FinancialReportStats()
    )

    fun setTimeFilter(filter: ReportTimeFilter) {
        _selectedFilter.value = filter
    }

    private fun calculateStats(
        allOrders: List<OrderWithCustomerAndItems>,
        filter: ReportTimeFilter
    ): FinancialReportStats {
        val cal = Calendar.getInstance()

        // Today start & end
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfToday = cal.timeInMillis

        // Month start & end
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfMonth = cal.timeInMillis

        // Year start & end
        cal.set(Calendar.DAY_OF_YEAR, 1)
        val startOfYear = cal.timeInMillis

        // Filter orders by time period if selected
        val activeOrders = allOrders.filter { it.order.orderStatus != OrderStatus.CANCELLED.name }

        val filteredOrders = when (filter) {
            ReportTimeFilter.TODAY -> activeOrders.filter { it.order.deliveryDate in startOfToday..endOfToday }
            ReportTimeFilter.THIS_MONTH -> activeOrders.filter { it.order.deliveryDate >= startOfMonth }
            ReportTimeFilter.THIS_YEAR -> activeOrders.filter { it.order.deliveryDate >= startOfYear }
            ReportTimeFilter.ALL_TIME -> activeOrders
        }

        var totalRev = 0.0
        var totalPending = 0.0
        var todayRev = 0.0
        var todayCount = 0
        var monthRev = 0.0
        var monthCount = 0
        var yearRev = 0.0
        var yearCount = 0

        val pendingOrders = mutableListOf<OrderWithCustomerAndItems>()

        val dayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val dailyMap = mutableMapOf<String, Pair<Int, Double>>()
        val monthlyMap = mutableMapOf<String, Pair<Int, Double>>()
        val yearlyMap = mutableMapOf<String, Pair<Int, Double>>()

        activeOrders.forEach { item ->
            val order = item.order
            val dateMs = order.deliveryDate
            val dayKey = dayFormat.format(Date(dateMs))
            val monthKey = monthFormat.format(Date(dateMs))
            val yearKey = yearFormat.format(Date(dateMs))

            // Global Metrics
            if (dateMs in startOfToday..endOfToday) {
                todayRev += order.totalAmount
                todayCount++
            }
            if (dateMs >= startOfMonth) {
                monthRev += order.totalAmount
                monthCount++
            }
            if (dateMs >= startOfYear) {
                yearRev += order.totalAmount
                yearCount++
            }

            if (order.balanceDue > 0.0) {
                totalPending += order.balanceDue
                pendingOrders.add(item)
            }

            // Daily Revenue Map
            val currentDay = dailyMap.getOrDefault(dayKey, Pair(0, 0.0))
            dailyMap[dayKey] = Pair(currentDay.first + 1, currentDay.second + order.totalAmount)

            // Monthly Revenue Map
            val currentMonth = monthlyMap.getOrDefault(monthKey, Pair(0, 0.0))
            monthlyMap[monthKey] = Pair(currentMonth.first + 1, currentMonth.second + order.totalAmount)

            // Yearly Revenue Map
            val currentYear = yearlyMap.getOrDefault(yearKey, Pair(0, 0.0))
            yearlyMap[yearKey] = Pair(currentYear.first + 1, currentYear.second + order.totalAmount)
        }

        val categoryMap = mutableMapOf<String, Pair<Int, Double>>()

        filteredOrders.forEach { item ->
            totalRev += item.order.totalAmount
            item.items.forEach { oi ->
                val cat = if (oi.category.isNotBlank()) oi.category else "CUSTOM"
                val curr = categoryMap.getOrDefault(cat, Pair(0, 0.0))
                categoryMap[cat] = Pair(curr.first + oi.quantity, curr.second + oi.itemTotal)
            }
        }

        val totalBilled = totalRev + totalPending
        val collectionRate = if (totalBilled > 0) ((totalRev / totalBilled) * 100.0) else 100.0
        val avgOrderVal = if (filteredOrders.isNotEmpty()) totalRev / filteredOrders.size else 0.0

        val categoryList = categoryMap.map { (catKey, pair) ->
            val catDisplayName = runCatching { MenuCategory.valueOf(catKey).displayName }.getOrDefault(catKey)
            val pct = if (totalRev > 0) ((pair.second / totalRev) * 100.0) else 0.0
            CategorySalesStat(
                categoryName = catDisplayName,
                itemsCount = pair.first,
                revenue = pair.second,
                percentage = pct
            )
        }.sortedByDescending { it.revenue }

        val dailyList = dailyMap.entries.map { (date, pair) ->
            DailyRevenue(dateLabel = date, timestamp = 0L, ordersCount = pair.first, revenue = pair.second)
        }.reversed().take(14)

        val monthlyList = monthlyMap.entries.map { (month, pair) ->
            MonthlyRevenue(monthLabel = month, ordersCount = pair.first, revenue = pair.second)
        }.reversed()

        val yearlyList = yearlyMap.entries.map { (year, pair) ->
            YearlyRevenue(yearLabel = year, ordersCount = pair.first, revenue = pair.second)
        }.reversed()

        return FinancialReportStats(
            totalOrdersCount = filteredOrders.size,
            totalRevenue = totalRev,
            totalPendingBalance = totalPending,
            avgOrderValue = avgOrderVal,
            collectionRatePercentage = collectionRate,
            todayRevenue = todayRev,
            todayOrdersCount = todayCount,
            thisMonthRevenue = monthRev,
            thisMonthOrdersCount = monthCount,
            thisYearRevenue = yearRev,
            thisYearOrdersCount = yearCount,
            dailyRevenues = dailyList,
            monthlyRevenues = monthlyList,
            yearlyRevenues = yearlyList,
            categorySales = categoryList,
            pendingPaymentOrders = pendingOrders.sortedByDescending { it.order.balanceDue }
        )
    }

    class Factory(
        private val repository: BakeryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ReportsViewModel::class.java)) {
                return ReportsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
