package com.ovenlybytes.app.model

data class DailyRevenue(
    val dateLabel: String,
    val timestamp: Long,
    val ordersCount: Int,
    val revenue: Double
)

data class MonthlyRevenue(
    val monthLabel: String,
    val ordersCount: Int,
    val revenue: Double
)

data class YearlyRevenue(
    val yearLabel: String,
    val ordersCount: Int,
    val revenue: Double
)

data class CategorySalesStat(
    val categoryName: String,
    val itemsCount: Int,
    val revenue: Double,
    val percentage: Double
)

data class FinancialReportStats(
    val totalOrdersCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val totalPendingBalance: Double = 0.0,
    val avgOrderValue: Double = 0.0,
    val collectionRatePercentage: Double = 100.0,
    val todayRevenue: Double = 0.0,
    val todayOrdersCount: Int = 0,
    val thisMonthRevenue: Double = 0.0,
    val thisMonthOrdersCount: Int = 0,
    val thisYearRevenue: Double = 0.0,
    val thisYearOrdersCount: Int = 0,
    val dailyRevenues: List<DailyRevenue> = emptyList(),
    val monthlyRevenues: List<MonthlyRevenue> = emptyList(),
    val yearlyRevenues: List<YearlyRevenue> = emptyList(),
    val categorySales: List<CategorySalesStat> = emptyList(),
    val pendingPaymentOrders: List<OrderWithCustomerAndItems> = emptyList()
)
