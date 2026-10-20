package com.callbridge.personal

import com.callbridge.personal.data.local.entity.CallHistoryEntity
import com.callbridge.personal.data.local.entity.ContactEntity
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DataModelTest {

    @Test
    fun contactEntity_generatesValidUuidAndTimestamps() {
        val contact = ContactEntity(
            displayName = "Alice Smith",
            phoneE164 = "+12025550199",
            company = "Tech Corp"
        )

        assertThat(contact.id).isNotEmpty()
        assertThat(contact.displayName).isEqualTo("Alice Smith")
        assertThat(contact.phoneE164).isEqualTo("+12025550199")
        assertThat(contact.createdAt).isGreaterThan(0L)
        assertThat(contact.updatedAt).isGreaterThan(0L)
    }

    @Test
    fun callHistoryEntity_distinguishesUnknownDurationFromZero() {
        val callWithUnknownDuration = CallHistoryEntity(
            destinationE164 = "+14155550188",
            status = "FAILED",
            durationSeconds = null
        )

        val callWithZeroDuration = CallHistoryEntity(
            destinationE164 = "+14155550188",
            status = "COMPLETED",
            durationSeconds = 0L
        )

        assertThat(callWithUnknownDuration.durationSeconds).isNull()
        assertThat(callWithZeroDuration.durationSeconds).isEqualTo(0L)
    }

    @Test
    fun callHistoryEntity_defaultsToOutgoingDirection() {
        val call = CallHistoryEntity(
            destinationE164 = "+12125550122",
            status = "COMPLETED",
            durationSeconds = 120L
        )

        assertThat(call.direction).isEqualTo("OUTGOING")
    }
}
