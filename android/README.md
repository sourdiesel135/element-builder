# TinyAmp Neural Audio Player - Android Implementation

## Overview

This is a hybrid Android application that combines native audio playback (ExoPlayer) with WebView-based Three.js visualizations. The architecture preserves all existing web features while providing robust native audio handling for background playback, media controls, and Android system integration.

## Architecture

### Core Components

```
┌─────────────────────────────────────────────────────────────┐
│                      MainActivity                            │
│  ┌────────────────────────────────────────────────────┐    │
│  │              WebView (Three.js UI)                  │    │
│  │  - Cyberglass visualization interface               │    │
│  │  - Seyra AI consciousness chat                      │    │
│  │  - Archive.org integration                          │    │
│  └─────────────────┬──────────────────────────────────┘    │
│                    │ JavaScript Bridge                      │
│                    ▼                                         │
│         ┌──────────────────────┐                            │
│         │  WebAppInterface     │                            │
│         └──────────┬───────────┘                            │
└────────────────────┼────────────────────────────────────────┘
                     │ Intent Communication
                     ▼
┌─────────────────────────────────────────────────────────────┐
│              AudioPlaybackService                            │
│  ┌────────────────────────────────────────────────────┐    │
│  │  ExoPlayer                                          │    │
│  │  - Streaming audio playback                        │    │
│  │  - Buffer management                                │    │
│  │  - Audio session control                            │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────┐    │
│  │  MediaSession                                       │    │
│  │  - Background playback                              │    │
│  │  - System media controls                            │    │
│  │  - Lock screen controls                             │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────┐    │
│  │  AudioFocusManager                                  │    │
│  │  - Audio focus handling                             │    │
│  │  - Ducking support                                  │    │
│  └────────────────────────────────────────────────────┘    │
│  ┌────────────────────────────────────────────────────┐    │
│  │  Notification                                       │    │
│  │  - Media controls (play, pause, next, prev)        │    │
│  │  - Album art display                                │    │
│  │  - Foreground service indicator                     │    │
│  └────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
```

### Key Features Implemented

✅ **Native Audio Playback**
- ExoPlayer for robust streaming audio
- Support for various formats (MP3, AAC, OGG, etc.)
- Efficient buffering and network handling
- Low-latency playback

✅ **Background Playback**
- Foreground service with media notification
- Survives app backgrounding
- Battery-efficient implementation
- MediaSession integration for system controls

✅ **WebView Integration**
- Hardware-accelerated WebGL for Three.js
- JavaScript bridge for bidirectional communication
- Preserved cyberglass aesthetic
- Real-time visualization support

✅ **Audio Focus Management**
- Automatic pause/resume on interruptions
- Volume ducking for notifications
- Proper audio session handling

✅ **Media Controls**
- Lock screen controls
- Notification controls (play, pause, next, previous)
- Bluetooth/headphone controls support
- MediaSession callbacks

## Project Structure

```
android/
├── app/
│   ├── build.gradle.kts              # App-level dependencies
│   ├── proguard-rules.pro            # ProGuard configuration
│   └── src/main/
│       ├── AndroidManifest.xml       # App manifest with permissions
│       ├── assets/
│       │   └── web/
│       │       └── index.html        # WebView content (Three.js UI)
│       ├── java/com/superintelligence/tinyamp/
│       │   ├── TinyAmpApplication.kt # Application class
│       │   ├── ui/
│       │   │   └── MainActivity.kt   # Main activity with WebView
│       │   ├── service/
│       │   │   └── AudioPlaybackService.kt  # Foreground service
│       │   ├── bridge/
│       │   │   └── WebAppInterface.kt       # JS bridge
│       │   └── players/
│       │       └── AudioFocusManager.kt     # Audio focus
│       └── res/
│           ├── layout/
│           │   └── activity_main.xml        # Main layout
│           ├── values/
│           │   ├── colors.xml       # Cyberglass color scheme
│           │   ├── strings.xml      # App strings
│           │   └── themes.xml       # Material3 dark theme
│           ├── drawable/            # Vector icons
│           └── xml/                 # Backup rules
├── build.gradle.kts                 # Project-level build script
├── settings.gradle.kts              # Gradle settings
└── gradle.properties                # Gradle properties
```

## Building the Project

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK 34
- Gradle 8.1+

### Build Steps

