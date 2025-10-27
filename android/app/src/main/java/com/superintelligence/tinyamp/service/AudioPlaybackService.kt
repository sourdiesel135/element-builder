package com.superintelligence.tinyamp.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.superintelligence.tinyamp.R
import com.superintelligence.tinyamp.players.AudioFocusManager
import com.superintelligence.tinyamp.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.net.URL

/**
 * AudioPlaybackService - Foreground service for audio playback with ExoPlayer
 *
 * This service handles:
 * - Audio playback using ExoPlayer
 * - Background playback with MediaSession
 * - Foreground service with media notification
 * - Audio focus management
 * - Playback state synchronization with WebView
 */
class AudioPlaybackService : Service() {

    private val binder = AudioBinder()
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    // ExoPlayer
    private lateinit var exoPlayer: ExoPlayer

    // Media Session
    private lateinit var mediaSession: MediaSessionCompat

    // Audio Focus
    private lateinit var audioFocusManager: AudioFocusManager

    // Notification
    private lateinit var notificationManager: NotificationManager
    private var notificationBuilder: NotificationCompat.Builder? = null

    // Playback state
    private var currentTitle = "TinyAmp"
    private var currentArtist = "Neural Audio Player"
    private var currentAlbumArt: Bitmap? = null
    private var isPlaying = false
    private var shouldPlayWhenReady = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")

        // Initialize notification manager
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        createNotificationChannel()

        // Initialize ExoPlayer
        initializePlayer()

        // Initialize MediaSession
        initializeMediaSession()

