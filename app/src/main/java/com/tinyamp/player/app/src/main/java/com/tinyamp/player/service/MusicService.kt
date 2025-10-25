package com.tinyamp.player.service

import android.app.PendingIntent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.tinyamp.player.data.models.Song
import com.tinyamp.player.ui.main.MainActivity

class MusicService : MediaSessionService() {
    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    companion object {
        const val ACTION_PLAY = "com.tinyamp.player.PLAY"
        const val ACTION_PAUSE = "com.tinyamp.player.PAUSE"
        const val ACTION_NEXT = "com.tinyamp.player.NEXT"
        const val ACTION_PREVIOUS = "com.tinyamp.player.PREVIOUS"
        const val ACTION_STOP = "com.tinyamp.player.STOP"

        const val CUSTOM_COMMAND_TOGGLE_SHUFFLE = "TOGGLE_SHUFFLE"
        const val CUSTOM_COMMAND_TOGGLE_REPEAT = "TOGGLE_REPEAT"
    }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeSession()
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also {
                it.addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_ENDED -> {
                                // Handle playback end
                                it.seekToNext()
                            }
                            Player.STATE_READY -> {
                                // Player is ready
                            }
                            else -> {}
                        }
                    }

                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        // Update notification
                    }
                })
            }
    }

    private fun initializeSession() {
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(MediaSessionCallback())
            .build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

    private inner class MediaSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CUSTOM_COMMAND_TOGGLE_SHUFFLE, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_COMMAND_TOGGLE_REPEAT, Bundle.EMPTY))
                .build()

            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                CUSTOM_COMMAND_TOGGLE_SHUFFLE -> {
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                }
                CUSTOM_COMMAND_TOGGLE_REPEAT -> {
                    player.repeatMode = when (player.repeatMode) {
                        Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
                        Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
                        Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_OFF
                        else -> Player.REPEAT_MODE_OFF
                    }
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    fun setPlaylist(songs: List<Song>, startIndex: Int = 0) {
        val mediaItems = songs.map { song ->
            MediaItem.Builder()
                .setMediaId(song.id.toString())
                .setUri(Uri.parse(song.path))
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(song.title)
                        .setArtist(song.artist)
                        .setAlbumTitle(song.album)
                        .setArtworkUri(song.albumArtUri)
                        .build()
                )
                .build()
        }

        player.setMediaItems(mediaItems, startIndex, 0)
        player.prepare()
    }

    fun playSong(song: Song) {
        val mediaItem = MediaItem.Builder()
            .setMediaId(song.id.toString())
            .setUri(Uri.parse(song.path))
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(song.title)
                    .setArtist(song.artist)
                    .setAlbumTitle(song.album)
                    .setArtworkUri(song.albumArtUri)
                    .build()
            )
            .build()

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }
}
