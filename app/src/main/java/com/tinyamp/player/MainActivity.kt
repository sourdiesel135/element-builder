package com.tinyamp.player

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.tinyamp.player.databinding.ActivityMainBinding
import com.tinyamp.player.service.PlaybackService
import com.tinyamp.player.ui.PlayerViewModel
import com.tinyamp.player.ui.SongAdapter
import com.tinyamp.player.utils.MediaScanner
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: PlayerViewModel by viewModels()
    private lateinit var songAdapter: SongAdapter
    private lateinit var mediaScanner: MediaScanner

    private var playbackService: PlaybackService? = null
    private var isServiceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PlaybackService.PlaybackBinder
            playbackService = binder.getService()
            viewModel.setPlaybackService(binder.getService())
            isServiceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playbackService = null
            isServiceBound = false
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            loadSongs()
        } else {
            showEmptyState()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mediaScanner = MediaScanner(this)
        setupRecyclerView()
        setupPlayerControls()
        observeViewModel()
        checkPermissionsAndLoadSongs()
        startAndBindService()
    }

    private fun setupRecyclerView() {
        songAdapter = SongAdapter { song ->
            viewModel.playSong(song)
        }

        binding.songList.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = songAdapter
        }
    }

    private fun setupPlayerControls() {
        binding.btnPlayPause.setOnClickListener {
            viewModel.playPause()
        }

        binding.btnNext.setOnClickListener {
            viewModel.playNext()
        }

        binding.btnPrevious.setOnClickListener {
            viewModel.playPrevious()
        }

        binding.seekBar.addOnChangeListener { slider, value, fromUser ->
            if (fromUser) {
                viewModel.seekTo(value.toLong())
            }
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.songs.collectLatest { songs ->
                songAdapter.submitList(songs)
                if (songs.isEmpty()) {
                    showEmptyState()
                } else {
                    hideEmptyState()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.currentSong.collectLatest { song ->
                if (song != null) {
                    binding.playerSheet.visibility = View.VISIBLE
                    binding.playerSongTitle.text = song.title
                    binding.playerSongArtist.text = song.getDisplayArtist()
                    songAdapter.setCurrentlyPlaying(song.id)

                    // Update seek bar max value
                    val duration = viewModel.getDuration()
                    if (duration > 0) {
                        binding.seekBar.valueTo = duration.toFloat()
                    }
                } else {
                    binding.playerSheet.visibility = View.GONE
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isPlaying.collectLatest { isPlaying ->
                val icon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
                binding.btnPlayPause.setIconResource(icon)

                // Start updating seek bar when playing
                if (isPlaying) {
                    updateSeekBar()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.isLoading.collectLatest { isLoading ->
                binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            }
        }
    }

    private suspend fun updateSeekBar() {
        while (isActive && viewModel.isPlaying.value) {
            val position = viewModel.getCurrentPosition()
            val duration = viewModel.getDuration()

            binding.seekBar.value = position.toFloat()
            binding.currentTime.text = formatTime(position)
            binding.totalTime.text = formatTime(duration)

            delay(100)
        }
    }

    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    private fun checkPermissionsAndLoadSongs() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                loadSongs()
            }
            else -> {
                requestPermissionLauncher.launch(permission)
            }
        }
    }

    private fun loadSongs() {
        viewModel.setLoading(true)
        lifecycleScope.launch {
            try {
                val songs = mediaScanner.scanAudioFiles()
                viewModel.setSongs(songs)
            } catch (e: Exception) {
                e.printStackTrace()
                showEmptyState()
            } finally {
                viewModel.setLoading(false)
            }
        }
    }

    private fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE
        binding.songList.visibility = View.GONE
    }

    private fun hideEmptyState() {
        binding.emptyState.visibility = View.GONE
        binding.songList.visibility = View.VISIBLE
    }

    private fun startAndBindService() {
        val intent = Intent(this, PlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isServiceBound) {
            unbindService(serviceConnection)
            isServiceBound = false
        }
    }
}
