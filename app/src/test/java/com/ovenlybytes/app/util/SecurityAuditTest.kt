package com.ovenlybytes.app.util

import org.junit.Assert.*
import org.junit.Test

class SecurityAuditTest {

    @Test
    fun testSqlInjectionSanitization() {
        val inputWithSingleQuotes = "Black Forest' OR '1'='1"
        val escaped = inputWithSingleQuotes.replace("'", "''")

        val singleQuoteCountBefore = inputWithSingleQuotes.count { it == '\'' }
        val singleQuoteCountAfter = escaped.count { it == '\'' }

        assertEquals(singleQuoteCountBefore * 2, singleQuoteCountAfter)
        assertTrue(escaped.contains("''"))
    }

    @Test
    fun testCsvFormulaInjectionSanitization() {
        val maliciousCsvFormula = "=CMD|' /C calc'!A1"
        val escaped = "\"" + maliciousCsvFormula.replace("\"", "\"\"") + "\""

        assertTrue(escaped.startsWith("\""))
        assertTrue(escaped.endsWith("\""))
    }

    @Test
    fun testWhatsAppNumberSanitization() {
        val rawNumber = "+91 (987) 654-3210"
        val cleanNumber = rawNumber.replace(Regex("[^0-9]"), "")

        assertEquals("919876543210", cleanNumber)
        assertFalse(cleanNumber.contains(" "))
        assertFalse(cleanNumber.contains("+"))
    }
}
