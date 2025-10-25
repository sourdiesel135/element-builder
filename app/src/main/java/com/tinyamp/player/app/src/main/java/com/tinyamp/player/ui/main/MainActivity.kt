package com.tinyamp.player.ui.main

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.tinyamp.player.R
import com.tinyamp.player.data.models.PlaybackState
import com.tinyamp.player.data.models.PlaybackStatus
import com.tinyamp.player.data.models.Song
import com.tinyamp.player.databinding.ActivityMainBinding
import com.tinyamp.player.service.MusicService
import com.tinyamp.player.utils.PermissionHelper
import com.tinyamp.player.viewmodel.MusicViewModel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MusicViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.values.all { it }
        if (allGranted) {
            loadSongs()
        } else {
            showPermissionDeniedMessage()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupPlayerControls()
        setupObservers()

        checkPermissionsAndLoadSongs()
    }

    override fun onStart() {
        super.onStart()
        initializeMediaController()
    }

    override fun onStop() {
        super.onStop()
        releaseMediaController()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter { song, position ->
            playSong(song, position)
        }
        binding.recyclerViewSongs.adapter = songAdapter
    }

    private fun setupPlayerControls() {
        binding.apply {
            buttonPlayPause.setOnClickListener {
                togglePlayPause()
            }

            buttonNext.setOnClickListener {
                mediaController?.seekToNext()
            }

            buttonPrevious.setOnClickListener {
                mediaController?.seekToPrevious()
            }

            buttonShuffle.setOnClickListener {
                viewModel.toggleShuffle()
                mediaController?.shuffleModeEnabled = viewModel.playbackState.value.isShuffleEnabled
            }

            buttonRepeat.setOnClickListener {
                viewModel.toggleRepeatMode()
                val repeatMode = when (viewModel.playbackState.value.repeatMode) {
                    com.tinyamp.player.data.models.RepeatMode.OFF -> Player.REPEAT_MODE_OFF
                    com.tinyamp.player.data.models.RepeatMode.ALL -> Player.REPEAT_MODE_ALL
                    com.tinyamp.player.data.models.RepeatMode.ONE -> Player.REPEAT_MODE_ONE
                }
                mediaController?.repeatMode = repeatMode
            }

            seekBar.addOnChangeListener { _, value, fromUser ->
                if (fromUser) {
                    val duration = mediaController?.duration ?: 0
                    val position = (duration * value / 100).toLong()
                    mediaController?.seekTo(position)
                }
            }

            buttonGrantPermission.setOnClickListener {
                requestPermissions()
            }
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.songs.collect { songs ->
                songAdapter.submitList(songs)
                updateEmptyState(songs.isEmpty())
            }
        }

        lifecycleScope.launch {
            viewModel.playbackState.collect { state ->
                updatePlaybackUI(state)
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collect { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun checkPermissionsAndLoadSongs() {
        if (PermissionHelper.hasAllPermissions(this)) {
            loadSongs()
            binding.permissionLayout.visibility = View.GONE
        } else {
            binding.permissionLayout.visibility = View.VISIBLE
            binding.recyclerViewSongs.visibility = View.GONE
        }
    }

    private fun requestPermissions() {
        permissionLauncher.launch(PermissionHelper.getRequiredPermissions())
    }

    private fun loadSongs() {
        binding.permissionLayout.visibility = View.GONE
        binding.recyclerViewSongs.visibility = View.VISIBLE
        viewModel.loadSongs()
    }

    private fun showPermissionDeniedMessage() {
        Toast.makeText(
            this,
            R.string.permission_required,
            Toast.LENGTH_LONG
        ).show()
    }

    private fun initializeMediaController() {
        val sessionToken = SessionToken(this, ComponentName(this, MusicService::class.java))
        mediaControllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            setupMediaControllerListener()
        }, MoreExecutors.directExecutor())
    }

    private fun releaseMediaController() {
        mediaControllerFuture?.let { future ->
            MediaController.releaseFuture(future)
        }
        mediaController = null
    }

    private fun setupMediaControllerListener() {
        mediaController?.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayPauseButton(isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
                mediaItem?.let {
                    val songId = it.mediaId.toLongOrNull()
                    songId?.let { id ->
                        songAdapter.setCurrentPlayingSong(id)
                    }
                }
            }
        })
    }

    private fun playSong(song: Song, position: Int) {
        val songs = viewModel.songs.value
        viewModel.setCurrentPlaylist(songs)

        // Set playlist and start playback
        mediaController?.apply {
            setMediaItems(
                songs.map { s ->
                    androidx.media3.common.MediaItem.Builder()
                        .setMediaId(s.id.toString())
                        .setUri(s.path)
                        .setMediaMetadata(
                            androidx.media3.common.MediaMetadata.Builder()
                                .setTitle(s.title)
                                .setArtist(s.artist)
                                .setAlbumTitle(s.album)
                                .setArtworkUri(s.albumArtUri)
                                .build()
                        )
                        .build()
                },
                position,
                0
            )
            prepare()
            play()
        }

        // Update ViewModel state
        viewModel.updatePlaybackState(
            PlaybackState(
                currentSong = song,
                status = PlaybackStatus.PLAYING
            )
        )

        songAdapter.setCurrentPlayingSong(song.id)
    }

    private fun togglePlayPause() {
        mediaController?.apply {
            if (isPlaying) {
                pause()
                viewModel.updatePlaybackState(
                    viewModel.playbackState.value.copy(status = PlaybackStatus.PAUSED)
                )
            } else {
                play()
                viewModel.updatePlaybackState(
                    viewModel.playbackState.value.copy(status = PlaybackStatus.PLAYING)
                )
            }
        }
    }

    private fun updatePlaybackUI(state: PlaybackState) {
        binding.apply {
            state.currentSong?.let { song ->
                textCurrentSongTitle.text = song.title
                textCurrentSongArtist.text = song.displayArtist
                playerControlsCard.visibility = View.VISIBLE
            } ?: run {
                playerControlsCard.visibility = View.GONE
            }

            updatePlayPauseButton(state.isPlaying)

            // Update seek bar
            if (state.duration > 0) {
                seekBar.value = (state.currentPosition.toFloat() / state.duration.toFloat() * 100)
            }
        }
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        binding.buttonPlayPause.setImageResource(
            if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
        )
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerViewSongs.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}
