package com.callbridge.personal.ui.util

import android.media.AudioManager
import android.media.ToneGenerator

object DtmfPlayer {

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_DTMF, 85)
    } catch (e: Throwable) {
        null
    }

    /**
     * Plays authentic telephone DTMF tone for the given dialpad character.
     */
    fun playTone(char: Char) {
        val tone = when (char) {
            '0' -> ToneGenerator.TONE_DTMF_0
            '1' -> ToneGenerator.TONE_DTMF_1
            '2' -> ToneGenerator.TONE_DTMF_2
            '3' -> ToneGenerator.TONE_DTMF_3
            '4' -> ToneGenerator.TONE_DTMF_4
            '5' -> ToneGenerator.TONE_DTMF_5
            '6' -> ToneGenerator.TONE_DTMF_6
            '7' -> ToneGenerator.TONE_DTMF_7
            '8' -> ToneGenerator.TONE_DTMF_8
            '9' -> ToneGenerator.TONE_DTMF_9
            '*' -> ToneGenerator.TONE_DTMF_S
            '#' -> ToneGenerator.TONE_DTMF_P
            else -> -1
        }
        if (tone != -1) {
            try {
                toneGenerator?.startTone(tone, 120) // 120ms standard burst
            } catch (ignored: Throwable) {}
        }
    }

    /**
     * Plays a crisp tactile delete click tone.
     */
    fun playDeleteTone() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 50)
        } catch (ignored: Throwable) {}
    }
}
