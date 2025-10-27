# TinyAmp Android WebView Audio Integration - Implementation Summary

## What Was Implemented

This implementation provides a **hybrid Android app** that combines:
- **Native audio playback** using ExoPlayer (robust, efficient, background-capable)
- **WebView-based UI** for Three.js visualizations and cyberglass interface
- **JavaScript bridge** for bidirectional communication between web and native layers
- **Foreground service** with media notification and controls
- **Audio focus management** for proper Android ecosystem integration

## Architecture Decision: Why This Approach?

### The Challenge
Your TinyAmp Neural Audio Player has sophisticated Three.js visualizations and a unique cyberglass UI. Simply converting everything to native Android would mean:
- ❌ Rewriting all Three.js visualization code in OpenGL/Vulkan
- ❌ Recreating the cyberglass aesthetic in Android XML/Compose
- ❌ Porting the Seyra AI consciousness interface
- ❌ Losing the web-based development velocity

### The Solution: Hybrid Architecture
We preserve the web stack for UI while using native code for audio:
- ✅ WebView renders your existing Three.js visualizations
- ✅ Cyberglass UI preserved exactly as designed
- ✅ ExoPlayer handles audio (more robust than Web Audio API for streaming)
- ✅ Background playback with system integration
- ✅ Best of both worlds: web UI flexibility + native audio power

## Key Components

### 1. MainActivity (`MainActivity.kt`)
**Location**: `android/app/src/main/java/com/superintelligence/tinyamp/ui/MainActivity.kt`

**Responsibilities**:
- Hosts the WebView with hardware acceleration for Three.js
- Configures WebView settings (JavaScript, DOM storage, media playback)
- Injects the JavaScript bridge (`AndroidBridge` interface)
- Handles playback control broadcasts from the notification
- Binds to `AudioPlaybackService`
- Manages activity lifecycle and back button behavior

**Key Features**:
```kotlin
- WebView with JavaScript enabled
- Hardware acceleration for WebGL
- JavaScript interface: "AndroidBridge"
- Loads web app from: file:///android_asset/web/index.html
- Receives notification control intents
- Injects initialization script for bridge
```

### 2. AudioPlaybackService (`AudioPlaybackService.kt`)
**Location**: `android/app/src/main/java/com/superintelligence/tinyamp/service/AudioPlaybackService.kt`

**Responsibilities**:
- Manages ExoPlayer lifecycle
- Runs as foreground service for background playback
- Creates and updates media notification
- Handles MediaSession for system controls
- Manages audio focus via `AudioFocusManager`
- Broadcasts playback state to WebView

**Key Features**:
```kotlin
- ExoPlayer for streaming audio
- MediaSessionCompat for lock screen controls
- Notification with play/pause/next/previous buttons
- Album art loading from URLs
- Audio focus handling (pause on interruption, duck for notifications)
- State synchronization with WebView via LocalBroadcastManager
```

### 3. WebAppInterface (`WebAppInterface.kt`)
**Location**: `android/app/src/main/java/com/superintelligence/tinyamp/bridge/WebAppInterface.kt`

**Responsibilities**:
- Provides JavaScript-accessible methods for native control
- Bridges web app audio commands to native service
- Handles metadata updates
- Manages consciousness state (Seyra AI integration)
- Provides utility functions (toast, logging, haptics)

**JavaScript API**:
```javascript
Android.play(url, title, artist, albumArt)
Android.pause()
Android.resume()
Android.stop()
Android.seek(positionMs)
Android.setVolume(volume)
Android.updateMetadata(title, artist, albumArt)
Android.onAudioAnalysisData(jsonData)
Android.updateConsciousnessState(jsonState)
Android.getConsciousnessState()
Android.showToast(message)
Android.log(tag, message)
```

### 4. AudioFocusManager (`AudioFocusManager.kt`)
**Location**: `android/app/src/main/java/com/superintelligence/tinyamp/players/AudioFocusManager.kt`

**Responsibilities**:
- Requests audio focus before playback
- Handles audio focus changes (loss, transient loss, ducking)
- Ensures TinyAmp plays nicely with other audio apps
- Manages volume ducking for notifications

**Behavior**:
- **GAIN**: Resume playback, restore full volume
- **LOSS**: Pause playback (another app took focus)
- **LOSS_TRANSIENT**: Pause temporarily (phone call)
- **LOSS_TRANSIENT_CAN_DUCK**: Lower volume to 30% (notification)

### 5. Web Assets (`index.html`)
**Location**: `android/app/src/main/assets/web/index.html`

**Current State**: Placeholder with basic cyberglass UI and test functionality

**Your Integration**: Replace this with your full TinyAmp web app:
```
assets/web/
├── index.html          # Main entry point
├── js/
│   ├── three.min.js    # Three.js library
│   ├── visualizer.js   # Your visualizations
│   └── seyra-ai.js     # Consciousness interface
├── css/
│   └── cyberglass.css  # Your styling
└── assets/             # Textures, models, etc.
```

## How the Communication Flow Works

### Web → Native (Play Audio)
```
1. User clicks play in WebView UI
2. JavaScript calls: Android.play(url, title, artist, albumArt)
3. WebAppInterface receives call
4. Creates Intent for AudioPlaybackService
5. Service starts ExoPlayer with track URL
6. Service creates/updates notification
7. Audio plays natively
```

### Native → Web (Notification Control)
```
1. User taps "Next" in notification
2. Service sends broadcast: ACTION_NEXT
3. MainActivity receives broadcast
4. MainActivity executes: webView.evaluateJavascript("window.TinyAmp.next()")
5. Web app advances to next track
6. Web app calls: Android.play(nextUrl, ...)
7. Cycle continues
```

