package com.ovenlybytes.app.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.ovenlybytes.app.model.OrderWithCustomerAndItems
import java.net.URLEncoder

object WhatsAppHelper {

    fun formatOrderReceiptMessage(orderWithDetails: OrderWithCustomerAndItems): String {
        val order = orderWithDetails.order
        val customer = orderWithDetails.customer
        val items = orderWithDetails.items

        val sb = StringBuilder()
        sb.append("✨ *OVENLY BITES - ORDER CONFIRMATION* ✨\n")
        sb.append("_Freshly Baked With Love ❤️ (100% Eggless)_\n")
        sb.append("────────────────────────\n")
        sb.append("*Order ID:* #OB-${order.id}\n")
        if (customer != null) {
            sb.append("*Customer:* ${customer.name}\n")
            if (customer.address.isNotBlank()) {
                sb.append("*Delivery Address:* ${customer.address}\n")
            }
        }
        sb.append("*Delivery Date:* ${Formatters.formatDate(order.deliveryDate)} (${order.deliveryTimeSlot})\n")
        sb.append("────────────────────────\n")
        sb.append("*Items Ordered:*\n")

        items.forEach { item ->
            sb.append("• ${item.quantity}x ${item.itemName} (${item.variantName}) - ₹${item.itemTotal.toInt()}\n")
        }

        if (order.cakeMessage.isNotBlank()) {
            sb.append("\n*Message on Cake:* \"${order.cakeMessage}\"\n")
        }

        if (order.customizationNotes.isNotBlank()) {
            sb.append("*Customization:* ${order.customizationNotes}")
            if (order.customizationCharge > 0) {
                sb.append(" (+₹${order.customizationCharge.toInt()})")
            }
            sb.append("\n")
        }

        sb.append("────────────────────────\n")
        sb.append("*Subtotal:* ₹${order.subtotal.toInt()}\n")
        if (order.customizationCharge > 0) {
            sb.append("*Customization:* ₹${order.customizationCharge.toInt()}\n")
        }
        if (order.discount > 0) {
            sb.append("*Discount:* -₹${order.discount.toInt()}\n")
        }
        sb.append("*Total Amount:* ₹${order.totalAmount.toInt()}\n")
        sb.append("*Advance Paid:* ₹${order.advancePaid.toInt()}\n")
        sb.append("*Balance Due:* ₹${order.balanceDue.toInt()}\n")
        sb.append("*Payment Status:* ${order.paymentStatus}\n")
        sb.append("────────────────────────\n")
        sb.append("Thank you for ordering with Ovenly Bites! 🍰\n")
        sb.append("_100% Eggless • Premium Quality_")

        return sb.toString()
    }

    fun formatBakingProgressMessage(orderWithDetails: OrderWithCustomerAndItems): String {
        val order = orderWithDetails.order
        val customerName = orderWithDetails.customer?.name ?: "Customer"
        return """
            🎂 *OVENLY BITES UPDATE* 🎂
            Hi $customerName!
            Good news — your order *#OB-${order.id}* is now being baked fresh with love in our kitchen! ❤️
            It is 100% eggless and crafted with premium ingredients.
            We'll notify you once it's beautifully packed and ready! 🧁
        """.trimIndent()
    }

    fun formatOrderReadyMessage(orderWithDetails: OrderWithCustomerAndItems): String {
        val order = orderWithDetails.order
        val customerName = orderWithDetails.customer?.name ?: "Customer"
        return """
            🎉 *YOUR OVENLY BITES ORDER IS READY!* 🎉
            Hi $customerName!
            Your fresh, delicious order *#OB-${order.id}* is ready! 🍰✨
            Slot: ${order.deliveryTimeSlot}
            ${if (order.balanceDue > 0) "Pending balance: ₹" + order.balanceDue.toInt() else "Payment completed in full."}
            Thank you for celebrating with us!
        """.trimIndent()
    }

    fun openWhatsApp(context: Context, rawPhone: String, message: String) {
        // Clean phone number (strip spaces, dashes, ensure country code)
        var cleanPhone = rawPhone.replace(Regex("[^0-9]"), "")
        if (cleanPhone.length == 10) {
            cleanPhone = "${AppConstants.DEFAULT_COUNTRY_CODE}$cleanPhone" // Default India country code
        }

        try {
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val uri = Uri.parse("https://wa.me/$cleanPhone?text=$encodedMessage")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }
}

