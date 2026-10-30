package com.ovenlybytes.app.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ovenlybytes.app.ui.components.OrderChannelBadge
import com.ovenlybytes.app.ui.components.OvenlyTopAppBar
import com.ovenlybytes.app.ui.theme.*
import com.ovenlybytes.app.util.Formatters
import com.ovenlybytes.app.util.WhatsAppHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val stats by viewModel.reportStats.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Daily, 1: Monthly, 2: Yearly
    var pendingPage by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            OvenlyTopAppBar(
                title = "Reports & Financials",
                subtitle = "Revenue, orders & pending balances",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // Time Filter Bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ReportTimeFilter.entries.forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { viewModel.setTimeFilter(filter) },
                            label = { Text(filter.displayName) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            HorizontalDivider(color = WarmBorder)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // SECTION 1: PRIMARY KPI CARDS
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Total Orders Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Total Orders", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${stats.totalOrdersCount}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                                )
                                Text(selectedFilter.displayName, fontSize = 11.sp, color = TextMuted)
                            }
                        }

                        // Total Revenue Card
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Revenue Made", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(StatusDelivered.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.Payments, contentDescription = null, tint = StatusDelivered, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = Formatters.formatCurrency(stats.totalRevenue),
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold, color = StatusDelivered)
                                )
                                Text(selectedFilter.displayName, fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }

                // Pending Balance Card
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = if (stats.totalPendingBalance > 0) Color(0xFFFFF3E0) else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(BalanceDueColor.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Pending, contentDescription = null, tint = BalanceDueColor, modifier = Modifier.size(22.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Pending Payments Due", style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary, fontWeight = FontWeight.SemiBold))
                                    Text(
                                        text = Formatters.formatCurrency(stats.totalPendingBalance),
                                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = BalanceDueColor)
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = BalanceDueColor,
                                contentColor = Color.White
                            ) {
                                Text(
                                    text = "${stats.pendingPaymentOrders.size} Orders",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // SECTION 2: QUICK PERIOD COMPARISON SUMMARY
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Revenue & Sales Comparison",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Today
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("Today", fontSize = 11.sp, color = TextSecondary)
                                    Text(Formatters.formatCurrency(stats.todayRevenue), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = StatusReady)
                                    Text("${stats.todayOrdersCount} orders", fontSize = 10.sp, color = TextMuted)
                                }
                                VerticalDivider(modifier = Modifier.height(36.dp), color = WarmBorder)
                                // Month
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("This Month", fontSize = 11.sp, color = TextSecondary)
                                    Text(Formatters.formatCurrency(stats.thisMonthRevenue), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
                                    Text("${stats.thisMonthOrdersCount} orders", fontSize = 10.sp, color = TextMuted)
                                }
                                VerticalDivider(modifier = Modifier.height(36.dp), color = WarmBorder)
                                // Year
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("This Year", fontSize = 11.sp, color = TextSecondary)
                                    Text(Formatters.formatCurrency(stats.thisYearRevenue), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SecondaryRose)
                                    Text("${stats.thisYearOrdersCount} orders", fontSize = 10.sp, color = TextMuted)
                                }
                            }
                        }
                    }
                }

                // SECTION 3: REVENUE BREAKDOWN BY DAY / MONTH / YEAR
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Revenue Breakdown",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                )
                                TabRow(
                                    selectedTabIndex = selectedTab,
                                    modifier = Modifier.width(220.dp),
                                    containerColor = Color.Transparent,
                                    divider = {}
                                ) {
                                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                                        Text("Daily", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                                    }
                                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                                        Text("Monthly", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                                    }
                                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }) {
                                        Text("Yearly", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            when (selectedTab) {
                                0 -> { // Daily
                                    if (stats.dailyRevenues.isEmpty()) {
                                        Text("No sales records found", fontSize = 12.sp, color = TextMuted)
                                    } else {
                                        val maxDaily = stats.dailyRevenues.maxOfOrNull { it.revenue } ?: 1.0
                                        stats.dailyRevenues.forEach { item ->
                                            val progress = if (maxDaily > 0) (item.revenue / maxDaily).toFloat() else 0f
                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("${item.dateLabel} (${item.ordersCount} orders)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                    Text(Formatters.formatCurrency(item.revenue), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                LinearProgressIndicator(
                                                    progress = { progress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = MaterialTheme.colorScheme.primary,
                                                    trackColor = WarmBorder
                                                )
                                            }
                                        }
                                    }
                                }
                                1 -> { // Monthly
                                    if (stats.monthlyRevenues.isEmpty()) {
                                        Text("No monthly records found", fontSize = 12.sp, color = TextMuted)
                                    } else {
                                        val maxMonthly = stats.monthlyRevenues.maxOfOrNull { it.revenue } ?: 1.0
                                        stats.monthlyRevenues.forEach { item ->
                                            val progress = if (maxMonthly > 0) (item.revenue / maxMonthly).toFloat() else 0f
                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("${item.monthLabel} (${item.ordersCount} orders)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                    Text(Formatters.formatCurrency(item.revenue), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                LinearProgressIndicator(
                                                    progress = { progress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = StatusReady,
                                                    trackColor = WarmBorder
                                                )
                                            }
                                        }
                                    }
                                }
                                2 -> { // Yearly
                                    if (stats.yearlyRevenues.isEmpty()) {
                                        Text("No yearly records found", fontSize = 12.sp, color = TextMuted)
                                    } else {
                                        val maxYearly = stats.yearlyRevenues.maxOfOrNull { it.revenue } ?: 1.0
                                        stats.yearlyRevenues.forEach { item ->
                                            val progress = if (maxYearly > 0) (item.revenue / maxYearly).toFloat() else 0f
                                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text("Year ${item.yearLabel} (${item.ordersCount} orders)", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                                    Text(Formatters.formatCurrency(item.revenue), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                LinearProgressIndicator(
                                                    progress = { progress },
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(6.dp)
                                                        .clip(RoundedCornerShape(3.dp)),
                                                    color = SecondaryRose,
                                                    trackColor = WarmBorder
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // SECTION 4: PENDING PAYMENTS DIRECTORY & 1-TAP WHATSAPP REMINDER
                item {
                    Text(
                        text = "Outstanding Payment Orders (${stats.pendingPaymentOrders.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary)
                    )
                }

                if (stats.pendingPaymentOrders.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusReady, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("All Payments Collected!", fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("No pending balance due on active orders.", fontSize = 12.sp, color = TextSecondary)
                            }
                        }
                    }
                } else {
                    val pendingList = stats.pendingPaymentOrders
                    val pageSize = 4
                    val totalPages = maxOf(1, (pendingList.size + pageSize - 1) / pageSize)
                    val safePage = pendingPage.coerceIn(0, totalPages - 1)
                    val startIndex = safePage * pageSize
                    val endIndex = minOf(startIndex + pageSize, pendingList.size)
                    val paginatedPending = pendingList.subList(startIndex, endIndex)

                    items(paginatedPending) { orderDetail ->
                        val order = orderDetail.order
                        val customer = orderDetail.customer

                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BalanceDueColor.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text("Order #${order.id}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text(customer?.name ?: "Customer", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                    }
                                    OrderChannelBadge(sourceName = order.orderSource)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Total: ${Formatters.formatCurrency(order.totalAmount)}", fontSize = 12.sp, color = TextSecondary)
                                        Text("Paid: ${Formatters.formatCurrency(order.advancePaid)}", fontSize = 12.sp, color = AdvancePaidColor)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text("Balance Due", fontSize = 11.sp, color = BalanceDueColor, fontWeight = FontWeight.SemiBold)
                                        Text(Formatters.formatCurrency(order.balanceDue), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BalanceDueColor)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // WhatsApp Payment Reminder Button
                                    Button(
                                        onClick = {
                                            val phone = customer?.phone ?: ""
                                            val reminderMsg = "Hi ${customer?.name ?: "Customer"}! Gentle payment reminder from Ovenly Bytes for Order #${order.id}. Total: ₹${order.totalAmount.toInt()}, Paid: ₹${order.advancePaid.toInt()}. Balance Due: ₹${order.balanceDue.toInt()}. Thank you! ❤️"
                                            WhatsAppHelper.openWhatsApp(context, phone, reminderMsg)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreenDark),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("WhatsApp Reminder", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }

                                    // View Order Details Button
                                    OutlinedButton(
                                        onClick = { onNavigateToOrderDetail(order.id) },
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("View Order", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Pending Orders Pagination Bar
                    if (totalPages > 1) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Showing ${startIndex + 1}–$endIndex of ${pendingList.size} pending orders",
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        OutlinedIconButton(
                                            onClick = { if (safePage > 0) pendingPage = safePage - 1 },
                                            enabled = safePage > 0,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page", modifier = Modifier.size(18.dp))
                                        }

                                        Text(
                                            "${safePage + 1}/$totalPages",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )

                                        OutlinedIconButton(
                                            onClick = { if (safePage < totalPages - 1) pendingPage = safePage + 1 },
                                            enabled = safePage < totalPages - 1,
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Page", modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}