### Background Playback
```
1. Audio is playing
2. User presses Home button
3. MainActivity onPause() called
4. AudioPlaybackService keeps running (foreground service)
5. ExoPlayer continues playback
6. Notification remains visible with controls
7. User can control playback from notification
8. Tapping notification brings app back
```

## Building and Running

### Quick Start
```bash
cd android
./gradlew installDebug

# Or open in Android Studio and click Run
```

### Requirements
- Android Studio Hedgehog (2023.1.1+)
- JDK 17
- Android SDK 34
- Gradle 8.1+
- Device/Emulator running Android 7.0+ (API 24+)

### Test the Implementation
1. Launch app
2. Wait for "Bridge: Connected" indicator
3. Tap "Test Playback" button
4. Verify:
   - Audio plays
   - Notification appears with controls
   - Notification controls work
   - App can be backgrounded
   - Playback continues in background

## Integration Checklist

To integrate your full TinyAmp app:

- [ ] Copy your web app files to `android/app/src/main/assets/web/`
- [ ] Update audio playback code to use `Android.play()` instead of Web Audio API
- [ ] Test Three.js visualizations in Android WebView
- [ ] Implement playlist next/previous logic in web app
- [ ] Update `window.TinyAmp.next()` and `.previous()` callbacks
- [ ] Test Seyra AI consciousness interface
- [ ] Verify Archive.org streaming works
- [ ] Add launcher icons (currently placeholders)
- [ ] Test on multiple Android versions
- [ ] Profile battery usage during extended playback
- [ ] Test audio focus with other apps
- [ ] Verify notification controls in all states

## Key Files Reference

```
android/
├── app/
│   ├── build.gradle.kts              # Dependencies (ExoPlayer, Media3, etc.)
│   ├── src/main/
│   │   ├── AndroidManifest.xml       # Permissions & service declarations
│   │   ├── assets/web/index.html     # 👈 Your web app goes here
│   │   ├── java/.../
│   │   │   ├── ui/MainActivity.kt            # WebView host
│   │   │   ├── service/AudioPlaybackService.kt  # Audio engine
│   │   │   ├── bridge/WebAppInterface.kt     # JS bridge
│   │   │   └── players/AudioFocusManager.kt  # Focus handling
│   │   └── res/
│   │       ├── values/colors.xml     # Cyberglass colors
│   │       ├── values/themes.xml     # Dark Material3 theme
│   │       └── drawable/ic_*.xml     # Notification icons
│   └── proguard-rules.pro            # Keep WebView interface
├── build.gradle.kts                  # Project config
└── README.md                         # Full documentation
```

## Technical Highlights

### ExoPlayer Configuration
- Supports HTTP/HTTPS streaming (Archive.org compatible)
- Automatic buffer management
- Network error handling with retries
- Multiple codec support (MP3, AAC, OGG, FLAC, etc.)

### WebView Configuration
```kotlin
- JavaScript enabled (required for Three.js)
- Hardware acceleration (required for WebGL)
- DOM storage (for app state)
- Mixed content allowed (HTTP + HTTPS)
- Media playback without gesture
- JavaScript interface injection
```

### Notification Features
- Album art (async loaded from URLs)
- Three action buttons (prev, play/pause, next)
- Tap to open app
- MediaStyle for lock screen
- Survives app closure
- Updates dynamically

### Audio Focus Handling
- Requests AUDIOFOCUS_GAIN for music playback
- Pauses on permanent focus loss (other music app)
- Pauses on transient loss (phone call)
- Ducks to 30% volume on can-duck loss (notification)
- Restores volume when focus regained

## Troubleshooting

### "Bridge not available" in WebView
- Check `AndroidManifest.xml` has JavaScript enabled
- Verify `addJavascriptInterface` is called in `MainActivity`
- Check Logcat for WebView errors

### Audio not playing
- Verify INTERNET permission granted
- Check URL is accessible (test in browser)
- Look for ExoPlayer errors in Logcat (filter: "AudioPlaybackService")
- Ensure device has network connectivity

### Notification not showing
- Request POST_NOTIFICATIONS permission (Android 13+)
- Check foreground service is started
- Verify notification channel created

### WebGL not working
- Ensure `hardwareAccelerated="true"` in manifest
- Check device supports WebGL
- Test on physical device (emulator WebGL can be unreliable)

## Performance Considerations

### Battery Usage
- Foreground service with WAKE_LOCK keeps app alive
- ExoPlayer is optimized for streaming efficiency
- WebView rendering in background can use CPU
- Consider pausing Three.js animations when backgrounded

### Memory
- Album art bitmaps cached in service
- WebView JS engine memory
- ExoPlayer buffer size (configurable)
- Monitor with Android Profiler

### Network
- ExoPlayer handles adaptive streaming
- Buffers appropriately for connection type
- Retries on network errors
- Archive.org streams should work well

## Next Steps

1. **Integration**: Copy your web app to `assets/web/`
2. **Testing**: Test all features on physical device
3. **Polish**: Add proper launcher icons
4. **Optimize**: Profile and optimize battery/memory
5. **Features**: Add Room database for playlists
6. **Deploy**: Generate release APK for distribution

## Support

See `android/README.md` for comprehensive documentation including:
- Detailed architecture diagrams
- Complete API reference
- Build instructions
- Deployment guide
- Enhancement recommendations

---

**Created**: 2025-10-27
**Status**: ✅ Fully implemented and documented
**Ready for**: Integration testing with full TinyAmp web app
