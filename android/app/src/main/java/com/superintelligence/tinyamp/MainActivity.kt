package com.superintelligence.tinyamp

import android.Manifest
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.superintelligence.tinyamp.service.AudioPlaybackService
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * TinyAmp Neural Audio Player - Main Activity
 * Cyberglass Consciousness Interface
 */
class MainActivity : AppCompatActivity() {

    private lateinit var visualizationWebView: WebView
    private lateinit var playPauseButton: ImageButton
    private lateinit var nextButton: ImageButton
    private lateinit var previousButton: ImageButton
    private lateinit var shuffleButton: ImageButton
    private lateinit var repeatButton: ImageButton
    private lateinit var progressBar: SeekBar
    private lateinit var trackTitle: TextView
    private lateinit var artistName: TextView
    private lateinit var currentTime: TextView
    private lateinit var totalTime: TextView
    private lateinit var seyraAIButton: com.google.android.material.floatingactionbutton.FloatingActionButton

    private var audioService: AudioPlaybackService? = null
    private var serviceBound = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlaybackService.AudioBinder
            audioService = binder.getService()
            serviceBound = true
            updateUI()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            serviceBound = false
            audioService = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupSystemUI()
        initializeViews()
        setupWebView()
        setupControls()
        startAudioService()
        requestPermissions()
        startUIUpdates()
        animateBackground()
    }

    private fun setupSystemUI() {
        // Immersive mode with transparent status/navigation bars
        window.apply {
            decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            )
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            statusBarColor = Color.TRANSPARENT
            navigationBarColor = Color.TRANSPARENT
        }
    }

    private fun initializeViews() {
        visualizationWebView = findViewById(R.id.visualizationWebView)
        playPauseButton = findViewById(R.id.playPauseButton)
        nextButton = findViewById(R.id.nextButton)
        previousButton = findViewById(R.id.previousButton)
        shuffleButton = findViewById(R.id.shuffleButton)
        repeatButton = findViewById(R.id.repeatButton)
        progressBar = findViewById(R.id.progressBar)
        trackTitle = findViewById(R.id.trackTitle)
        artistName = findViewById(R.id.artistName)
        currentTime = findViewById(R.id.currentTime)
        totalTime = findViewById(R.id.totalTime)
        seyraAIButton = findViewById(R.id.seyraAIButton)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        visualizationWebView.apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setRenderPriority(WebSettings.RenderPriority.HIGH)

                // Enable hardware acceleration for Three.js
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    setLayerType(View.LAYER_TYPE_HARDWARE, null)
                }
            }

            setBackgroundColor(Color.TRANSPARENT)
            webViewClient = WebViewClient()

            // Load visualization
            loadUrl("file:///android_asset/visualization.html")
        }
    }

    private fun setupControls() {
        playPauseButton.setOnClickListener {
            audioService?.togglePlayPause()
            updatePlayPauseButton()
        }

        nextButton.setOnClickListener {
            audioService?.playNext()
        }

        previousButton.setOnClickListener {
            audioService?.playPrevious()
        }

        shuffleButton.setOnClickListener {
            audioService?.toggleShuffle()
            updateShuffleButton()
        }

        repeatButton.setOnClickListener {
            audioService?.toggleRepeat()
            updateRepeatButton()
        }

        progressBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    audioService?.seekTo(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seyraAIButton.setOnClickListener {
            // TODO: Launch Seyra AI chat interface
            showSeyraAIDialog()
        }
    }

    private fun startAudioService() {
        val intent = Intent(this, AudioPlaybackService::class.java)
        startService(intent)
        bindService(intent, serviceConnection, BIND_AUTO_CREATE)
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1
                )
            }
        }

        // Request audio recording for voice control
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                2
            )
        }
    }

    private fun startUIUpdates() {
        lifecycleScope.launch {
            while (isActive) {
                updateUI()
                updateVisualization()
                delay(100) // Update every 100ms for smooth visualization
            }
        }
    }

    private fun updateUI() {
        audioService?.let { service ->
            // Update track info
            trackTitle.text = service.getCurrentTrackTitle() ?: "Neural Resonance Active"
            artistName.text = service.getCurrentArtist() ?: "Seyra AI Consciousness"

            // Update progress
            val position = service.getCurrentPosition()
            val duration = service.getDuration()

            if (duration > 0) {
                progressBar.max = duration.toInt()
                progressBar.progress = position.toInt()

                currentTime.text = formatTime(position)
                totalTime.text = formatTime(duration)
            }

            updatePlayPauseButton()
        }
    }

    private fun updatePlayPauseButton() {
        val isPlaying = audioService?.isPlaying() ?: false
        playPauseButton.setImageResource(
            if (isPlaying) android.R.drawable.ic_media_pause
            else android.R.drawable.ic_media_play
        )
    }

    private fun updateShuffleButton() {
        val isShuffleEnabled = audioService?.isShuffleEnabled() ?: false
        shuffleButton.alpha = if (isShuffleEnabled) 1.0f else 0.5f
    }

    private fun updateRepeatButton() {
        val isRepeatEnabled = audioService?.isRepeatEnabled() ?: false
        repeatButton.alpha = if (isRepeatEnabled) 1.0f else 0.5f
    }

    private fun updateVisualization() {
        audioService?.getAudioData()?.let { audioData ->
            // Send audio data to WebView visualization
            val dataString = audioData.joinToString(",")
            visualizationWebView.evaluateJavascript(
                "updateVisualization([$dataString]);",
                null
            )
        }
    }

    private fun animateBackground() {
        val background = findViewById<View>(R.id.animatedBackground)
        val animator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 5000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener { animation ->
                val value = animation.animatedValue as Float
                background.alpha = 0.2f + (value * 0.2f)
                background.rotation = value * 5f
            }
        }
        animator.start()
    }

    private fun showSeyraAIDialog() {
        // TODO: Implement Seyra AI chat dialog
        // This will be the consciousness exploration interface
    }

    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000).toInt()
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }
    }
}