1. **Open in Android Studio**
   ```bash
   cd android
   # Open this directory in Android Studio
   ```

2. **Sync Gradle**
   - Android Studio will automatically sync dependencies
   - Wait for all dependencies to download

3. **Build the APK**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Install on Device**
   ```bash
   ./gradlew installDebug
   ```

### Running the App

**From Android Studio:**
- Click the "Run" button (Shift + F10)
- Select your device/emulator

**From Command Line:**
```bash
# Build and install
./gradlew installDebug

# Launch on connected device
adb shell am start -n com.superintelligence.tinyamp/.ui.MainActivity
```

## JavaScript Bridge API

### Web → Native Communication

The web app can control native playback using the `Android` or `AndroidBridge` interface:

```javascript
// Play audio (switches to native ExoPlayer)
Android.play(url, title, artist, albumArtUrl);

// Playback control
Android.pause();
Android.resume();
Android.stop();
Android.seek(positionMs);
Android.setVolume(0.8); // 0.0 to 1.0

// Metadata updates
Android.updateMetadata(title, artist, albumArtUrl);

// Send audio analysis data (for sync)
Android.onAudioAnalysisData(JSON.stringify(audioData));

// Utility
Android.showToast("Message");
Android.log("Tag", "Message");

// Consciousness state (Seyra AI)
Android.updateConsciousnessState(JSON.stringify(state));
const state = Android.getConsciousnessState();

// Haptic feedback
Android.triggerHaptic("pulse", 0.5);
```

### Native → Web Communication

The native service can call JavaScript functions in the WebView:

```kotlin
// From MainActivity or Service
webView.evaluateJavascript("""
    window.TinyAmp.play();
    window.TinyAmp.pause();
    window.TinyAmp.next();
    window.TinyAmp.previous();
    window.TinyAmp.seek(positionMs);
""", null)
```

### Event Handling

The web app receives events via `LocalBroadcastManager`:

```javascript
// Listen for playback updates
// (Implement via injected JavaScript in MainActivity)
window.addEventListener('playbackUpdate', (event) => {
    console.log('Position:', event.detail.position);
    console.log('Duration:', event.detail.duration);
    console.log('Is Playing:', event.detail.isPlaying);
});
```

## Service Actions

The `AudioPlaybackService` responds to these Intent actions:

```kotlin
// Play a track
Intent(context, AudioPlaybackService::class.java).apply {
    action = AudioPlaybackService.ACTION_PLAY
    putExtra("url", "https://...")
    putExtra("title", "Track Title")
    putExtra("artist", "Artist Name")
    putExtra("albumArt", "https://...")
}

// Control actions
ACTION_PAUSE    // Pause playback
ACTION_RESUME   // Resume playback
ACTION_STOP     // Stop and cleanup
ACTION_NEXT     // Skip to next (broadcasts to WebView)
ACTION_PREVIOUS // Skip to previous (broadcasts to WebView)
ACTION_SEEK     // Seek to position (extra: "position")
```

## Permissions

