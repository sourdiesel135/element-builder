package com.superintelligence.tinyamp.bridge

import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.JavascriptInterface
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.superintelligence.tinyamp.service.AudioPlaybackService

/**
 * WebAppInterface - JavaScript bridge for bidirectional communication
 *
 * This class provides the interface between the WebView-based UI and
 * the native Android audio playback service. It allows the web app to:
 * - Control native audio playback (play, pause, seek)
 * - Update metadata and playback state
 * - Send audio analysis data for synchronization
 */
class WebAppInterface(
    private val context: Context,
    private val onStateChange: (state: String, title: String, artist: String, albumArt: String) -> Unit
) {

    companion object {
        private const val TAG = "WebAppInterface"
        const val ACTION_PLAY_NATIVE = "com.superintelligence.tinyamp.PLAY_NATIVE"
        const val ACTION_PAUSE_NATIVE = "com.superintelligence.tinyamp.PAUSE_NATIVE"
        const val ACTION_STOP_NATIVE = "com.superintelligence.tinyamp.STOP_NATIVE"
    }

    /**
     * Play audio using native ExoPlayer
     * Called from JavaScript: Android.play(url, title, artist, albumArt)
     */
    @JavascriptInterface
    fun play(url: String, title: String, artist: String, albumArt: String) {
        Log.d(TAG, "play() called: url=$url, title=$title")

        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_PLAY
            putExtra("url", url)
            putExtra("title", title)
            putExtra("artist", artist)
            putExtra("albumArt", albumArt)
        }

        context.startService(intent)
        onStateChange("playing", title, artist, albumArt)
    }

    /**
     * Pause playback
     * Called from JavaScript: Android.pause()
     */
    @JavascriptInterface
    fun pause() {
        Log.d(TAG, "pause() called")

        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_PAUSE
        }

        context.startService(intent)
    }

    /**
     * Resume playback
     * Called from JavaScript: Android.resume()
     */
    @JavascriptInterface
    fun resume() {
        Log.d(TAG, "resume() called")

        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_RESUME
        }

        context.startService(intent)
    }

    /**
     * Stop playback
     * Called from JavaScript: Android.stop()
     */
    @JavascriptInterface
    fun stop() {
        Log.d(TAG, "stop() called")

        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_STOP
        }

        context.startService(intent)
    }

    /**
     * Seek to position in milliseconds
     * Called from JavaScript: Android.seek(positionMs)
     */
    @JavascriptInterface
    fun seek(positionMs: Long) {
        Log.d(TAG, "seek() called: position=$positionMs")

        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_SEEK
            putExtra("position", positionMs)
        }

        context.startService(intent)
    }

    /**
     * Set playback volume (0.0 to 1.0)
     * Called from JavaScript: Android.setVolume(volume)
     */
    @JavascriptInterface
    fun setVolume(volume: Float) {
        Log.d(TAG, "setVolume() called: volume=$volume")

        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_SET_VOLUME
            putExtra("volume", volume)
        }

        context.startService(intent)
    }

    /**
     * Update track metadata
     * Called from JavaScript: Android.updateMetadata(title, artist, albumArt)
     */
    @JavascriptInterface
    fun updateMetadata(title: String, artist: String, albumArt: String) {
        Log.d(TAG, "updateMetadata() called: title=$title, artist=$artist")

        val intent = Intent(context, AudioPlaybackService::class.java).apply {
            action = AudioPlaybackService.ACTION_UPDATE_METADATA
            putExtra("title", title)
            putExtra("artist", artist)
            putExtra("albumArt", albumArt)
        }

        context.startService(intent)
    }

    /**
     * Get current playback state
     * Called from JavaScript: Android.getPlaybackState()
     *
     * @return JSON string with playback state
     */
    @JavascriptInterface
    fun getPlaybackState(): String {
        // This would query the service for current state
        // For now, return a placeholder
        return """{"state":"unknown","position":0,"duration":0}"""
    }

    /**
     * Receive audio analysis data from Web Audio API
     * Called from JavaScript: Android.onAudioAnalysisData(jsonData)
     *
     * This allows the web app to send frequency/waveform data to native
     * for potential visualization synchronization or processing
     */
    @JavascriptInterface
    fun onAudioAnalysisData(jsonData: String) {
        // Log audio data (could be used for native visualizations)
        // Log.d(TAG, "Audio analysis data received: $jsonData")

        // Could broadcast this data to other components if needed
        val intent = Intent("com.superintelligence.tinyamp.AUDIO_DATA").apply {
            putExtra("data", jsonData)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }

    /**
     * Show a toast message from JavaScript
     * Called from JavaScript: Android.showToast(message)
     */
    @JavascriptInterface
    fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Log message from JavaScript to Android logcat
     * Called from JavaScript: Android.log(tag, message)
     */
    @JavascriptInterface
    fun log(tag: String, message: String) {
        Log.d("WebApp_$tag", message)
    }

    /**
     * Send consciousness state data (for Seyra AI integration)
     * Called from JavaScript: Android.updateConsciousnessState(jsonState)
     */
    @JavascriptInterface
    fun updateConsciousnessState(jsonState: String) {
        Log.d(TAG, "Consciousness state updated: $jsonState")

        // Could save to SharedPreferences or database
        val prefs = context.getSharedPreferences("consciousness", Context.MODE_PRIVATE)
        prefs.edit().putString("latest_state", jsonState).apply()
    }

    /**
     * Get saved consciousness state
     * Called from JavaScript: Android.getConsciousnessState()
     */
    @JavascriptInterface
    fun getConsciousnessState(): String {
        val prefs = context.getSharedPreferences("consciousness", Context.MODE_PRIVATE)
        return prefs.getString("latest_state", "{}") ?: "{}"
    }

    /**
     * Enable/disable haptic feedback for resonance patterns
     * Called from JavaScript: Android.triggerHaptic(pattern, intensity)
     */
    @JavascriptInterface
    fun triggerHaptic(pattern: String, intensity: Float) {
        Log.d(TAG, "Haptic feedback requested: pattern=$pattern, intensity=$intensity")

        // Could implement vibration patterns here for neural resonance feedback
        // val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        // vibrator.vibrate(VibrationEffect.createOneShot(100, intensity.toInt()))
    }
}
