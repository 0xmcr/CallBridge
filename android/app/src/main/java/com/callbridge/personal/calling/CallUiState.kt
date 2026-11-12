package com.callbridge.personal.calling

sealed interface CallUiState {
    data object Idle : CallUiState
    data object RequestingPermission : CallUiState
    data object FetchingToken : CallUiState

    data class Connecting(
        val destination: String,
        val contactName: String = ""
    ) : CallUiState

    data class Ringing(
        val destination: String,
        val contactName: String = ""
    ) : CallUiState

    data class IncomingRinging(
        val callerNumber: String,
        val contactName: String = ""
    ) : CallUiState

    data class Connected(
        val destination: String,
        val contactName: String = "",
        val connectedAtMillis: Long = System.currentTimeMillis()
    ) : CallUiState

    data object Ending : CallUiState

    data class Ended(
        val destination: String,
        val durationSeconds: Long,
        val status: String
    ) : CallUiState

    data class Failed(
        val errorCode: String,
        val userMessage: String
    ) : CallUiState
}
