package com.callbridge.personal.calling

import android.content.Context
import android.util.Log
import com.callbridge.personal.data.local.entity.CallHistoryEntity
import com.callbridge.personal.data.remote.api.VoiceApiService
import com.callbridge.personal.data.repository.CallHistoryRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.twilio.voice.AcceptOptions
import com.twilio.voice.Call
import com.twilio.voice.CallException
import com.twilio.voice.CallInvite
import com.twilio.voice.CancelledCallInvite
import com.twilio.voice.ConnectOptions
import com.twilio.voice.MessageListener
import com.twilio.voice.RegistrationException
import com.twilio.voice.RegistrationListener
import com.twilio.voice.Voice
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class CallManager(
    private val context: Context,
    private val apiServiceProvider: () -> VoiceApiService,
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
    private var activeCallInvite: CallInvite? = null
    private var activeCallRecordId: String? = null
    private var callStartedAt: Long? = null
    private var callConnectedAt: Long? = null
    private var currentDestination: String = ""
    private var currentContactName: String = ""
    private var currentDirection: String = "OUTBOUND"
    private var callJob: kotlinx.coroutines.Job? = null

    private val callListener = object : Call.Listener {
        override fun onRinging(call: Call) {
            Log.d(tag, "Call ringing: ${call.sid}")
            _callState.value = CallUiState.Ringing(currentDestination)
            audioDeviceManager.startRingbackTone()
        }

        override fun onConnected(call: Call) {
            audioDeviceManager.stopRingbackTone()
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
            audioDeviceManager.stopRingbackTone()
            audioDeviceManager.stopCallAudio()
            Log.e(tag, "Call connect failure: ${callException.message} (code ${callException.errorCode})")
            val userMsg = "${callException.message ?: "Failed to connect call"} (Twilio code ${callException.errorCode})"
            finalizeCallRecord(
                status = "FAILED",
                errorCode = callException.errorCode.toString(),
                errorMessage = userMsg
            )
        }

        override fun onDisconnected(call: Call, callException: CallException?) {
            audioDeviceManager.stopRingbackTone()
            audioDeviceManager.stopCallAudio()
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
        currentDirection = "OUTBOUND"
        callStartedAt = System.currentTimeMillis()
        callConnectedAt = null
        activeCallRecordId = UUID.randomUUID().toString()
        _isMuted.value = false
        _isSpeakerOn.value = false
        _isHeld.value = false
        _activeCallNote.value = ""

        _callState.value = CallUiState.FetchingToken

        callJob?.cancel()

        callJob = coroutineScope.launch(Dispatchers.IO) {
            try {
                if (!isActive) return@launch

                // Prepare hardware audio for VoIP call
                audioDeviceManager.startCallAudio()

                // 1. Fetch short-lived token from backend over HTTPS using dynamic service
                val apiService = apiServiceProvider()
                val tokenResponse = apiService.getVoiceToken(
                    mapOf("identity" to "cb_operator")
                )

                if (!isActive) return@launch

                if (tokenResponse.token.isBlank() ||
                    tokenResponse.token.startsWith("mock_") ||
                    tokenResponse.token.startsWith("dummy_")
                ) {
                    val err = "Backend server returned an invalid or mock token"
                    Log.e(tag, err)
                    audioDeviceManager.stopCallAudio()
                    finalizeCallRecord(status = "FAILED", errorCode = "INVALID_TOKEN", errorMessage = err)
                    return@launch
                }

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

            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                audioDeviceManager.stopCallAudio()
                val errorMsg = "Call Failed: ${e.localizedMessage ?: e.message ?: "Connection error"}"
                Log.e(tag, errorMsg, e)
                finalizeCallRecord(
                    status = "FAILED",
                    errorCode = "EXCEPTION",
                    errorMessage = errorMsg
                )
            }
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
        audioDeviceManager.stopRingbackTone()
        audioDeviceManager.stopCallAudio()
        if (activeCall != null) {
            try {
                activeCall?.disconnect()
            } catch (e: Exception) {
                // ignore
            }
            activeCall = null
        }
        val duration = if (callConnectedAt != null) ((System.currentTimeMillis() - callConnectedAt!!) / 1000).toInt() else 0
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
        audioDeviceManager.stopRingbackTone()
        audioDeviceManager.stopCallAudio()

        val recordId = activeCallRecordId ?: UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        val started = callStartedAt ?: now
        val duration = if (callConnectedAt != null) (now - callConnectedAt!!) / 1000 else null

        val record = CallHistoryEntity(
            id = recordId,
            destinationE164 = currentDestination,
            direction = currentDirection,
            status = status,
            durationSeconds = duration,
            startedAt = started,
            connectedAt = callConnectedAt,
            endedAt = now,
            errorCode = errorCode,
            note = _activeCallNote.value.ifBlank { if (status == "FAILED") errorMessage else null },
            createdAt = started
        )

        CoroutineScope(Dispatchers.IO).launch {
            try {
                callHistoryRepository.saveCallRecord(record)
            } catch (e: Exception) {
                Log.e(tag, "Failed to save call record", e)
            }
        }

        activeCallRecordId = null
        callStartedAt = null
        callConnectedAt = null
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
        audioDeviceManager.stopRingbackTone()
        audioDeviceManager.stopCallAudio()
        activeCall = null
        activeCallInvite = null
        _callState.value = CallUiState.Idle
    }

    // =========================================================================
    // Incoming Call Support (Twilio Voice FCM Push Registration & CallInvite)
    // =========================================================================

    private val registrationListener = object : RegistrationListener {
        override fun onRegistered(accessToken: String, fcmToken: String) {
            Log.d(tag, "Successfully registered for Twilio incoming calls via FCM")
        }

        override fun onError(registrationException: RegistrationException, accessToken: String, fcmToken: String) {
            Log.w(tag, "Twilio FCM registration error: ${registrationException.message}")
        }
    }

    private val incomingMessageListener = object : MessageListener {
        override fun onCallInvite(callInvite: CallInvite) {
            Log.d(tag, "Incoming CallInvite received from ${callInvite.from}")
            if (_callState.value !is CallUiState.Idle &&
                _callState.value !is CallUiState.Ended &&
                _callState.value !is CallUiState.Failed
            ) {
                // Already on another active call; reject new invite politely
                callInvite.reject(context)
                return
            }

            activeCallInvite = callInvite
            currentDestination = callInvite.from ?: "Unknown Caller"
            currentContactName = ""
            currentDirection = "INBOUND"
            _callState.value = CallUiState.IncomingRinging(
                callerNumber = currentDestination,
                contactName = ""
            )
        }

        override fun onCancelledCallInvite(
            cancelledCallInvite: CancelledCallInvite,
            callException: CallException?
        ) {
            Log.d(tag, "Incoming CallInvite cancelled: ${cancelledCallInvite.from}")
            val caller = cancelledCallInvite.from ?: currentDestination
            activeCallInvite = null
            if (_callState.value is CallUiState.IncomingRinging) {
                _callState.value = CallUiState.Idle
            }
            val now = System.currentTimeMillis()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    callHistoryRepository.saveCallRecord(
                        CallHistoryEntity(
                            id = UUID.randomUUID().toString(),
                            destinationE164 = caller,
                            direction = "INBOUND",
                            status = "MISSED",
                            durationSeconds = 0,
                            startedAt = now,
                            endedAt = now,
                            createdAt = now
                        )
                    )
                } catch (e: Exception) {
                    Log.e(tag, "Failed to log missed call: ${e.message}")
                }
            }
        }
    }

    fun registerForIncomingCalls(coroutineScope: CoroutineScope, overrideFcmToken: String? = null) {
        if (FirebaseApp.getApps(context).isEmpty()) {
            Log.d(tag, "Firebase not configured (google-services.json absent); skipping FCM voice registration")
            return
        }

        val handleToken: (String) -> Unit = { fcmToken ->
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val apiService = apiServiceProvider()
                    val tokenResponse = apiService.getVoiceToken(
                        mapOf("identity" to "cb_operator")
                    )
                    if (tokenResponse.token.isNotBlank() &&
                        !tokenResponse.token.startsWith("mock_") &&
                        !tokenResponse.token.startsWith("dummy_")
                    ) {
                        Voice.register(
                            tokenResponse.token,
                            Voice.RegistrationChannel.FCM,
                            fcmToken,
                            registrationListener
                        )
                    }
                } catch (e: Exception) {
                    Log.w(tag, "Skipping incoming call registration: ${e.message}")
                }
            }
        }

        if (!overrideFcmToken.isNullOrBlank()) {
            handleToken(overrideFcmToken)
        } else {
            try {
                FirebaseMessaging.getInstance().token.addOnSuccessListener { fcmToken ->
                    if (!fcmToken.isNullOrBlank()) {
                        handleToken(fcmToken)
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Unable to obtain FCM token: ${e.message}")
            }
        }
    }

    fun handleIncomingCallMessage(data: Map<String, String>): Boolean {
        return try {
            Voice.handleMessage(context, data, incomingMessageListener)
        } catch (e: Exception) {
            Log.e(tag, "Failed to process incoming Twilio push message: ${e.message}")
            false
        }
    }

    fun acceptIncomingCall() {
        val invite = activeCallInvite ?: return
        activeCallInvite = null
        callStartedAt = System.currentTimeMillis()
        callConnectedAt = null
        activeCallRecordId = UUID.randomUUID().toString()
        _isMuted.value = false
        _isSpeakerOn.value = false
        _isHeld.value = false
        _activeCallNote.value = ""
        audioDeviceManager.startCallAudio()
        _callState.value = CallUiState.Connecting(
            destination = currentDestination,
            contactName = currentContactName
        )

        val acceptOptions = AcceptOptions.Builder().build()
        activeCall = invite.accept(context, acceptOptions, callListener)
    }

    fun rejectIncomingCall() {
        val invite = activeCallInvite ?: return
        activeCallInvite = null
        invite.reject(context)
        val now = System.currentTimeMillis()
        val caller = currentDestination
        _callState.value = CallUiState.Idle
        CoroutineScope(Dispatchers.IO).launch {
            try {
                callHistoryRepository.saveCallRecord(
                    CallHistoryEntity(
                        id = UUID.randomUUID().toString(),
                        destinationE164 = caller,
                        direction = "INBOUND",
                        status = "REJECTED",
                        startedAt = now,
                        endedAt = now,
                        createdAt = now
                    )
                )
            } catch (e: Exception) {
                Log.e(tag, "Failed to log rejected call: ${e.message}")
            }
        }
    }
}
