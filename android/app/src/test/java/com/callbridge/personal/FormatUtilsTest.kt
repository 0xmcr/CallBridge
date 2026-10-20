package com.callbridge.personal

import com.callbridge.personal.ui.util.FormatUtils
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FormatUtilsTest {

    @Test
    fun extractNationalDigits_limitsToTenDigits() {
        // Strip country code 1 and symbols
        assertThat(FormatUtils.extractNationalDigits("+1 (202) 555-0143")).isEqualTo("2025550143")
        // Truncate more than 10 digits
        assertThat(FormatUtils.extractNationalDigits("2222222222222222")).isEqualTo("2222222222")
        // Empty
        assertThat(FormatUtils.extractNationalDigits("+1")).isEqualTo("")
    }

    @Test
    fun formatNanpDisplay_formatsProgressively() {
        assertThat(FormatUtils.formatNanpDisplay("")).isEqualTo("+1")
        assertThat(FormatUtils.formatNanpDisplay("202")).isEqualTo("+1 (202")
        assertThat(FormatUtils.formatNanpDisplay("202555")).isEqualTo("+1 (202) 555")
        assertThat(FormatUtils.formatNanpDisplay("2025550143")).isEqualTo("+1 (202) 555-0143")
    }

    @Test
    fun validateNanp_validatesAccurately() {
        // Incomplete
        val incomplete = FormatUtils.validateNanp("2025")
        assertThat(incomplete.isValid).isFalse()
        assertThat(incomplete.message).contains("Need 6 more digits")

        // Valid US Number
        val valid = FormatUtils.validateNanp("2025550143")
        assertThat(valid.isValid).isTrue()
        assertThat(valid.e164).isEqualTo("+12025550143")
        assertThat(valid.message).isEqualTo("Valid US Number")

        // Invalid Area code starting with 0 or 1
        val invalidArea = FormatUtils.validateNanp("0123456789")
        assertThat(invalidArea.isValid).isFalse()
        assertThat(invalidArea.message).contains("Area code")

        // Invalid Exchange code starting with 1
        val invalidExchange = FormatUtils.validateNanp("2021550143")
        assertThat(invalidExchange.isValid).isFalse()
        assertThat(invalidExchange.message).contains("Exchange code")
    }

    @Test
    fun normalizePhoneNumber_cleansFormattingAndAddsPlusOne() {
        assertThat(FormatUtils.normalizePhoneNumber("2025550143")).isEqualTo("+12025550143")
        assertThat(FormatUtils.normalizePhoneNumber("(202) 555-0143")).isEqualTo("+12025550143")
        assertThat(FormatUtils.normalizePhoneNumber("12025550143")).isEqualTo("+12025550143")
        assertThat(FormatUtils.normalizePhoneNumber("+12025550143")).isEqualTo("+12025550143")
        assertThat(FormatUtils.normalizePhoneNumber("+447911123456")).isEqualTo("+447911123456")
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
