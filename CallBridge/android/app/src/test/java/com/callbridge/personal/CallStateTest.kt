package com.callbridge.personal

import com.callbridge.personal.calling.CallUiState
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class CallStateTest {

    @Test
    fun callState_idleIsInitialState() {
        val state: CallUiState = CallUiState.Idle
        assertThat(state).isInstanceOf(CallUiState.Idle::class.java)
    }

    @Test
    fun callState_connectedStoresValidTimestamp() {
        val now = System.currentTimeMillis()
        val state = CallUiState.Connected(
            destination = "+12025550143",
            contactName = "John Miller",
            connectedAtMillis = now
        )

        assertThat(state.destination).isEqualTo("+12025550143")
        assertThat(state.contactName).isEqualTo("John Miller")
        assertThat(state.connectedAtMillis).isEqualTo(now)
    }

    @Test
    fun callState_failedPreservesErrorDetails() {
        val state = CallUiState.Failed(
            errorCode = "DESTINATION_NOT_ALLOWED",
            userMessage = "Destination is not in US allowlist"
        )

        assertThat(state.errorCode).isEqualTo("DESTINATION_NOT_ALLOWED")
        assertThat(state.userMessage).contains("allowlist")
    }
}
