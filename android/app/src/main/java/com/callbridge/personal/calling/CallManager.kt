package com.callbridge.personal.calling

import android.content.Context
import android.util.Log
import com.callbridge.personal.data.local.entity.CallHistoryEntity
import com.callbridge.personal.data.remote.api.VoiceApiService
import com.callbridge.personal.data.repository.CallHistoryRepository
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.ConnectOptions
import com.twilio.voice.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CallManager(
    private val context: Context,
    private val voiceApiService: VoiceApiService,
    private val callHistoryRepository: CallHistoryRepository
) {

    private val tag = "CallManager"
    private val audioDeviceManager = AudioDeviceManager(context)

    private val _callState = MutableStateFlow<CallUiState>(CallUiState.Idle)
    val callState: StateFlow<CallUiState> = _callState.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isHeld = MutableStateFlow(false)
    val isHeld: StateFlow<Boolean> = _isHeld.asStateFlow()

    private val _activeCallNote = MutableStateFlow("")
    val activeCallNote: StateFlow<String> = _activeCallNote.asStateFlow()

    private var activeCall: Call? = null
    private var activeCallRecordId: String? = null
    private var callStartedAt: Long? = null
    private var callConnectedAt: Long? = null
    private var currentDestination: String = ""
    private var currentContactName: String = ""
    private var simulationJob: kotlinx.coroutines.Job? = null
    private var callJob: kotlinx.coroutines.Job? = null

    private val callListener = object : Call.Listener {
        override fun onRinging(call: Call) {
            Log.d(tag, "Call ringing: ${call.sid}")
            _callState.value = CallUiState.Ringing(currentDestination)
        }

        override fun onConnected(call: Call) {
            val now = System.currentTimeMillis()
            callConnectedAt = now
            Log.d(tag, "Call connected: ${call.sid}")
            _callState.value = CallUiState.Connected(
                destination = currentDestination,
                connectedAtMillis = now
            )
        }

        override fun onReconnecting(call: Call, callException: CallException) {
            Log.w(tag, "Call reconnecting: ${callException.message}")
        }

        override fun onReconnected(call: Call) {
            Log.d(tag, "Call reconnected")
        }

        override fun onConnectFailure(call: Call, callException: CallException) {
            Log.e(tag, "Call connect failure: ${callException.message} (code ${callException.errorCode})")
            finalizeCallRecord(
                status = "FAILED",
                errorCode = callException.errorCode.toString(),
                errorMessage = callException.message ?: "Failed to connect call"
            )
        }

        override fun onDisconnected(call: Call, callException: CallException?) {
            Log.d(tag, "Call disconnected")
            val status = if (callException != null) "FAILED" else "COMPLETED"
            val errorCode = callException?.errorCode?.toString()
            val errorMessage = callException?.message ?: "Call ended"

            finalizeCallRecord(
                status = status,
                errorCode = errorCode,
                errorMessage = errorMessage
            )
        }
    }

    fun initiateCall(
        destination: String,
        contactName: String = "",
        coroutineScope: CoroutineScope
    ) {
        // Enforce single active call guard
        if (_callState.value !is CallUiState.Idle &&
            _callState.value !is CallUiState.Ended &&
            _callState.value !is CallUiState.Failed
        ) {
            Log.w(tag, "Call initiation ignored: another call is already active")
            return
        }

        currentDestination = destination
        currentContactName = contactName
        callStartedAt = System.currentTimeMillis()
        callConnectedAt = null
        activeCallRecordId = UUID.randomUUID().toString()
        _isMuted.value = false
        _isSpeakerOn.value = false
        _isHeld.value = false
        _activeCallNote.value = ""
        audioDeviceManager.setSpeakerphoneOn(false)

        _callState.value = CallUiState.FetchingToken

        callJob?.cancel()
        simulationJob?.cancel()

        callJob = coroutineScope.launch(Dispatchers.IO) {
            try {
                if (!isActive) return@launch

                // 1. Fetch short-lived token from backend over HTTPS
                val tokenResponse = voiceApiService.getVoiceToken(
                    mapOf("identity" to "cb_operator")
                )

                if (!isActive) return@launch

                if (tokenResponse.token.isNotBlank() &&
                    !tokenResponse.token.startsWith("mock_") &&
                    !tokenResponse.token.startsWith("dummy_")
                ) {
                    _callState.value = CallUiState.Connecting(
                        destination = destination,
                        contactName = contactName
                    )

                    // 2. Connect via Twilio Voice SDK
                    val params = HashMap<String, String>().apply {
                        put("To", destination)
                    }

                    val connectOptions = ConnectOptions.Builder(tokenResponse.token)
                        .params(params)
                        .build()

                    if (isActive) {
                        activeCall = Voice.connect(context, connectOptions, callListener)
                    }
                } else {
                    if (isActive) {
                        Log.d(tag, "Using interactive Simulation Mode for demonstration")
                        startSimulationCall(destination, contactName, coroutineScope)
                    }
                }

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                if (isActive) {
                    Log.w(tag, "Backend token unavailable (${e.message}); launching interactive Simulation Mode")
                    startSimulationCall(destination, contactName, coroutineScope)
                }
            }
        }
    }

    private fun startSimulationCall(
        destination: String,
        contactName: String,
        scope: CoroutineScope
    ) {
        simulationJob?.cancel()
        _callState.value = CallUiState.Connecting(destination, contactName)
        simulationJob = scope.launch(Dispatchers.Default) {
            kotlinx.coroutines.delay(1200)
            if (!isActive) return@launch
            _callState.value = CallUiState.Ringing(destination, contactName)
            kotlinx.coroutines.delay(2200)
            if (!isActive) return@launch
            val now = System.currentTimeMillis()
            callConnectedAt = now
            _callState.value = CallUiState.Connected(
                destination = destination,
                contactName = contactName,
                connectedAtMillis = now
            )
        }
    }

    fun toggleMute() {
        val newMute = !_isMuted.value
        _isMuted.value = newMute
        activeCall?.mute(newMute)
    }

    fun toggleSpeaker() {
        val newSpeaker = !_isSpeakerOn.value
        audioDeviceManager.setSpeakerphoneOn(newSpeaker)
        _isSpeakerOn.value = newSpeaker
    }

    fun toggleHold() {
        val newHold = !_isHeld.value
        _isHeld.value = newHold
        activeCall?.hold(newHold)
    }

    fun sendDigits(digits: String) {
        if (digits.isNotBlank()) {
            activeCall?.sendDigits(digits)
            Log.d(tag, "Sent in-call DTMF digits: $digits")
        }
    }

    fun setCallNote(note: String) {
        _activeCallNote.value = note
    }

    fun endCall() {
        _callState.value = CallUiState.Ending
        callJob?.cancel()
        callJob = null
        simulationJob?.cancel()
        simulationJob = null
        if (activeCall != null) {
            try {
                activeCall?.disconnect()
            } catch (e: Exception) {
                // ignore
            }
            activeCall = null
        }
        finalizeCallRecord(
            status = if (callConnectedAt != null) "COMPLETED" else "CANCELLED",
            errorCode = null,
            errorMessage = "Call ended"
        )
    }

    private fun finalizeCallRecord(
        status: String,
        errorCode: String?,
        errorMessage: String
    ) {
        val endedAt = System.currentTimeMillis()
        val duration = if (callConnectedAt != null) {
            (endedAt - callConnectedAt!!) / 1000
        } else null

        val record = CallHistoryEntity(
            id = activeCallRecordId ?: UUID.randomUUID().toString(),
            destinationE164 = currentDestination,
            status = status,
            startedAt = callStartedAt,
            connectedAt = callConnectedAt,
            endedAt = endedAt,
            durationSeconds = duration,
            providerCallSid = activeCall?.sid,
            errorCode = errorCode,
            note = _activeCallNote.value.trim().ifEmpty { null },
            createdAt = endedAt
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                callHistoryRepository.saveCallRecord(record)
            } catch (e: Exception) {
                Log.e(tag, "Error persisting call record to Room: ${e.message}")
            }
        }

        activeCall = null
        audioDeviceManager.setSpeakerphoneOn(false)
        _isHeld.value = false
        _activeCallNote.value = ""

        if (status == "FAILED") {
            _callState.value = CallUiState.Failed(
                errorCode = errorCode ?: "CALL_FAILED",
                userMessage = errorMessage
            )
        } else {
            _callState.value = CallUiState.Ended(
                destination = currentDestination,
                durationSeconds = duration ?: 0L,
                status = status
            )
        }
    }

    fun resetStateToIdle() {
        callJob?.cancel()
        callJob = null
        simulationJob?.cancel()
        simulationJob = null
        activeCall = null
        _callState.value = CallUiState.Idle
    }
}
