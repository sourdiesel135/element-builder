package com.tinyamp.player.data.models

enum class PlaybackStatus {
    IDLE,
    PLAYING,
    PAUSED,
    STOPPED
}

data class PlaybackState(
    val currentSong: Song? = null,
    val status: PlaybackStatus = PlaybackStatus.IDLE,
    val currentPosition: Long = 0L,
    val duration: Long = 0L,
    val isShuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.OFF
) {
    val progress: Float
        get() = if (duration > 0) currentPosition.toFloat() / duration.toFloat() else 0f

    val isPlaying: Boolean
        get() = status == PlaybackStatus.PLAYING
}

enum class RepeatMode {
    OFF,      // No repeat
    ONE,      // Repeat current song
    ALL       // Repeat all songs
}
