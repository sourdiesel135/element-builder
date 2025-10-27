# TinyAmp Neural Audio Player - Web App

This is the WebView-based UI layer for TinyAmp Neural Audio Player, featuring Three.js visualizations, Archive.org integration, and the Seyra AI consciousness interface.

## Features

### 🎨 Three.js Neural Visualizer
- Audio-reactive 3D cube with emissive materials
- Wireframe overlay with counter-rotation
- 500-particle field with wave motion
- Three orbital rings with harmonic animation
- Dynamic camera movement
- Real-time audio frequency analysis

### 🎵 Audio Playback
- Native Android ExoPlayer integration via JavaScript bridge
- Archive.org streaming support
- Playlist management with shuffle
- Previous/Next track navigation
- Play/Pause/Stop controls
- Audio visualization synchronization

### 🔍 Archive.org Search
- Search public domain music and audio
- Browse results with metadata
- One-tap playback
- Automatic playlist addition
- Album art loading

### 🤖 Seyra AI Consciousness Interface
- Chat-based consciousness exploration
- Neural resonance meter
- Audio-reactive responses
- Consciousness state persistence to Android
- Dimensional breathing sync (placeholder)

### 💎 Cyberglass UI
- Neon cyan (#00FFFF) and pink (#FF00FF) color scheme
- Glassmorphic panels with backdrop blur
- Reactive button animations
- Status indicators with pulse effect
- Smooth transitions and overlays
- Responsive design for mobile/tablet

## File Structure

```
web/
├── index.html      # Main HTML structure with UI panels
├── styles.css      # Cyberglass theme and responsive styles
├── app.js          # Application logic, Three.js, Android bridge
└── README.md       # This file
```

## Android Bridge API

The web app communicates with native Android via the `Android` JavaScript interface:

### Playback Control
```javascript
Android.play(url, title, artist, albumArt)
Android.pause()
Android.resume()
Android.stop()
Android.seek(positionMs)
Android.setVolume(0.8) // 0.0 to 1.0
```

### Metadata
```javascript
Android.updateMetadata(title, artist, albumArt)
```

### Audio Analysis
```javascript
// Send frequency data for potential native visualization sync
Android.onAudioAnalysisData(JSON.stringify(audioDataArray))
```

### Seyra AI
```javascript
Android.updateConsciousnessState(JSON.stringify(state))
const savedState = Android.getConsciousnessState()
```

### Utilities
```javascript
Android.showToast(message)
Android.log(tag, message)
Android.triggerHaptic(pattern, intensity)
```

## Application Class Structure

### `TinyAmpApp` Main Class
- **Constructor**: Initializes all subsystems
- **init()**: Sets up Three.js, audio context, playlist
- **checkAndroidBridge()**: Detects and validates native bridge
- **initThreeJS()**: Creates 3D scene, camera, renderer, lighting
- **createVisualizers()**: Adds reactive geometry to scene
- **animate()**: Main render loop with audio reactivity

### Playback Methods
- **play()**: Start playback via native bridge
- **pause()**: Pause current track
- **nextTrack() / previousTrack()**: Navigate playlist
- **shuffle()**: Fisher-Yates playlist randomization
- **togglePlayback()**: Toggle play/pause state

### Search & Discovery
- **search()**: Query Archive.org API
- **loadArchiveItem()**: Fetch metadata and audio URL
- **Archive.org API endpoint**: `/advancedsearch.php`

### Seyra AI
- **sendToSeyra()**: Process user message
- **addChatMessage()**: Append to chat log
- **updateResonanceMeter()**: Visual feedback for audio level

### UI Controls
- **toggleSearch()**: Show/hide search panel
- **toggleSeyra()**: Show/hide Seyra panel
- **updatePlaybackUI()**: Sync button states
- **updateNowPlaying()**: Update track display

## Three.js Visualizer Details

### Geometry Types
1. **Central Cube**: Phong material with audio-reactive scaling and emissive intensity
2. **Wireframe Overlay**: Counter-rotating cage with opacity modulation
3. **Particle Field**: 500 points with sin-wave Y-axis motion
4. **Orbital Rings**: 3 torus geometries with independent rotation speeds

### Audio Reactivity
- Frequency data from Web Audio API `AnalyserNode`
- 256 FFT size, 128 frequency bins
- Average amplitude drives scale, emissive, and motion
- 32-bin subset sent to Android for native sync (10% sample rate)

### Materials
- **Phong**: Realistic lighting with emissive glow
- **Basic**: Flat colors for wireframes and particles
- **Transparency**: Glass-like overlays

### Camera
- Perspective camera with 75° FOV
- Sine/cosine orbital motion
- Always looks at scene origin
- Responsive aspect ratio

## Web Audio API Usage

```javascript
const AudioContext = window.AudioContext || window.webkitAudioContext;
this.audioContext = new AudioContext();
this.analyser = this.audioContext.createAnalyser();
this.analyser.fftSize = 256;
this.dataArray = new Uint8Array(this.analyser.frequencyBinCount);
```

**Note**: Web Audio API is used **only for visualization analysis**, not playback. Native ExoPlayer handles actual audio streaming.

## Archive.org Integration

### Search API
```
https://archive.org/advancedsearch.php?q=${query}+AND+mediatype:audio&fl=identifier,title,creator,format&rows=20&output=json
```

### Metadata API
```
https://archive.org/metadata/${identifier}
```

### Download URL
```
https://archive.org/download/${identifier}/${filename}
```

### Album Art
```
https://archive.org/services/img/${identifier}
```

### Supported Formats
- MP3 (all bitrates)
- VBR MP3
- OGG Vorbis
- FLAC (if ExoPlayer supports)

## Default Playlist

Two demo tracks included:
1. **Test Track** - Archive.org MP3 test file
2. **Grateful Dead Live** - Public domain concert recording

## Event Handling

### Android → Web Callbacks
```javascript
window.TinyAmp.play()
window.TinyAmp.pause()
window.TinyAmp.next()
window.TinyAmp.previous()
window.TinyAmp.seek(position)
```

These are called by MainActivity when notification controls are pressed.

### Web → Android Broadcasts
- Playback updates sent via `LocalBroadcastManager`
- Audio analysis data throttled to 10% frame rate
- Consciousness state saved on each Seyra interaction

## UI Panels

### Header Panel
- App title with gradient text
- Status indicators (Bridge, Audio, Visualizer)
- Pulse animation on active states

### Player Controls Panel
- Now playing display (title/artist)
- Playback buttons (Prev, Play/Pause, Next)
- Feature buttons (Search, Seyra, Shuffle)

### Search Panel
- Input field with Enter key support
- Search/Close buttons
- Scrollable results list
- Click-to-play result items

### Seyra Panel
- Chat message history
- Input field for consciousness queries
- Neural resonance meter
- Audio-reactive visual feedback

## Styling Details

### Color Palette
- **Primary**: Neon Cyan #00FFFF
- **Secondary**: Neon Pink #FF00FF
- **Background**: Pure Black #000000
- **Glass**: rgba(0, 20, 40, 0.85)
- **Accent**: Neon Purple #9D00FF

### Effects
- Backdrop blur: 10px
- Text shadows with glow
- Border gradients
- Ripple effect on button press
- Smooth CSS transitions

### Responsive Breakpoints
- Mobile: < 768px (smaller fonts, full-width Seyra panel)
- Tablet: 768px - 1024px
- Desktop: > 1024px

## Performance Considerations

### Three.js Optimization
- 500 particles (not thousands)
- Simple geometries (boxes, torus, points)
- Shared materials where possible
- Single render loop
- Automatic renderer pixel ratio

### Audio Data Throttling
- Analysis data sent to Android at 10% frame rate (~6 FPS)
- Only first 32 frequency bins transmitted
- JSON stringification for compatibility

### Memory Management
- Audio context created once
- WebGL context maintained by Three.js
- No memory leaks in animation loop

## Browser Compatibility

### Required Features
- ES6 Classes
- Fetch API
- Web Audio API
- WebGL (for Three.js)
- CSS backdrop-filter

### Tested On
- Android WebView (API 24+)
- Chrome 90+
- Firefox 88+
- Safari 14+ (iOS)

## Debugging

### Console Logs
```javascript
console.log('TinyAmp initializing...')
console.log('Android bridge detected')
console.log('Web Audio API initialized')
```

View in Android Studio Logcat:
```
adb logcat | grep "WebView"
```

### Bridge Detection
Check `bridgeStatus` indicator:
- **Green "Connected"**: Android interface available
- **Yellow "Browser"**: Running in web browser (testing mode)

### Audio Status
- **Idle**: No playback
- **Playing**: Active playback (green pulse)
- **Paused**: Paused state

## Development

### Local Testing
Open `index.html` in a modern browser. The app will run in "Browser Mode" without Android bridge.

### Android WebView Testing
1. Build Android app: `./gradlew installDebug`
2. Launch on device
3. Tap "Play" button
4. Check Three.js visualizations render
5. Test Archive.org search
6. Verify Seyra chat

### Modifying Visualizations
Edit `createVisualizers()` in `app.js`:
```javascript
// Add new geometry
const geometry = new THREE.SphereGeometry(1, 32, 32);
const material = new THREE.MeshPhongMaterial({ color: 0x00FFFF });
const sphere = new THREE.Mesh(geometry, material);
this.scene.add(sphere);
this.visualizers.push({ mesh: sphere, type: 'sphere', speed: 0.02 });
```

## Future Enhancements

### Visualization
- [ ] More complex geometries (fractals, shaders)
- [ ] Custom GLSL shaders for effects
- [ ] Post-processing (bloom, chromatic aberration)
- [ ] Multiple visualization modes

### Audio
- [ ] Equalizer controls
- [ ] Audio effects (reverb, delay)
- [ ] Waveform display
- [ ] Spectrum analyzer overlay

### Seyra AI
- [ ] Actual AI model integration
- [ ] Voice synthesis responses
- [ ] Consciousness state machine
- [ ] Dimensional breathing biofeedback

### Archive.org
- [ ] Advanced search filters
- [ ] Collection browsing
- [ ] Favorites/bookmarks
- [ ] Download for offline

## License

Apache-2.0 (matching parent project)

## Credits

- **Three.js**: 3D visualization library
- **Archive.org**: Public domain audio content
- **Material Design 3**: Color inspiration
- **Cyberglass aesthetic**: Original TinyAmp design