        // Initialize AudioFocus
        initializeAudioFocus()
    }

    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            addListener(playerListener)
            playWhenReady = false
        }
    }

    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, TAG).apply {
            setCallback(mediaSessionCallback)
            isActive = true
        }

        updatePlaybackState(PlaybackStateCompat.STATE_NONE)
    }

    private fun initializeAudioFocus() {
        audioFocusManager = AudioFocusManager(this) { focusChange ->
            handleAudioFocusChange(focusChange)
        }
    }

    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_IDLE -> {
                    Log.d(TAG, "Player state: IDLE")
                    updatePlaybackState(PlaybackStateCompat.STATE_NONE)
                }
                Player.STATE_BUFFERING -> {
                    Log.d(TAG, "Player state: BUFFERING")
                    updatePlaybackState(PlaybackStateCompat.STATE_BUFFERING)
                    updateNotification()
                }
                Player.STATE_READY -> {
                    Log.d(TAG, "Player state: READY")
                    updatePlaybackState(
                        if (exoPlayer.playWhenReady) PlaybackStateCompat.STATE_PLAYING
                        else PlaybackStateCompat.STATE_PAUSED
                    )
                    updateNotification()

                    // Send playback info to WebView
                    sendPlaybackUpdate()
                }
                Player.STATE_ENDED -> {
                    Log.d(TAG, "Player state: ENDED")
                    updatePlaybackState(PlaybackStateCompat.STATE_STOPPED)

                    // Notify WebView that track ended
                    sendBroadcast("TRACK_ENDED")
                }
            }
        }

        override fun onIsPlayingChanged(playing: Boolean) {
            isPlaying = playing
            Log.d(TAG, "Is playing: $playing")

            updatePlaybackState(
                if (playing) PlaybackStateCompat.STATE_PLAYING
                else PlaybackStateCompat.STATE_PAUSED
            )
            updateNotification()

            // Send state to WebView
            sendPlaybackUpdate()
        }

        override fun onPlayerError(error: PlaybackException) {
            Log.e(TAG, "Player error: ${error.message}", error)
            updatePlaybackState(PlaybackStateCompat.STATE_ERROR)

            // Notify WebView of error
            sendBroadcast("PLAYBACK_ERROR", error.message ?: "Unknown error")
        }
    }

    private val mediaSessionCallback = object : MediaSessionCompat.Callback() {
        override fun onPlay() {
            Log.d(TAG, "MediaSession: onPlay")
            resumePlayback()
        }

        override fun onPause() {
            Log.d(TAG, "MediaSession: onPause")
            pausePlayback()
        }

        override fun onStop() {
            Log.d(TAG, "MediaSession: onStop")
            stopPlayback()
        }

        override fun onSeekTo(pos: Long) {
            Log.d(TAG, "MediaSession: onSeekTo $pos")
            exoPlayer.seekTo(pos)
        }

        override fun onSkipToNext() {
            Log.d(TAG, "MediaSession: onSkipToNext")
            sendBroadcast("NEXT_TRACK")
        }

        override fun onSkipToPrevious() {
            Log.d(TAG, "MediaSession: onSkipToPrevious")
            sendBroadcast("PREVIOUS_TRACK")
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_STICKY
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PLAY -> {
                val url = intent.getStringExtra("url") ?: return
                val title = intent.getStringExtra("title") ?: "Unknown"
                val artist = intent.getStringExtra("artist") ?: "Unknown Artist"
                val albumArtUrl = intent.getStringExtra("albumArt")

                playTrack(url, title, artist, albumArtUrl)
            }
            ACTION_PAUSE -> pausePlayback()
            ACTION_RESUME -> resumePlayback()
            ACTION_STOP -> stopPlayback()
            ACTION_SEEK -> {
                val position = intent.getLongExtra("position", 0L)
                exoPlayer.seekTo(position)
            }
            ACTION_SET_VOLUME -> {
                val volume = intent.getFloatExtra("volume", 1.0f)
                exoPlayer.volume = volume
            }
            ACTION_UPDATE_METADATA -> {
                val title = intent.getStringExtra("title") ?: currentTitle
                val artist = intent.getStringExtra("artist") ?: currentArtist
                val albumArtUrl = intent.getStringExtra("albumArt")

                updateMetadata(title, artist, albumArtUrl)
            }
        }
    }

    private fun playTrack(url: String, title: String, artist: String, albumArtUrl: String?) {
        Log.d(TAG, "Playing track: $title by $artist - $url")

        currentTitle = title
        currentArtist = artist

        // Load album art asynchronously
        loadAlbumArt(albumArtUrl)

        // Request audio focus
        if (!audioFocusManager.requestAudioFocus()) {
            Log.w(TAG, "Failed to gain audio focus")
            return
        }

        // Prepare media item
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        shouldPlayWhenReady = true

        // Start foreground service
        startForeground(NOTIFICATION_ID, buildNotification())
    }

    private fun pausePlayback() {
        Log.d(TAG, "Pausing playback")
        exoPlayer.playWhenReady = false
        shouldPlayWhenReady = false
        updateNotification()
    }

    private fun resumePlayback() {
        Log.d(TAG, "Resuming playback")

        if (!audioFocusManager.requestAudioFocus()) {
            Log.w(TAG, "Failed to gain audio focus")
            return
        }

        exoPlayer.playWhenReady = true
        shouldPlayWhenReady = true
        updateNotification()
    }

    private fun stopPlayback() {
        Log.d(TAG, "Stopping playback")
        exoPlayer.stop()
        exoPlayer.clearMediaItems()
        audioFocusManager.abandonAudioFocus()

        stopForeground(true)
        stopSelf()
    }

    private fun handleAudioFocusChange(focusChange: AudioFocusManager.FocusChange) {
        when (focusChange) {
            is AudioFocusManager.FocusChange.GAIN -> {
                if (shouldPlayWhenReady) {
                    exoPlayer.playWhenReady = true
                }
                exoPlayer.volume = 1.0f
            }
            is AudioFocusManager.FocusChange.LOSS -> {
                pausePlayback()
            }
            is AudioFocusManager.FocusChange.LOSS_TRANSIENT -> {
                pausePlayback()
            }
            is AudioFocusManager.FocusChange.LOSS_TRANSIENT_CAN_DUCK -> {
                exoPlayer.volume = 0.3f // Duck volume
            }
        }
    }

    private fun loadAlbumArt(albumArtUrl: String?) {
        if (albumArtUrl.isNullOrEmpty()) {
            currentAlbumArt = null
            return
        }

        serviceScope.launch(Dispatchers.IO) {
            try {
                val url = URL(albumArtUrl)
                val bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream())
                currentAlbumArt = bitmap

                launch(Dispatchers.Main) {
                    updateNotification()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading album art", e)
            }
        }
    }

    private fun updateMetadata(title: String, artist: String, albumArtUrl: String?) {
        currentTitle = title
        currentArtist = artist
        loadAlbumArt(albumArtUrl)
        updateNotification()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = getString(R.string.notification_channel_description)
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                R.drawable.ic_pause,
                getString(R.string.action_pause),
                getPendingIntent(ACTION_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                R.drawable.ic_play,
                getString(R.string.action_play),
                getPendingIntent(ACTION_RESUME)
            )
        }

        val contentIntent = Intent(this, MainActivity::class.java).let { intent ->
            PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setSmallIcon(R.drawable.ic_music_note)
            .setContentTitle(currentTitle)
            .setContentText(currentArtist)
            .setLargeIcon(currentAlbumArt ?: getDefaultAlbumArt())
            .setContentIntent(contentIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setShowWhen(false)
            .addAction(
                R.drawable.ic_previous,
                getString(R.string.action_previous),
                getPendingIntent(ACTION_PREVIOUS)
            )
            .addAction(playPauseAction)
            .addAction(
                R.drawable.ic_next,
                getString(R.string.action_next),
                getPendingIntent(ACTION_NEXT)
            )
            .setColor(getColor(R.color.neon_cyan))
            .build()
    }

    private fun updateNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun getPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, AudioPlaybackService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun getDefaultAlbumArt(): Bitmap {
        return BitmapFactory.decodeResource(resources, R.drawable.ic_music_note)
    }

    private fun updatePlaybackState(state: Int) {
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or
                        PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(state, exoPlayer.currentPosition, 1.0f)

        mediaSession.setPlaybackState(stateBuilder.build())
    }

    private fun sendPlaybackUpdate() {
        val intent = Intent("com.superintelligence.tinyamp.PLAYBACK_UPDATE").apply {
            putExtra("position", exoPlayer.currentPosition)
            putExtra("duration", exoPlayer.duration)
            putExtra("isPlaying", isPlaying)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun sendBroadcast(action: String, data: String = "") {
        val intent = Intent("com.superintelligence.tinyamp.$action").apply {
            if (data.isNotEmpty()) {
                putExtra("data", data)
            }
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun updatePlaybackState(state: String, title: String, artist: String, albumArt: String) {
        currentTitle = title
        currentArtist = artist
        isPlaying = state == "playing"
        updateNotification()
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Service destroyed")

        exoPlayer.release()
        mediaSession.release()
        audioFocusManager.abandonAudioFocus()
        serviceScope.cancel()
    }

    inner class AudioBinder : Binder() {
        fun getService(): AudioPlaybackService = this@AudioPlaybackService
    }

    companion object {
        private const val TAG = "AudioPlaybackService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "tinyamp_playback"

        const val ACTION_PLAY = "com.superintelligence.tinyamp.action.PLAY"
        const val ACTION_PAUSE = "com.superintelligence.tinyamp.action.PAUSE"
        const val ACTION_RESUME = "com.superintelligence.tinyamp.action.RESUME"
        const val ACTION_STOP = "com.superintelligence.tinyamp.action.STOP"
        const val ACTION_NEXT = "com.superintelligence.tinyamp.action.NEXT"
        const val ACTION_PREVIOUS = "com.superintelligence.tinyamp.action.PREVIOUS"
        const val ACTION_SEEK = "com.superintelligence.tinyamp.action.SEEK"
        const val ACTION_SET_VOLUME = "com.superintelligence.tinyamp.action.SET_VOLUME"
        const val ACTION_UPDATE_METADATA = "com.superintelligence.tinyamp.action.UPDATE_METADATA"
    }
}
