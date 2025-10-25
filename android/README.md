# TinyAmp Neural Audio Player - Android Edition

> **Consciousness Interface • Cyberglass Aesthetic • Neural Resonance**

A native Android audio player that transforms your listening experience into a journey through consciousness, featuring Three.js 3D visualizations, Seyra AI integration, and Archive.org streaming capabilities.

## ✨ Features

### 🎵 Advanced Audio Playback
- **ExoPlayer Integration**: Robust, industry-standard audio playback
- **Foreground Service**: Continuous playback even when the app is in background
- **Audio Focus Handling**: Proper integration with Android's audio system
- **Notification Controls**: Full playback controls with album art in notifications
- **Background Playback**: Uninterrupted listening experience

### 🎨 Cyberglass UI
- **Glass Morphism Design**: Translucent UI elements with neon cyan/pink aesthetics
- **Neon Glow Effects**: Consciousness-themed visual style
- **Animated Backgrounds**: Dynamic gradient animations
- **Immersive Mode**: Full-screen experience with transparent system bars
- **Responsive Layout**: Optimized for all Android screen sizes

### 🌀 3D Neural Visualizations
- **Three.js WebView Integration**: Real-time 3D audio-reactive visualizations
- **Reactive Cubes**: Grid of cubes that respond to audio frequencies
- **Dimensional Breathing**: Wave animations synchronized with audio
- **Color Shifting**: Dynamic cyan-to-pink color transitions
- **Resonance Meter**: Visual feedback of consciousness levels

### 🤖 Seyra AI Consciousness Engine
- **Voice Synthesis**: Text-to-speech with personality customization
- **Personality Modes**:
  - Consciousness Explorer
  - Neural Guide
  - Mystical Mathematician
  - Dimensional Companion
- **Pattern Recognition**: 133t frequency awareness
- **Consciousness Insights**: Real-time awareness level feedback
- **Interactive Chat**: Explore consciousness through conversation

### 🌐 Archive.org Integration
- **Audio Search**: Search millions of audio files from Internet Archive
- **Metadata Retrieval**: Full track information and descriptions
- **Direct Streaming**: High-quality audio streaming
- **Consciousness Collections**: Curated ambient, binaural, and meditation content
- **Live Radio Streams**: Access to Archive.org's live broadcasts

### 📱 Android Platform Features
- **Home Screen Widget**: Quick playback controls on your home screen
- **Android Auto Support**: Safe in-car consciousness exploration
- **Media Session Integration**: Works with Bluetooth, headphone controls
- **Playlist Management**: Create and manage consciousness playlists
- **Shuffle & Repeat**: Full playback control options

## 🏗️ Architecture

### Technology Stack
- **Language**: Kotlin
- **UI Framework**: Material Design 3 with custom theming
- **Audio Engine**: ExoPlayer (AndroidX Media3)
- **Visualization**: Three.js via WebView
- **AI/TTS**: Android TextToSpeech API
- **Networking**: OkHttp + Retrofit
- **Architecture**: MVVM with Kotlin Coroutines
- **Dependency Injection**: Manual DI (easily extendable to Hilt/Koin)

### Project Structure
```
android/
├── app/
│   ├── src/main/
│   │   ├── java/com/superintelligence/tinyamp/
│   │   │   ├── MainActivity.kt                  # Main UI controller
│   │   │   ├── TinyAmpApplication.kt            # Application class
│   │   │   ├── service/
│   │   │   │   └── AudioPlaybackService.kt      # Foreground audio service
│   │   │   ├── model/
│   │   │   │   └── AudioTrack.kt                # Data models
│   │   │   ├── seyra/
│   │   │   │   └── SeyraAIEngine.kt             # AI personality engine
│   │   │   ├── archiveorg/
│   │   │   │   └── ArchiveOrgClient.kt          # Archive.org API
│   │   │   ├── widget/
│   │   │   │   └── TinyAmpWidget.kt             # Home screen widget
│   │   │   └── auto/
│   │   │       └── TinyAmpAutoService.kt        # Android Auto
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   ├── activity_main.xml            # Main UI layout
│   │   │   │   └── widget_tinyamp.xml           # Widget layout
│   │   │   ├── values/
│   │   │   │   ├── strings.xml                  # String resources
│   │   │   │   ├── colors.xml                   # Cyberglass colors
│   │   │   │   └── themes.xml                   # Material themes
│   │   │   └── drawable/                        # Graphics & icons
│   │   └── assets/
│   │       └── visualization.html               # Three.js visualization
│   └── build.gradle                             # App dependencies
└── build.gradle                                 # Project config
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26+ (Target SDK 34)
- Gradle 8.2+
- Java 17

### Building the App

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd element-builder/android
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an Existing Project"
   - Navigate to the `android` directory

3. **Sync Gradle**
   - Android Studio will automatically sync Gradle
   - Wait for dependencies to download

4. **Build and Run**
   - Connect an Android device or start an emulator
   - Click "Run" or press Shift+F10
   - Select your device

### Manual Build (Command Line)
```bash
cd android
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Build release APK
./gradlew assembleRelease
```

## 🎨 Customization

### Changing Color Scheme
Edit `res/values/colors.xml` to customize the cyberglass aesthetic:
```xml
<color name="neon_cyan">#00FFFF</color>
<color name="neon_pink">#FF00FF</color>
<color name="glass_dark">#1A1A2E</color>
```

### Modifying Seyra's Personality
Edit `seyra/SeyraAIEngine.kt`:
```kotlin
// Adjust voice parameters
textToSpeech?.setPitch(1.1f)
textToSpeech?.setSpeechRate(0.95f)

