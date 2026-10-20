package com.callbridge.personal.ui.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {

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
