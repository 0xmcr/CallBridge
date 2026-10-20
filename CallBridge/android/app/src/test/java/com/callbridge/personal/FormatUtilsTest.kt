package com.callbridge.personal

import com.callbridge.personal.ui.util.FormatUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun normalizePhoneNumber_cleansFormattingAndAddsPlusOne() {
        // Standard 10-digit number
        assertThat(FormatUtils.normalizePhoneNumber("2025550143")).isEqualTo("+12025550143")

        // US formatted with parenthesis and spaces
        assertThat(FormatUtils.normalizePhoneNumber("(202) 555-0143")).isEqualTo("+12025550143")

        // 11 digits starting with 1
        assertThat(FormatUtils.normalizePhoneNumber("12025550143")).isEqualTo("+12025550143")

        // Already normalized E.164
        assertThat(FormatUtils.normalizePhoneNumber("+12025550143")).isEqualTo("+12025550143")

        // International format starting with +
        assertThat(FormatUtils.normalizePhoneNumber("+447911123456")).isEqualTo("+447911123456")

        // Empty input
        assertThat(FormatUtils.normalizePhoneNumber("")).isEmpty()
    }

    @Test
    fun formatDuration_handlesNullZeroAndVariousSeconds() {
        assertThat(FormatUtils.formatDuration(null)).isEqualTo("0s")
        assertThat(FormatUtils.formatDuration(0L)).isEqualTo("0s")
        assertThat(FormatUtils.formatDuration(-5L)).isEqualTo("0s")
        assertThat(FormatUtils.formatDuration(45L)).isEqualTo("45s")
        assertThat(FormatUtils.formatDuration(60L)).isEqualTo("1m 0s")
        assertThat(FormatUtils.formatDuration(135L)).isEqualTo("2m 15s")
        assertThat(FormatUtils.formatDuration(3600L)).isEqualTo("60m 0s")
    }

    @Test
    fun formatTimestamp_formatsKnownTimes() {
        val now = System.currentTimeMillis()
        val formattedToday = FormatUtils.formatTimestamp(now)
        assertThat(formattedToday).startsWith("Today, ")

        val yesterday = now - 24 * 60 * 60 * 1000L
        val formattedYesterday = FormatUtils.formatTimestamp(yesterday)
        assertThat(formattedYesterday).isNotEmpty()

        val invalid = FormatUtils.formatTimestamp(0L)
        assertThat(invalid).isEqualTo("Unknown")
    }
}