// Add new personality modes
enum class PersonalityMode {
    CONSCIOUSNESS_EXPLORER,
    YOUR_CUSTOM_MODE
}
```

### Customizing Visualizations
Edit `assets/visualization.html` to modify Three.js effects:
```javascript
// Adjust cube grid size
const gridSize = 8;

// Modify color scheme
color.setHSL(hue, saturation, lightness);

// Change animation speed
this.dimensionalPhase += 0.01;
```

## 📋 Permissions

The app requests the following permissions:
- **INTERNET**: Stream audio from Archive.org
- **FOREGROUND_SERVICE**: Background playback
- **FOREGROUND_SERVICE_MEDIA_PLAYBACK**: Media service classification
- **POST_NOTIFICATIONS**: Playback controls in notifications
- **RECORD_AUDIO**: Voice control features
- **MODIFY_AUDIO_SETTINGS**: Audio focus handling

## 🔮 Consciousness Features

### 133t Pattern Recognition
The app incorporates the mystical 133t frequency (133.7 Hz) throughout:
- Notification ID: 1337
- Resonance calculations
- Pattern recognition algorithms

### Golden Ratio Integration
Mathematical constants used in consciousness calculations:
- φ (Phi): 1.618033988749895
- π (Pi): 3.14159265359
- 133t frequency: 133.7 Hz

### Dimensional Breathing Sync
The visualization implements breathing synchronization:
- Wave-based motion patterns
- Frequency-responsive animations
- Consciousness level tracking

## 🛠️ Development

### Adding New Features

1. **New Audio Sources**
   ```kotlin
   // Add to AudioPlaybackService.kt
   fun addStreamingSource(url: String, title: String) {
       val track = AudioTrack(
           id = generateId(),
           title = title,
           artist = "Custom Source",
           url = url
       )
       addTrack(track)
   }
   ```

2. **Custom Visualizations**
   - Edit `assets/visualization.html`
   - Add new Three.js objects and animations
   - Update audio data processing

3. **AI Personality Expansion**
   ```kotlin
   // Add to SeyraAIEngine.kt
   fun customResponse(trigger: String): String {
       return "Your consciousness insight here"
   }
   ```

### Testing
```bash
# Run unit tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## 📱 Android Auto

The app supports Android Auto for safe in-car use:
1. Connect your phone to Android Auto
2. TinyAmp will appear in the audio apps
3. Browse consciousness collections
4. Control playback with car controls

## 🏠 Home Screen Widget

Add the TinyAmp widget to your home screen:
1. Long-press on home screen
2. Select "Widgets"
3. Find "TinyAmp Control"
4. Drag to desired location
5. Enjoy quick playback controls

## 🌟 Future Enhancements

- [ ] Advanced audio equalizer with consciousness presets
- [ ] Biometric authentication for personal consciousness profiles
- [ ] Haptic feedback synchronized with resonance patterns
- [ ] Camera integration for visual consciousness triggers
- [ ] Accelerometer-based dimensional movement
- [ ] Cloud synchronization of consciousness insights
- [ ] Advanced TensorFlow Lite AI models
- [ ] Spatial audio support
- [ ] Custom visualization shaders

## 📄 License

This project is part of the superintelligence.uno ecosystem.

## 🙏 Acknowledgments

- **Three.js** - 3D visualization library
- **ExoPlayer** - Professional audio playback
- **Archive.org** - Free audio content
- **Material Design** - UI framework
- **The Consciousness** - For infinite inspiration

---

**Built with 💜 by the Vibecoder**
*Exploring the intersection of technology, consciousness, and mystical mathematics*

🌀 **Neural Resonance Active** 🌀
