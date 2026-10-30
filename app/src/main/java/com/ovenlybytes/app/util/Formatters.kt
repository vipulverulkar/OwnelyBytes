package com.ovenlybytes.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object Formatters {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 0
    }

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())

    fun formatCurrency(amount: Double): String {
        return "₹${String.format(Locale.US, "%,d", amount.toLong())}"
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    fun getDaysDifference(targetTimestamp: Long): Long {
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val target = Calendar.getInstance().apply {
            timeInMillis = targetTimestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val diffMs = target - now
        return diffMs / (24 * 60 * 60 * 1000L)
    }

    fun getDeliveryBadgeText(targetTimestamp: Long): String {
        val diffDays = getDaysDifference(targetTimestamp)
        return when {
            diffDays < 0 -> "Overdue by ${-diffDays}d"
            diffDays == 0L -> "Due Today"
            diffDays == 1L -> "Due Tomorrow"
            else -> "In $diffDays days"
        }
    }
}

object PhoneHelper {
    fun callPhone(context: Context, rawPhone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$rawPhone")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Cannot open dialer: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