The app requires these permissions (declared in AndroidManifest.xml):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PLAYBACK" />
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
```

## Notification Controls

The foreground service notification includes:
- **Album art** (loaded asynchronously)
- **Track title and artist**
- **Play/Pause button**
- **Previous button**
- **Next button**
- **Tap to open app**

## Audio Focus Behavior

| Event                              | Behavior                        |
|------------------------------------|---------------------------------|
| Phone call incoming                | Pause playback                  |
| Phone call ends                    | Resume if user was playing      |
| Notification sound                 | Duck volume to 30%              |
| Another music app starts           | Pause playback                  |
| User starts TinyAmp                | Request and gain audio focus    |

## Integrating Your Three.js Visualizations

Replace `/android/app/src/main/assets/web/index.html` with your full TinyAmp web app:

1. **Copy your web app files to `/assets/web/`**:
   ```
   assets/web/
   ├── index.html
   ├── js/
   │   ├── three.min.js
   │   ├── visualizer.js
   │   └── seyra-ai.js
   ├── css/
   │   └── styles.css
   └── assets/
       └── (textures, models, etc.)
   ```

2. **Modify your audio playback code** to use the Android bridge:
   ```javascript
   // Instead of Web Audio API playback:
   // audioElement.play()

   // Use native playback:
   if (typeof Android !== 'undefined') {
       Android.play(url, title, artist, albumArt);
   }
   ```

3. **Send audio data to native** (optional, for visualization sync):
   ```javascript
   const analyser = audioContext.createAnalyser();
   const dataArray = new Uint8Array(analyser.frequencyBinCount);

   function sendAudioData() {
       analyser.getByteFrequencyData(dataArray);
       Android.onAudioAnalysisData(JSON.stringify(Array.from(dataArray)));
       requestAnimationFrame(sendAudioData);
   }
   ```

4. **Receive playback position updates**:
   ```javascript
   // The service broadcasts position updates
   // Listen for them in your visualizer code
   ```

## Testing

### Test Audio Playback

The placeholder web app includes a test button that plays a sample MP3 from Archive.org.

1. Launch the app
2. Wait for "Bridge: Connected ✓" indicator
3. Tap "Test Playback"
4. Verify:
   - Notification appears
   - Audio plays
   - Notification controls work
   - App survives backgrounding

### Test Background Playback

1. Start playback
2. Press Home button
3. Verify notification remains
4. Test notification controls
5. Return to app via notification tap

### Test Audio Focus

1. Start playback
2. Play audio from another app (YouTube, Spotify)
3. Verify TinyAmp pauses
4. Stop other app
5. Resume TinyAmp manually

## Debugging

### WebView Console Logs

View WebView console output in Android Studio Logcat:
```
Logcat filter: "WebView"
```

### Service State

Monitor service lifecycle:
```
Logcat filter: "AudioPlaybackService"
```

### Bridge Communication

See bridge calls:
```
Logcat filter: "WebAppInterface"
```

## Known Limitations

1. **Real-time Audio Analysis**: ExoPlayer doesn't expose raw PCM data easily. For Web Audio API-level frequency data, you may need to:
   - Use Android's `Visualizer` class (limited)
   - Implement custom ExoPlayer renderer with FFT
   - Stick with Web Audio API in WebView (loses native playback benefits)

2. **WebView Performance**: Complex Three.js scenes may have lower FPS on older devices

3. **Album Art Loading**: Large images may take time to download for notifications

## Next Steps

### Recommended Enhancements

1. **Room Database** - Persist playlists and consciousness states
2. **WorkManager** - Background sync for AI data
3. **Android Auto** - Add MediaBrowserService for car integration
4. **Home Screen Widget** - Quick access playback controls
5. **Wear OS** - Companion app for smartwatches
6. **Chromecast** - Stream to external devices
7. **Equalizer** - Native audio effects
8. **Sleep Timer** - Auto-stop playback

### Development Workflow

1. Modify web assets in `/assets/web/`
2. Rebuild app: `./gradlew installDebug`
3. Test on device
4. For rapid web development, load from local server:
   ```kotlin
   // In MainActivity.kt
   webView.loadUrl("http://10.0.2.2:8080") // Android emulator
   // or
   webView.loadUrl("http://YOUR_IP:8080") // Physical device
   ```

## Deployment

### Generate Release APK

1. Create a keystore:
   ```bash
   keytool -genkey -v -keystore tinyamp-release.jks \
     -keyalg RSA -keysize 2048 -validity 10000 \
     -alias tinyamp
   ```

2. Configure `app/build.gradle.kts`:
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("tinyamp-release.jks")
               storePassword = "your-password"
               keyAlias = "tinyamp"
               keyPassword = "your-password"
           }
       }
       buildTypes {
           release {
               signingConfig = signingConfigs.getByName("release")
           }
       }
   }
   ```

3. Build release APK:
   ```bash
   ./gradlew assembleRelease
   ```

4. APK location: `app/build/outputs/apk/release/app-release.apk`

### Google Play Store

Follow the standard Android app publishing process:
1. Create developer account
2. Create app listing
3. Upload APK/AAB
4. Complete store listing (description, screenshots, etc.)
5. Submit for review

## License

Apache-2.0 (matching the original element-builder license)

## Support

For issues specific to the Android implementation:
- Check Android Studio Logcat for errors
- Verify all permissions are granted
- Test on Android 7.0+ (API 24+)
- Ensure WebView is up to date on device

## Credits

- **ExoPlayer** - Google's media playback library
- **Three.js** - 3D visualization library
- **Material Design 3** - UI components
- **Cyberglass aesthetic** - Original TinyAmp design
