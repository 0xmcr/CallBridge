package com.callbridge.personal.calling

import android.content.Context
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.util.Log

class AudioDeviceManager(private val context: Context) {

    private val tag = "AudioDeviceManager"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var toneGenerator: ToneGenerator? = null

    /**
     * Prepares hardware audio routing for an active VoIP telecommunication call.
     * Keeps AudioManager in MODE_IN_COMMUNICATION so earpiece and mic work properly.
     */
    fun startCallAudio() {
        try {
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
            setSpeakerphoneOn(false)
            Log.d(tag, "VoIP call audio started: mode=MODE_IN_COMMUNICATION, speaker=false")
        } catch (e: Exception) {
            Log.e(tag, "Failed to start call audio", e)
        }
    }

    /**
     * Toggles audio between speakerphone and standard telephone earpiece.
     */
    fun setSpeakerphoneOn(on: Boolean) {
        try {
            // Ensure we stay in IN_COMMUNICATION during call
            audioManager.mode = AudioManager.MODE_IN_COMMUNICATION

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (on) {
                    val speakerDevice = audioManager.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
                    }
                    if (speakerDevice != null) {
                        audioManager.setCommunicationDevice(speakerDevice)
                    }
                } else {
                    val earpieceDevice = audioManager.availableCommunicationDevices.firstOrNull {
                        it.type == AudioDeviceInfo.TYPE_BUILTIN_EARPIECE
                    }
                    if (earpieceDevice != null) {
                        audioManager.setCommunicationDevice(earpieceDevice)
                    } else {
                        audioManager.clearCommunicationDevice()
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = on
            }
            Log.d(tag, "Speakerphone set to $on")
        } catch (e: Exception) {
            Log.e(tag, "Failed to set speakerphone to $on", e)
        }
    }

    /**
     * Restores normal system audio mode when the call ends.
     */
    fun stopCallAudio() {
        stopRingbackTone()
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.clearCommunicationDevice()
            } else {
                @Suppress("DEPRECATION")
                audioManager.isSpeakerphoneOn = false
            }
            audioManager.mode = AudioManager.MODE_NORMAL
            Log.d(tag, "VoIP call audio stopped: mode restored to MODE_NORMAL")
        } catch (e: Exception) {
            Log.e(tag, "Failed to stop call audio", e)
        }
    }

    /**
     * Plays telephone ringback tone (ringing sound) while waiting for the called party to answer.
     */
    fun startRingbackTone() {
        try {
            if (toneGenerator == null) {
                toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 65)
            }
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE)
            Log.d(tag, "Ringback tone started")
        } catch (e: Exception) {
            Log.w(tag, "Could not start ringback tone: ${e.message}")
        }
    }

    /**
     * Stops ringing sound as soon as call connects, fails, or is cancelled.
     */
    fun stopRingbackTone() {
        try {
            toneGenerator?.stopTone()
            toneGenerator?.release()
            toneGenerator = null
            Log.d(tag, "Ringback tone stopped")
        } catch (e: Exception) {
            toneGenerator = null
        }
    }

    fun hasBluetoothDevice(): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                audioManager.availableCommunicationDevices.any {
                    it.type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO ||
                    it.type == AudioDeviceInfo.TYPE_BLE_HEADSET
                }
            } else {
                @Suppress("DEPRECATION")
                audioManager.isBluetoothScoAvailableOffCall || audioManager.isBluetoothA2dpOn
            }
        } catch (_: Exception) {
            false
        }
    }

    fun isSpeakerphoneOn(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            audioManager.communicationDevice?.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        } else {
            @Suppress("DEPRECATION")
            audioManager.isSpeakerphoneOn
        }
    }
}
