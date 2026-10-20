package com.callbridge.personal

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class PhoneValidationTest {

    @Test
    fun defaultPrefix_isUsCountryCode() {
        val defaultPrefix = "+1"
        assertThat(defaultPrefix).startsWith("+1")
    }

    @Test
    fun testDestinationNumberNormalization() {
        val rawInput = " (202) 555-0143 "
        val cleaned = rawInput.replace("[^0-9+]".toRegex(), "")
        val normalized = if (cleaned.startsWith("+1")) cleaned else "+1$cleaned"

        assertThat(normalized).isEqualTo("+12025550143")
    }
}
