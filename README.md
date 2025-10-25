# TinyAMP - A Minimalist Music Player for Android

TinyAMP is a lightweight, modern music player for Android built with Kotlin and following Material Design 3 guidelines. It provides a clean, intuitive interface for playing local audio files with essential playback controls.

## Features

### Core Functionality
- Local audio file playback (MP3, FLAC, OGG, and other formats)
- Background audio playback with foreground service
- Lock screen and notification controls
- Automatic audio file scanning from device storage
- Album art display with fallback icons

### Playback Controls
- Play/Pause
- Next/Previous track
- Seek bar for navigation
- Shuffle mode
- Repeat modes (Off, All, One)

### User Interface
- Material Design 3 theming
- Dark and light theme support (follows system settings)
- RecyclerView-based song list with smooth scrolling
- Bottom player controls with persistent playback state
- Search functionality (planned)
- Empty state and loading indicators

## Architecture

TinyAMP follows modern Android development best practices with a clean architecture:

### MVVM Pattern
```
app/src/main/java/com/tinyamp/player/
├── data/
│   ├── models/          # Data models (Song, PlaybackState, Playlist)
│   └── repository/      # Repository pattern for data access
├── ui/
│   ├── main/           # Main activity and song adapter
│   └── player/         # Player-related UI components (future)
├── service/            # Background music service
├── utils/              # Utilities (AudioScanner, PermissionHelper)
└── viewmodel/          # ViewModels for UI state management
```

### Key Components

#### Data Layer
- **Song**: Represents an audio file with metadata (title, artist, album, duration)
- **PlaybackState**: Tracks current playback status and position
- **MusicRepository**: Provides data access abstraction for audio files

#### Service Layer
- **MusicService**: MediaSessionService for background playback
- Uses ExoPlayer (Media3) for robust audio playback
- Handles media session for lock screen controls

#### UI Layer
- **MainActivity**: Main entry point with song list and player controls
- **SongAdapter**: RecyclerView adapter for displaying songs
- **MusicViewModel**: Manages UI state and business logic

#### Utilities
- **AudioScanner**: Scans device storage for audio files using MediaStore
- **PermissionHelper**: Handles runtime permission requests

## Technology Stack

- **Language**: Kotlin
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM with Repository pattern
- **UI Framework**: Material Design 3 components
- **Media Playback**: AndroidX Media3 (ExoPlayer)
- **Async Operations**: Kotlin Coroutines and Flow
- **Image Loading**: Glide
- **Persistence**: Room Database (for playlists - future)

## Permissions

The app requests the following permissions:

### Required Permissions
- **READ_MEDIA_AUDIO** (Android 13+): Access audio files
- **READ_EXTERNAL_STORAGE** (Android 12 and below): Access audio files
- **FOREGROUND_SERVICE**: Run music service in background
- **FOREGROUND_SERVICE_MEDIA_PLAYBACK**: Specific foreground service type
- **POST_NOTIFICATIONS** (Android 13+): Show playback notifications

## Building the Project

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- JDK 17
- Android SDK with API 34
- Gradle 8.2+

### Build Instructions

1. Clone the repository:
```bash
git clone https://github.com/vector-im/element-builder.git
cd element-builder
```

2. Open the project in Android Studio

3. Sync Gradle files

4. Build the project:
```bash
./gradlew build
```

5. Run on device or emulator:
```bash
./gradlew installDebug
```

## Project Structure

```
TinyAMP/
├── app/
│   ├── build.gradle                 # App-level Gradle configuration
│   ├── proguard-rules.pro          # ProGuard rules
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/tinyamp/player/
│           │   ├── data/
│           │   ├── service/
│           │   ├── ui/
│           │   ├── utils/
│           │   └── viewmodel/
│           └── res/
│               ├── drawable/        # Vector icons
│               ├── layout/          # XML layouts
│               ├── mipmap/          # App icons
│               ├── values/          # Strings, colors, themes
│               ├── values-night/    # Dark theme
│               └── xml/             # Backup rules
├── build.gradle                     # Root-level Gradle configuration
├── settings.gradle                  # Gradle settings
└── gradle.properties               # Gradle properties
```

## Future Enhancements

### Planned Features
- Playlist creation and management
- Audio equalizer integration
- Folder-based browsing
- Album and artist views
- Lyrics display
- Sleep timer
- Widget for home screen
- Android Auto support
- Search functionality
- Sorting options (by title, artist, date added)

### Technical Improvements
- Room database integration for favorites and playlists
- Dependency injection (Hilt/Koin)
- Unit and integration tests
- CI/CD pipeline
- Performance optimizations

## Development Guidelines

### Code Style
- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Add KDoc comments for public APIs
- Keep functions small and focused

### Git Workflow
- Create feature branches from `main`
- Use descriptive commit messages
- Follow conventional commits format
- Test before committing

### Testing
- Write unit tests for business logic
- Test edge cases and error handling
- Use MockK for mocking in tests

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- Material Design 3 components by Google
- ExoPlayer/Media3 by Google
- Glide image loading library
- Android community for best practices and guidelines

## Contact

For questions, issues, or contributions, please open an issue on GitHub.

---

Built with Kotlin and Material Design 3
