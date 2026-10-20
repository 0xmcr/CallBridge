package com.callbridge.personal.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class PhoneValidation(
    val isValid: Boolean,
    val formatted: String,
    val e164: String,
    val message: String
)

object FormatUtils {

    /**
     * Extracts national digits (up to 10 digits) from a raw input string,
     * ignoring "+1", parentheses, spaces, and dashes.
     */
    fun extractNationalDigits(input: String): String {
        var cleaned = input.trim().replace("[^0-9]".toRegex(), "")
        // If it starts with country code 1, strip it to get the national number
        if (cleaned.startsWith("1") && cleaned.length > 10) {
            cleaned = cleaned.substring(1)
        } else if (cleaned.startsWith("1") && cleaned.length == 1) {
            cleaned = ""
        }
        return cleaned.take(10)
    }

    /**
     * Formats national digits into clean North American standard phone representation:
     * e.g. "+1 (202) 555-0143"
     */
    fun formatNanpDisplay(digits: String): String {
        val clean = digits.take(10)
        return when {
            clean.isEmpty() -> "+1"
            clean.length <= 3 -> "+1 ($clean"
            clean.length <= 6 -> "+1 (${clean.substring(0, 3)}) ${clean.substring(3)}"
            else -> "+1 (${clean.substring(0, 3)}) ${clean.substring(3, 6)}-${clean.substring(6)}"
        }
    }

    /**
     * Validates whether national digits form a legitimate North American Numbering Plan number.
     * US numbers require 10 digits where:
     * - Area code (digits 0-2) cannot start with 0 or 1
     * - Central office code (digits 3-5) cannot start with 0 or 1
     */
    fun validateNanp(digits: String): PhoneValidation {
        val clean = digits.take(10)
        val formatted = formatNanpDisplay(clean)
        val e164 = if (clean.length == 10) "+1$clean" else ""

        return when {
            clean.isEmpty() -> PhoneValidation(
                isValid = false,
                formatted = formatted,
                e164 = "",
                message = "Enter 10-digit US phone number"
            )
            clean.length < 10 -> PhoneValidation(
                isValid = false,
                formatted = formatted,
                e164 = "",
                message = "Need ${10 - clean.length} more digits"
            )
            clean[0] in listOf('0', '1') -> PhoneValidation(
                isValid = false,
                formatted = formatted,
                e164 = "",
                message = "Area code cannot start with 0 or 1"
            )
            clean[3] in listOf('0', '1') -> PhoneValidation(
                isValid = false,
                formatted = formatted,
                e164 = "",
                message = "Exchange code cannot start with 0 or 1"
            )
            else -> PhoneValidation(
                isValid = true,
                formatted = formatted,
                e164 = e164,
                message = "Valid US Number"
            )
        }
    }

    /**
     * Normalizes a user-input phone number to E.164 US format (+1XXXXXXXXXX).
     * If user enters a 10-digit number without country code, prepends "+1".
     * Strips whitespace, dashes, parentheses.
     */
    fun normalizePhoneNumber(input: String): String {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) return ""

        val cleaned = trimmed.replace("[^0-9+]".toRegex(), "")
        return when {
            cleaned.startsWith("+1") -> cleaned
            cleaned.startsWith("1") && cleaned.length == 11 -> "+$cleaned"
            cleaned.startsWith("+") -> cleaned
            else -> "+1$cleaned"
        }
    }

    /**
     * Formats duration in seconds to a human-readable string (e.g. "2m 15s", "45s", "0s").
     */
    fun formatDuration(seconds: Long?): String {
        if (seconds == null || seconds <= 0) return "0s"
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return if (minutes > 0) {
            "${minutes}m ${remainingSeconds}s"
        } else {
            "${remainingSeconds}s"
        }
    }

    /**
     * Formats epoch timestamp in milliseconds to human-readable format.
     */
    fun formatTimestamp(millis: Long): String {
        if (millis <= 0) return "Unknown"
        val now = System.currentTimeMillis()
        val diff = now - millis

        val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

        return when {
            diff < 24 * 60 * 60 * 1000 && isSameDay(now, millis) -> {
                "Today, ${timeFormat.format(Date(millis))}"
            }
            diff < 48 * 60 * 60 * 1000 && isYesterday(now, millis) -> {
                "Yesterday, ${timeFormat.format(Date(millis))}"
            }
            else -> {
                dateFormat.format(Date(millis))
            }
        }
    }

    private fun isSameDay(t1: Long, t2: Long): Boolean {
        val f = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return f.format(Date(t1)) == f.format(Date(t2))
    }

    private fun isYesterday(now: Long, target: Long): Boolean {
        val dayMillis = 24 * 60 * 60 * 1000
        val f = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        return f.format(Date(now - dayMillis)) == f.format(Date(target))
    }
}
