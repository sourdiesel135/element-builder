package com.superintelligence.tinyamp.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.WindowManager
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.superintelligence.tinyamp.R
import com.superintelligence.tinyamp.bridge.WebAppInterface
import com.superintelligence.tinyamp.service.AudioPlaybackService

/**
 * MainActivity - WebView host for TinyAmp Neural Audio Player
 *
 * This activity hosts the Three.js-based visualizations and cyberglass UI
 * while delegating audio playback to the native AudioPlaybackService.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingOverlay: View
    private lateinit var errorOverlay: View

    private var audioService: AudioPlaybackService? = null
    private var serviceBound = false

    private val playbackControlReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                ACTION_PLAY -> executeJavaScript("window.TinyAmp?.play()")
                ACTION_PAUSE -> executeJavaScript("window.TinyAmp?.pause()")
                ACTION_NEXT -> executeJavaScript("window.TinyAmp?.next()")
                ACTION_PREVIOUS -> executeJavaScript("window.TinyAmp?.previous()")
                ACTION_SEEK -> {
                    val position = intent.getLongExtra("position", 0L)
                    executeJavaScript("window.TinyAmp?.seek($position)")
                }
            }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioPlaybackService.AudioBinder
            audioService = binder.getService()
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            audioService = null
            serviceBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Keep screen on during playback (optional, can be controlled from web)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Initialize views
        webView = findViewById(R.id.webView)
        loadingOverlay = findViewById(R.id.loadingOverlay)
        errorOverlay = findViewById(R.id.errorOverlay)

        // Request notification permission for Android 13+
        requestNotificationPermission()

        // Setup WebView
        setupWebView()

        // Register broadcast receiver for playback controls
        registerPlaybackReceiver()

        // Bind to audio service
        bindAudioService()

        // Load web app
        loadWebApp()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.apply {
            settings.apply {
                // Enable JavaScript for Three.js and web app
                javaScriptEnabled = true

                // Enable DOM storage for web app state
                domStorageEnabled = true

                // Allow media playback without user gesture (for native control)
                mediaPlaybackRequiresUserGesture = false

                // Enable hardware acceleration for Three.js WebGL
                setRenderPriority(WebSettings.RenderPriority.HIGH)

                // Cache settings for better performance
                cacheMode = WebSettings.LOAD_DEFAULT
                setAppCacheEnabled(true)

                // Enable viewport and responsive design
                useWideViewPort = true
                loadWithOverviewMode = true

                // Enable mixed content for Archive.org streaming
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                }

                // Database for Web SQL (if needed)
                databaseEnabled = true

                // Allow file access for local assets
                allowFileAccess = true
                allowContentAccess = true
            }

            // Add JavaScript interface for native bridge
            addJavascriptInterface(
                WebAppInterface(this@MainActivity, ::onPlaybackStateChange),
                "AndroidBridge"
            )

            // WebViewClient for URL handling
            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    hideLoading()

                    // Inject initialization script
                    injectInitScript()
                }

                override fun onReceivedError(
                    view: WebView?,
                    errorCode: Int,
                    description: String?,
                    failingUrl: String?
                ) {
                    super.onReceivedError(view, errorCode, description, failingUrl)
                    showError("Error loading app: $description")
                }
            }

            // WebChromeClient for console logging and debugging
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                    consoleMessage?.let {
                        android.util.Log.d(
                            "WebView",
                            "${it.message()} -- From line ${it.lineNumber()} of ${it.sourceId()}"
                        )
                    }
                    return true
                }

                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    super.onProgressChanged(view, newProgress)
                    if (newProgress < 100) {
                        showLoading("Loading: $newProgress%")
                    }
                }
            }
        }
    }

    private fun loadWebApp() {
        showLoading()

        // Load the web app from assets
        // For development, you can load a URL: webView.loadUrl("http://10.0.2.2:8080")
        webView.loadUrl("file:///android_asset/web/index.html")
    }

    private fun injectInitScript() {
        // Inject Android-specific initialization
        val initScript = """
            (function() {
                console.log('TinyAmp Android Bridge initialized');

                // Expose Android bridge to global scope
                window.AndroidBridge = {
                    play: (url, title, artist, albumArt) => {
                        Android.play(url, title || '', artist || '', albumArt || '');
                    },
                    pause: () => Android.pause(),
                    resume: () => Android.resume(),
                    stop: () => Android.stop(),
                    seek: (position) => Android.seek(position),
                    setVolume: (volume) => Android.setVolume(volume),
                    getPlaybackState: () => Android.getPlaybackState(),
                    updateMetadata: (title, artist, albumArt) => {
                        Android.updateMetadata(title, artist, albumArt);
                    },
                    sendAudioData: (dataJson) => {
                        Android.onAudioAnalysisData(dataJson);
                    },
                    showToast: (message) => Android.showToast(message)
                };

                // Notify web app that Android bridge is ready
                window.dispatchEvent(new Event('androidBridgeReady'));
            })();
        """.trimIndent()

        executeJavaScript(initScript)
    }

    private fun executeJavaScript(script: String) {
        runOnUiThread {
            webView.evaluateJavascript(script, null)
        }
    }

    private fun onPlaybackStateChange(
        state: String,
        title: String,
        artist: String,
        albumArt: String
    ) {
        // Update notification via service
        audioService?.updatePlaybackState(state, title, artist, albumArt)
    }

    private fun registerPlaybackReceiver() {
        val filter = IntentFilter().apply {
            addAction(ACTION_PLAY)
            addAction(ACTION_PAUSE)
            addAction(ACTION_NEXT)
            addAction(ACTION_PREVIOUS)
            addAction(ACTION_SEEK)
        }
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(playbackControlReceiver, filter)
    }

    private fun bindAudioService() {
        val intent = Intent(this, AudioPlaybackService::class.java)
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    private fun showLoading(message: String = getString(R.string.status_loading)) {
        runOnUiThread {
            loadingOverlay.visibility = View.VISIBLE
            errorOverlay.visibility = View.GONE
        }
    }

    private fun hideLoading() {
        runOnUiThread {
            loadingOverlay.visibility = View.GONE
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            loadingOverlay.visibility = View.GONE
            errorOverlay.visibility = View.VISIBLE
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        // Unregister receiver
        LocalBroadcastManager.getInstance(this)
            .unregisterReceiver(playbackControlReceiver)

        // Unbind service
        if (serviceBound) {
            unbindService(serviceConnection)
            serviceBound = false
        }

        // Cleanup WebView
        webView.destroy()
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            // Move to background instead of closing
            moveTaskToBack(true)
        }
    }

    companion object {
        private const val REQUEST_NOTIFICATION_PERMISSION = 1001

        const val ACTION_PLAY = "com.superintelligence.tinyamp.PLAY"
        const val ACTION_PAUSE = "com.superintelligence.tinyamp.PAUSE"
        const val ACTION_NEXT = "com.superintelligence.tinyamp.NEXT"
        const val ACTION_PREVIOUS = "com.superintelligence.tinyamp.PREVIOUS"
        const val ACTION_SEEK = "com.superintelligence.tinyamp.SEEK"
    }
}
