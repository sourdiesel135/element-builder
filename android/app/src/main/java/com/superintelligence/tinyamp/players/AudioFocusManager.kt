package com.superintelligence.tinyamp.players

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.util.Log

/**
 * AudioFocusManager - Handles audio focus and ducking for proper Android audio behavior
 *
 * This class manages audio focus to ensure TinyAmp plays nicely with other audio apps:
 * - Request focus before playback
 * - Duck/pause when other apps need audio
 * - Resume when audio focus is regained
 */
class AudioFocusManager(
    private val context: Context,
    private val onFocusChanged: (FocusChange) -> Unit
) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var currentFocusState = AudioManager.AUDIOFOCUS_LOSS

    private val audioFocusRequest: AudioFocusRequest? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(
                    AudioAttributes.Builder().run {
                        setUsage(AudioAttributes.USAGE_MEDIA)
                        setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        build()
                    }
                )
                setOnAudioFocusChangeListener(audioFocusChangeListener)
                setAcceptsDelayedFocusGain(true)
                setWillPauseWhenDucked(false) // We'll handle ducking ourselves
                build()
            }
        } else {
            null
        }
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        currentFocusState = focusChange

        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d(TAG, "Audio focus gained")
                onFocusChanged(FocusChange.GAIN)
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(TAG, "Audio focus lost permanently")
                onFocusChanged(FocusChange.LOSS)
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(TAG, "Audio focus lost temporarily")
                onFocusChanged(FocusChange.LOSS_TRANSIENT)
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Log.d(TAG, "Audio focus lost - can duck")
                onFocusChanged(FocusChange.LOSS_TRANSIENT_CAN_DUCK)
            }
        }
    }

    /**
     * Request audio focus for playback
     *
     * @return true if focus was granted, false otherwise
     */
    fun requestAudioFocus(): Boolean {
        val result = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
                ?: AudioManager.AUDIOFOCUS_REQUEST_FAILED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                audioFocusChangeListener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        val granted = result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        if (granted) {
            Log.d(TAG, "Audio focus granted")
        } else {
            Log.w(TAG, "Audio focus denied")
        }

        return granted
    }

    /**
     * Abandon audio focus when playback stops
     */
    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(audioFocusChangeListener)
        }
        Log.d(TAG, "Audio focus abandoned")
    }

    /**
     * Get current audio focus state
     */
    fun getCurrentFocusState(): Int = currentFocusState

    /**
     * Check if we currently have audio focus
     */
    fun hasAudioFocus(): Boolean = currentFocusState == AudioManager.AUDIOFOCUS_GAIN

    sealed class FocusChange {
        object GAIN : FocusChange()
        object LOSS : FocusChange()
        object LOSS_TRANSIENT : FocusChange()
        object LOSS_TRANSIENT_CAN_DUCK : FocusChange()
    }

    companion object {
        private const val TAG = "AudioFocusManager"
    }
}
