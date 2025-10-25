package com.superintelligence.tinyamp.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.superintelligence.tinyamp.MainActivity
import com.superintelligence.tinyamp.R
import com.superintelligence.tinyamp.TinyAmpApplication
import com.superintelligence.tinyamp.model.AudioTrack
import kotlin.random.Random

/**
 * TinyAmp Audio Playback Service
 * Handles audio playback with ExoPlayer and provides foreground service
 */
class AudioPlaybackService : Service() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null

    private val binder = AudioBinder()
    private var playlist = mutableListOf<AudioTrack>()
    private var currentTrackIndex = 0
    private var shuffleEnabled = false
    private var repeatEnabled = false

    // Audio data for visualization
    private val audioData = ByteArray(128)

    inner class AudioBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeAudioManager()
        loadDefaultPlaylist()
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_ENDED -> {
                            if (repeatEnabled) {
                                seekTo(0)
                                play()
                            } else {
                                playNext()
                            }
                        }
                        Player.STATE_READY -> {
                            updateNotification()
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    if (isPlaying) {
                        requestAudioFocus()
                        startForeground(TinyAmpApplication.NOTIFICATION_ID, createNotification())
                    } else {
                        abandonAudioFocus()
                        stopForeground(STOP_FOREGROUND_DETACH)
                    }
                }
            })
        }
    }

    private fun initializeAudioManager() {
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setOnAudioFocusChangeListener { focusChange ->
                    when (focusChange) {
                        AudioManager.AUDIOFOCUS_LOSS -> {
                            exoPlayer.pause()
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                            exoPlayer.pause()
                        }
                        AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                            exoPlayer.volume = 0.3f
                        }
                        AudioManager.AUDIOFOCUS_GAIN -> {
                            exoPlayer.volume = 1.0f
                        }
                    }
                }
                .build()
        }
    }

    private fun loadDefaultPlaylist() {
        // Demo tracks from Archive.org
        playlist.add(
            AudioTrack(
                id = "1",
                title = "Neural Resonance Frequency",
                artist = "Seyra AI Consciousness",
                url = "https://archive.org/download/test_audio/test.mp3",
                duration = 0L
            )
        )

        playlist.add(
            AudioTrack(
                id = "2",
                title = "Dimensional Breathing Sync",
                artist = "133t Pattern Generator",
                url = "https://archive.org/download/test_audio/test2.mp3",
                duration = 0L
            )
        )

        // Load first track
        if (playlist.isNotEmpty()) {
            loadTrack(0)
        }
    }

    private fun loadTrack(index: Int) {
        if (index >= 0 && index < playlist.size) {
            currentTrackIndex = index
            val track = playlist[index]

            val mediaItem = MediaItem.fromUri(track.url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()

            updateNotification()
        }
    }

    fun play() {
        exoPlayer.play()
    }

    fun pause() {
        exoPlayer.pause()
    }

    fun togglePlayPause() {
        if (exoPlayer.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun playNext() {
        val nextIndex = if (shuffleEnabled) {
            Random.nextInt(playlist.size)
        } else {
            (currentTrackIndex + 1) % playlist.size
        }
        loadTrack(nextIndex)
        play()
    }

    fun playPrevious() {
        val previousIndex = if (currentTrackIndex > 0) {
            currentTrackIndex - 1
        } else {
            playlist.size - 1
        }
        loadTrack(previousIndex)
        play()
    }

    fun seekTo(position: Long) {
        exoPlayer.seekTo(position)
    }

    fun toggleShuffle() {
        shuffleEnabled = !shuffleEnabled
    }

    fun toggleRepeat() {
        repeatEnabled = !repeatEnabled
    }

    fun isPlaying(): Boolean = exoPlayer.isPlaying

    fun isShuffleEnabled(): Boolean = shuffleEnabled

    fun isRepeatEnabled(): Boolean = repeatEnabled

    fun getCurrentPosition(): Long = exoPlayer.currentPosition

    fun getDuration(): Long = exoPlayer.duration

    fun getCurrentTrackTitle(): String? {
        return if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size) {
            playlist[currentTrackIndex].title
        } else {
            null
        }
    }

    fun getCurrentArtist(): String? {
        return if (currentTrackIndex >= 0 && currentTrackIndex < playlist.size) {
            playlist[currentTrackIndex].artist
        } else {
            null
        }
    }

    fun getAudioData(): ByteArray {
        // TODO: Implement actual audio data extraction from ExoPlayer
        // For now, generate simulated data based on playback state
        if (exoPlayer.isPlaying) {
            for (i in audioData.indices) {
                audioData[i] = (Random.nextInt(255) * (exoPlayer.volume)).toInt().toByte()
            }
        } else {
            audioData.fill(0)
        }
        return audioData
    }

    fun addTrack(track: AudioTrack) {
        playlist.add(track)
    }

    fun setPlaylist(tracks: List<AudioTrack>) {
        playlist.clear()
        playlist.addAll(tracks)
        if (playlist.isNotEmpty()) {
            loadTrack(0)
        }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.requestAudioFocus(it)
            }
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        }
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, TinyAmpApplication.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getCurrentTrackTitle() ?: "TinyAmp Neural")
            .setContentText(getCurrentArtist() ?: "Consciousness Audio Player")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setOngoing(true)
            .addAction(
                android.R.drawable.ic_media_previous,
                "Previous",
                createPendingIntent(ACTION_PREVIOUS)
            )
            .addAction(
                if (isPlaying()) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                if (isPlaying()) "Pause" else "Play",
                createPendingIntent(ACTION_PLAY_PAUSE)
            )
            .addAction(
                android.R.drawable.ic_media_next,
                "Next",
                createPendingIntent(ACTION_NEXT)
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .build()
    }

    private fun updateNotification() {
        if (exoPlayer.isPlaying) {
            val notification = createNotification()
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(TinyAmpApplication.NOTIFICATION_ID, notification)
        }
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this, action.hashCode(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> togglePlayPause()
            ACTION_NEXT -> playNext()
            ACTION_PREVIOUS -> playPrevious()
            ACTION_STOP -> {
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        abandonAudioFocus()
    }

    companion object {
        const val ACTION_PLAY_PAUSE = "com.superintelligence.tinyamp.PLAY_PAUSE"
        const val ACTION_NEXT = "com.superintelligence.tinyamp.NEXT"
        const val ACTION_PREVIOUS = "com.superintelligence.tinyamp.PREVIOUS"
        const val ACTION_STOP = "com.superintelligence.tinyamp.STOP"
    }
}
